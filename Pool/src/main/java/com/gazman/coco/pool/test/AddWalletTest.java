package com.gazman.coco.pool.test;

import com.gazman.coco.db.DB;
import org.bitcoinj.core.Base58;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Ilya Gazman on 7/15/2018.
 */

public class AddWalletTest {
    /**
     * 5> mtaN8@7NCKANXqjCAFFAiGQnUuTd1LN6HKehh3cuYbiCcEkmH7
     * 4> 3QAbUq@7wgXbdDabyiVKZda5cMmdeLfvXmakEBDC52iQVE2ssRj
     */


    public static void main(String...args){
        try(DB db = new DB()){
            addWallet(db, "3QAbUq@7wgXbdDabyiVKZda5cMmdeLfvXmakEBDC52iQVE2ssRj", 200);
            db.query("select wallet_id, public_key from core.wallets", new DB.StatementHandler() {
                @Override
                public boolean handle(ResultSet resultSet) throws SQLException {
                    while (resultSet.next()){
                        int walletId = resultSet.getInt(1);
                        String address = Base58.encodeWithCheckSum(resultSet.getBytes(2));
                        System.out.println(walletId + "> " + address);
                    }
                    return true;
                }
            });
        }
    }

    private static void addWallet(DB db, String address, int balance) {
        try {
            db.insert("core.wallets")
                    .add("wallet_id", 4)
                    .add("balance", balance)
                    .add("public_key", Base58.decode(address))
                    .execute();
        }
        catch (Exception e){
            System.err.println("Error adding wallet");
        }
    }
}
