package com.gazman.coco.pool.handlers.transaction;


import com.gazman.coco.core.api.ListTypes;
import com.gazman.coco.core.api.SummeryData;
import com.gazman.coco.core.utils.MultiByteInteger;
import com.gazman.coco.core.utils.Utils;
import com.gazman.coco.db.DB;
import com.gazman.coco.pool.blocks.BlockModel;
import com.gazman.coco.pool.handlers.BaseHandler;
import com.gazman.lifecycle.Factory;
import com.sun.net.httpserver.HttpExchange;
import org.whispersystems.curve25519.Curve25519;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.sql.SQLException;

/**
 * Created by Ilya Gazman on 1/19/2018.
 */
public class RootTransactionHandler extends BaseHandler {

    private BlockModel blockModel = Factory.inject(BlockModel.class);

    private TransactionHandler[] handlers = new TransactionHandler[100];

    {
        handlers[ListTypes.TYPE_1_TO_1] = Factory.inject(Transaction1To1Handler.class);
    }

    @Override
    public final void handle(HttpExchange exchange) {
        boolean preparing = parseParams(exchange).getOrDefault("preparing", "false").toLowerCase().equals("true");

        String error;
        try (InputStream inputStream = exchange.getRequestBody()) {
            error = onHandle(exchange, inputStream, preparing);
        } catch (Throwable e) {
            e.printStackTrace();
            error = "Internal error";
        }
        try {
            if (error != null) {
                sendError(exchange, error);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    String onHandle(HttpExchange exchange, InputStream inputStream, boolean preparing) throws IOException, SQLException {
        if (inputStream == null) {
            return "Error reading message";
        }

        byte[] signature = new byte[64];
        if (inputStream.read(signature) != signature.length) {
            return "Error reading signature";
        }

        int length = MultiByteInteger.parse(inputStream);
        if (length == -1) {
            return "Error reading length";
        }

        byte[] data = new byte[length];
        if (inputStream.read(data) != data.length) {
            return "Error reading message";
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte[] publicKey = new byte[32];
        buffer.get(publicKey);
        Curve25519 cipher = Utils.createCipher();
        if (!cipher.verifySignature(publicKey, data, signature)) {
            return "Wrong signature";
        }

        int type = MultiByteInteger.parse(buffer);
        if (type == -1) {
            return "Error reading type";
        }
        if (type < 0 || type > handlers.length || handlers[type] == null) {
            return "Unknown type " + type;
        }


        try (DB db = Factory.inject(DB.class)) {
            byte[] blockHash;
            if (!preparing) {
                blockHash = new byte[32];
                buffer.get(blockHash);
            } else {
                blockHash = getLatestBlockHash(db);
                if (blockHash == null) {
                    return "Internal error fetching block hash";
                }
            }

            int blockId = blockModel.findBlockId(db, blockHash);
            if (blockId == -1) {
                return "Wrong block hash or transaction expired";
            }
            SummeryData summeryData = new SummeryData();
            summeryData.blockHash = blockHash;
            if (preparing) {
                TransactionHandler.FeeData feeData = handlers[type].prepare(type, publicKey, signature, blockId, buffer, db);
                if (feeData.errorMessage != null) {
                    return feeData.errorMessage;
                }

                summeryData.totalFees = feeData.totalFees;
                sendResponse(exchange, summeryData, 200);
            } else {
                String errorMessage = handlers[type].execute(type, publicKey, signature, blockId, buffer, db);
                if (errorMessage != null) {
                    return errorMessage;
                }
                sendResponse(exchange, summeryData, 200);
            }
            return null;
        }
    }

    private byte[] getLatestBlockHash(DB db) {
        byte[][] hash = new byte[1][];
        db.query("SELECT current_block_hash from core.blocks\n" +
                "ORDER BY id desc LIMIT 1", resultSet -> {
            if (!resultSet.next()) {
                return false;
            }
            hash[0] = resultSet.getBytes(1);
            return true;
        });
        return hash[0];
    }

    interface TransactionHandler {
        String execute(int type, byte[] senderFullId, byte[] signature, int blockId, ByteBuffer buffer, DB db) throws IOException;

        FeeData prepare(int type, byte[] senderFullId, byte[] signature, int blockId, ByteBuffer buffer, DB db) throws IOException;

        class FeeData {
            String errorMessage;
            double totalFees;
        }
    }
}
