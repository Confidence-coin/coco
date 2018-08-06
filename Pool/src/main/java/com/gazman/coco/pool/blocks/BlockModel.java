package com.gazman.coco.pool.blocks;

import com.gazman.coco.db.DB;
import com.gazman.lifecycle.Singleton;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Ilya Gazman on 1/23/2018.
 */
public class BlockModel implements Singleton {

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
