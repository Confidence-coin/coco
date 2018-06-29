package com.gazman.coco.pool.handlers.transaction;

import com.gazman.coco.db.DB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Ilya Gazman on 2/14/2018.
 */
public class TransactionsHelper {

    public static final int SIGNATURE_SIZE = 64;
    public static final int KEY_SIZE = 32;

    public Sender fetchSenderId(int blockId, DB db, int senderId) {
        Sender[] senderHolder = new Sender[1];
        db.query("SELECT coalesce(b.balance, a.balance) as balance, public_key from core.wallets a " +
                "left JOIN pool.tmp_wallets b on a.wallet_id = b.wallet_id and b.block_id = " + blockId +
                " WHERE a.wallet_id = " + senderId, new DB.StatementHandler() {
            @Override
            public boolean handle(ResultSet resultSet) throws SQLException {
                if (!resultSet.next()) {
                    return false;
                }

                Sender sender = new Sender();
                sender.balance = resultSet.getDouble(1);
                sender.publicKey = resultSet.getBytes(2);
                senderHolder[0] = sender;

                return true;
            }
        });

        return senderHolder[0];
    }

    public Wallet[] fetchWallets(DB db, byte[]... ids) {
        StringBuilder query = new StringBuilder("SELECT wallet_id, public_key from core.wallets WHERE public_key in(");
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                query.append(",");
            }
            query.append("?");
        }
        query.append(")");


        try (PreparedStatement statement = db.getConnection().prepareStatement(query.toString())) {
            for (int i = 0; i < ids.length; i++) {
                byte[] id = ids[i];
                statement.setBytes(i + 1, id);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                ArrayList<Wallet> result = new ArrayList<>();
                for (int i = 0; resultSet.next(); i++) {
                    Wallet wallet = new Wallet();
                    wallet.id = resultSet.getInt(1);
                    wallet.publicKey = resultSet.getBytes(2);
                    result.add(wallet);
                }
                Wallet[] wallets = new Wallet[result.size()];
                return result.toArray(wallets);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Wallet[0];
    }

    public double fetchBalance(DB db, int senderId) {
        double[] balance = new double[1];
        if (!db.query("SELECT balance from core.wallets WHERE wallet_id = " + senderId, resultSet -> {
            if (!resultSet.next()) {
                return false;
            }
            balance[0] = resultSet.getDouble(1);
            return true;
        })) {
            return -1;
        }
        return balance[0];
    }

    public class Wallet {
        byte[] publicKey;
        int id;
    }

    public class Sender {
        byte[] publicKey;
        double balance;
    }
}
