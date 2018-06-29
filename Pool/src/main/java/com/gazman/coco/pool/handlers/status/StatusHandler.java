package com.gazman.coco.pool.handlers.status;

import com.gazman.coco.core.api.StatusData;
import com.gazman.coco.db.DB;
import com.gazman.coco.pool.handlers.ParamsHandler;
import com.sun.net.httpserver.HttpExchange;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by Ilya Gazman on 1/18/2018.
 */
public class StatusHandler extends ParamsHandler {

    @Override
    protected String onHandle(HttpExchange exchange, byte[] publicKey, HashMap<String, String> params)
            throws Exception {

        try (DB db = new DB(); PreparedStatement statement =
                db.getConnection().prepareStatement("select balance from core.wallets where public_key = ?")) {
            statement.setBytes(1, publicKey);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return "Wallet not found";
            }
            double balance = resultSet.getDouble(1);

            byte[][] hash = new byte[1][];
            db.query("SELECT current_block_hash from core.blocks\n" +
                    "ORDER BY id desc LIMIT 1", new DB.StatementHandler() {
                @Override
                public boolean handle(ResultSet resultSet) throws SQLException {
                    if (!resultSet.next()) {
                        return false;
                    }
                    hash[0] = resultSet.getBytes(1);
                    return true;
                }
            });
            if (hash[0] == null) {
                return "Internal error: block not found";
            }
            StatusData statusData = new StatusData();
            statusData.balance = balance;
            statusData.blockHash = hash[0];

            sendResponse(exchange, statusData, 200);
        }
        return null;
    }
}
