package com.gazman.coco.pool.handlers.transaction;


import com.gazman.coco.core.api.ListTypes;
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
        String error;
        try (InputStream inputStream = exchange.getRequestBody()) {
            error = onHandle(inputStream);
        } catch (Throwable e) {
            e.printStackTrace();
            error = "Internal error";
        }
        try {
            if (error != null) {
                sendError(exchange, error);
            } else {
                sendResponse(exchange, "success", 200);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected String onHandle(InputStream inputStream) throws IOException, SQLException {
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

        byte[] blockHash = new byte[32];
        buffer.get(blockHash);
        try (DB db = Factory.inject(DB.class)) {
            int blockId = blockModel.findBlockId(db, blockHash);
            if (blockId == -1) {
                return "Wrong block hash or transaction expired";
            }
            return handlers[type].handle(type, publicKey, signature, blockId, buffer, db);
        }
    }

    interface TransactionHandler {
        String handle(int type, byte[] publicKey, byte[] signature, int blockId, ByteBuffer buffer, DB db) throws IOException;
    }
}
