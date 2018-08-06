package com.gazman.coco.pool.handlers.work;

import com.gazman.coco.core.api.WorkData;
import com.gazman.coco.core.hash.Sha256Hash;
import com.gazman.coco.core.utils.ByteUtils;
import com.gazman.coco.core.utils.DifficultyUtils;
import com.gazman.coco.core.utils.MiningUtils;
import com.gazman.coco.db.DB;
import com.gazman.coco.pool.handlers.ParamsHandler;
import com.gazman.coco.pool.settings.PoolSettings;
import com.sun.net.httpserver.HttpExchange;
import org.bitcoinj.core.Base58;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Created by Ilya Gazman on 1/18/2018.
 */
public class WorkHandler extends ParamsHandler {

    @Override
    protected String onHandle(HttpExchange exchange, byte[] publicKey, HashMap<String, String> params) throws Exception {
        WorkData workData = new WorkData();
        try (DB db = new DB()) {
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

            int difficulty = readInt(params, "difficulty", 0);
            if (difficulty == 0) {
                return "No difficulty provided";
            }

            workData.shareErrorMessage = validateShare(db, publicKey,
                    headerAndTransactionsHash[0],
                    blockId[0],
                    readLong(params, "workId", -1),
                    readLong(params, "blob", -1),
                    readLong(params, "minedDate", -1), DifficultyUtils.getPoolDifficulty(difficulty));

            workData.step3Hash = headerAndTransactionsHash[0];
            workData.workId = id[0];

        }
        sendResponse(exchange, workData, 200);

        return null;
    }

    private String validateShare(DB db, byte[] publicKey, byte[] headerAndTransactionsHash, int blockId, long workId, long blob, long minedDate, byte[] poolDifficulty) throws NoSuchAlgorithmException, IOException, DigestException {
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

        byte[] jobId = computeUUID(publicKey, blockId);
        byte[] blockHash = MiningUtils.computeBlockHash(headerAndTransactionsHash, blob, minedDate);

        String error = coalesce(
                () -> validateWork(blockHash, poolDifficulty),
                () -> rewardWorker(db, publicKey, blob, minedDate, blockId)
        );
        if (error != null) {
            return error;
        }

        computeBlock(headerAndTransactionsHash, jobId, minedDate, blob, blockHash);

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

    interface StringTester {
        String test();
    }

    private String coalesce(StringTester... args) {
        for (StringTester stringTester : args) {
            String test = stringTester.test();
            if (test != null) {
                return test;
            }
        }
        return null;
    }


    private String validateWork(byte[] blobkHash, byte[] poolDifficulty) {
        if (ByteUtils.compare(blobkHash, poolDifficulty) >= 0) {
            return "bad hash";
        }
        return null;
    }

    private String rewardWorker(DB db, byte[] publicKey, long blob, long minedDate, int blockId) {
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
