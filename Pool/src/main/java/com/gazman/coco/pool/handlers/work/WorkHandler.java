package com.gazman.coco.pool.handlers.work;

import com.gazman.coco.core.api.WorkData;
import com.gazman.coco.core.hash.Sha256Hash;
import com.gazman.coco.core.utils.ByteUtils;
import com.gazman.coco.core.utils.MultiByteInteger;
import com.gazman.coco.db.DB;
import com.gazman.coco.pool.blocks.BlockModel;
import com.gazman.coco.pool.handlers.ParamsHandler;
import com.gazman.coco.pool.settings.PoolSettings;
import com.gazman.lifecycle.Factory;
import com.sun.net.httpserver.HttpExchange;
import org.bitcoinj.core.Base58;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Ilya Gazman on 1/18/2018.
 */
public class WorkHandler extends ParamsHandler {

    private BlockModel blockModel = Factory.inject(BlockModel.class);

    @Override
    protected String onHandle(HttpExchange exchange, byte[] publicKey, HashMap<String, String> params) throws Exception {
        WorkData workData = new WorkData();
        try (DB db = new DB()) {
            workData.shareErrorMessage = validateShare(db, publicKey,
                    readLong(params, "workId", -1),
                    readLong(params, "blob", -1),
                    readLong(params, "minedDate", -1));

            byte[][] headerAndTransactionsHash = new byte[1][];
            int[] id = new int[1];
            int[] blockId = new int[1];

            if (!db.query("SELECT id, block_id, hash from pool.next_block order by insert_date desc limit 1", resultSet -> {
                if (!resultSet.next()) {
                    return false;
                }
                id[0] = resultSet.getInt(1);
                blockId[0] = resultSet.getInt(2);
                headerAndTransactionsHash[0] = resultSet.getBytes(3);
                return true;
            })) {
                return "Internal fetching work";
            }

            workData.step3Hash = headerAndTransactionsHash[0];
            workData.difficulty = blockModel.getPoolDifficulty(db, blockId[0]);
            workData.workId = id[0];

        }
        sendResponse(exchange, workData, 200);

        return null;
    }

    private String validateShare(DB db, byte[] publicKey, long workId, long blob, long minedDate) throws NoSuchAlgorithmException, IOException {
        if (workId == -1) {
            return "no workId";
        }
        if (blob == -1) {
            return "no blob";
        }
        long currentTime = System.currentTimeMillis();
        long timeDif = Math.abs(currentTime - minedDate);
        if (timeDif > PoolSettings.SHARE_TIME_ERROR) {
            return "Wrong mineDate " + timeDif;
        }

        byte[][] headerAndTransactionsHash = new byte[1][];
        byte[][] data = new byte[1][];


        if (!db.query("SELECT data, hash from pool.next_block WHERE id = " + workId, resultSet -> {
            if (!resultSet.next()) {
                return false;
            }
            data[0] = resultSet.getBytes(1);
            headerAndTransactionsHash[0] = resultSet.getBytes(2);
            return true;
        })) {
            return "WorkId expired";
        }

        int blockId = MultiByteInteger.parse(data[0]);
        byte[] jobId = computeUUID(publicKey, blockId);

        byte[] blockHash = computeBlockHash(jobId, headerAndTransactionsHash[0], blob, minedDate);

        String error = coalesce(
                validateWork(db, blockHash, blockId),
                rewordWorker(db, publicKey, blob, minedDate, blockId)
        );
        if (error != null) {
            return error;
        }

        computeBlock(headerAndTransactionsHash[0], jobId, minedDate, blob, blockHash);

        return null;
    }

    private void computeBlock(byte[] headerAndTransactionsHash, byte[] jobId, long minedDate, long blob, byte[] blockHash) throws IOException {
        byte[] minedDateData = ByteUtils.toByteArray(minedDate);
        byte[] blobData = ByteUtils.toByteArray(blob);

        byte[] block = ByteUtils.toByteArray(headerAndTransactionsHash,
                jobId,
                minedDateData,
                blobData,
                blockHash);
        BlockRealiser.create(block, Base58.encode(blockHash));
    }

    private String coalesce(String... args) {
        for (String value : args) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private byte[] computeBlockHash(byte[] step2JobId,
                                    byte[] step1HeaderAndTransactions,
                                    long blob, long minedDate) throws NoSuchAlgorithmException {
        MessageDigest sha256 = Sha256Hash.createDigest();
        byte[] step3 = concatenateAndHash(sha256, step2JobId, step1HeaderAndTransactions);
        byte[] step4 = concatenateAndHash(sha256, ByteUtils.toByteArray(minedDate), step3);
        byte[] step5Blob = sha256.digest(ByteUtils.toByteArray(blob));
        long step6Seed = convertToLong(step5Blob);
        int step7Index = new Random(step6Seed).nextInt(5);
        byte[] step8 = randomHash(step5Blob, step7Index);

        return concatenateAndHash(sha256, step4, step5Blob, step8);
    }

    private byte[] randomHash(byte[] step5Blob, int index) throws NoSuchAlgorithmException {
        MessageDigest digest;
        switch (index) {
            case 1:
                digest = MessageDigest.getInstance("SHA-1");
                break;
            case 2:
                digest = MessageDigest.getInstance("SHA-384");
                break;
            case 3:
                digest = MessageDigest.getInstance("SHA-512");
                break;
            default: // 0 and 4 are MD5
                digest = MessageDigest.getInstance("MD5");
                break;
        }
        return digest.digest(step5Blob);
    }

    private long convertToLong(byte[] data) {
        byte[] out = new byte[8];
        for (int i = 0; i < out.length; i++) {
            for (int j = 0; j < 4; j++) {
                out[i] += data[i * 4 + j];
            }
        }

        return ByteBuffer.wrap(out).getLong();
    }

    private byte[] concatenateAndHash(MessageDigest digest, byte[]... items) {
        return digest.digest(ByteUtils.toByteArray(items));
    }

    private String validateWork(DB db, byte[] headerAndTransactionsHash, int blockId) {
        byte[] poolDifficulty = blockModel.getPoolDifficulty(db, blockId);
        if (ByteUtils.compare(headerAndTransactionsHash, poolDifficulty) >= 0) {
            return "bad hash";
        }
        return null;
    }

    private String rewordWorker(DB db, byte[] publicKey, long blob, long minedDate, int blockId) {
        int effectedRows = db.insert("pool.shares")
                .add("blob", blob)
                .add("mined_date", minedDate)
                .add("block_id", blockId)
                .execute();
        if (effectedRows == -1) {
            return "duplicate share";
        }

        db.insert("pool.workers")
                .add("address", publicKey)
                .add("shares", 1)
                .setConflictString("on CONFLICT (address) do UPDATE set shares = shares + 1")
                .execute();

        return null;
    }

    private byte[] computeUUID(byte[] publicKey, int blockId) {
        ByteBuffer buffer = ByteBuffer.allocate(PoolSettings.POOL_SECRET.length + publicKey.length + 4);
        buffer.put(PoolSettings.POOL_SECRET);
        buffer.put(publicKey);
        buffer.putInt(blockId);
        byte[] hash = Sha256Hash.hash(buffer.array());

        byte[] uuid = new byte[16];
        System.arraycopy(hash, 0, uuid, 0, 16);
        return uuid;
    }
}
