package com.gazman.coco.core.utils;

import com.gazman.coco.db.DB;
import org.bitcoinj.core.Base58;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Ilya Gazman on 1/23/2018.
 */
public class BlockUtils {
    private static final int _10_MINUTES = 1000 * 60 * 10;

    public static byte[] queryDifficulty(DB db, int blockId) {
        if (blockId < 144) {
            return Base58.decode("11111163TjSsDPN4E2ESjYR66ncR989TJ7sAa28saS");
        }
        if (blockId < 288) {
            byte[] previousDifficulty = Base58.decode("111HVkPNMRztH8Y1Q5XmiTczYaRjPnC69AWQcuwK6i");
            long actualTime = getTimeBetweenBlocks(db, 144, 95);
            long expectedTime = (144 - 95) * _10_MINUTES;
            return computeDifficulty(previousDifficulty, actualTime, expectedTime);
        }

        byte[] previousDifficulty = getPreviousDifficulty(db, blockId);
        long actualTime = getTimeBetweenBlocks(db, blockId / 144 * 144,
                blockId / 144 * 144 - 144);
        long expectedTime = 144 * _10_MINUTES;
        return computeDifficulty(previousDifficulty, actualTime, expectedTime);
    }

    public static byte[] queryBlockHash(DB db, int blockId) {
        byte hash[][] = new byte[1][];
        db.query("select current_block_hash from core.blocks where id = " + blockId, resultSet -> {
            if (!resultSet.next()) {
                return false;
            }
            hash[0] = resultSet.getBytes(1);
            return !resultSet.wasNull();
        });
        return hash[0];
    }

    private static long getTimeBetweenBlocks(DB db, int blockA, int blockB) {
        long timeDif[] = new long[]{-1};
        db.query("SELECT max(mined_date) - min(mined_date) from core.blocks WHERE id in " +
                "(" + blockA + "," + blockB + ")", new DB.StatementHandler() {
            @Override
            public boolean handle(ResultSet resultSet) throws SQLException {
                if (resultSet.next()) {
                    timeDif[0] = resultSet.getInt(1);
                    return true;
                }
                return false;
            }
        });
        return timeDif[0];
    }

    private static byte[] computeDifficulty(byte[] previousDifficulty, long actualTime, long expectedTime) {
        double delta = expectedTime / (double) actualTime;

        BigDecimal d = BigDecimal.valueOf(delta);
        BigDecimal preDifficulty = new BigDecimal(ByteUtils.toBigInteger(previousDifficulty));

        return ByteUtils.toByteArray(preDifficulty.multiply(d).toBigInteger());
    }

    private static byte[] getPreviousDifficulty(DB db, int blockId) {
        byte[][] dif = new byte[1][];
        db.query("SELECT difficulty from core.blocks WHERE id = " + (blockId / 144 * 144 - 1), resultSet -> {
            if (resultSet.next()) {
                dif[0] = resultSet.getBytes(1);
                return true;
            }
            return false;
        });
        return dif[0];
    }
}
