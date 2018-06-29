package com.gazman.coco.pool.blocks;

import com.gazman.coco.core.utils.BlockUtils;
import com.gazman.coco.core.utils.ByteUtils;
import com.gazman.coco.db.DB;
import com.gazman.coco.pool.settings.PoolSettings;
import com.gazman.lifecycle.Singleton;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Ilya Gazman on 1/23/2018.
 */
public class BlockModel implements Singleton {

    private byte[] poolDifficulty;

    public byte[] getPoolDifficulty(DB db, int blockId) {
        if (poolDifficulty == null) {
            poolDifficulty = getPoolDifficulty(db, PoolSettings.POOL_DIFFICULTY_MULTIPLIER, blockId);
        }
        return poolDifficulty;
    }

    private byte[] getPoolDifficulty(DB db, int power, int blockId) {
        byte[] difficulty = BlockUtils.queryDifficulty(db, blockId);
        double delta = 1D / power;
        BigDecimal d = BigDecimal.valueOf(delta);
        BigDecimal difficultyValue = new BigDecimal(ByteUtils.toBigInteger(difficulty));
        return ByteUtils.toByteArray(difficultyValue.multiply(d).toBigInteger(), difficulty.length);
    }

    public int findBlockId(DB db, byte[] blockHash) throws SQLException {
        try (PreparedStatement statement = db.getConnection().prepareStatement("select id from core.blocks where " +
                "current_block_hash = ?")) {
            statement.setBytes(1, blockHash);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return -1;
    }
}
