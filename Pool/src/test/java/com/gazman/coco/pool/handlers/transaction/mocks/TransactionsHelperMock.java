package com.gazman.coco.pool.handlers.transaction.mocks;

import com.gazman.coco.db.DB;
import com.gazman.coco.pool.handlers.transaction.TransactionsHelper;
import com.gazman.lifecycle.Singleton;

/**
 * Created by Ilya Gazman on 2/24/2018.
 */
public class TransactionsHelperMock extends TransactionsHelper implements Singleton {

    public Sender sender;
    public Wallet[] wallets;
    public double balance;

    @Override
    public Sender fetchSenderId(int blockId, DB db, int senderId) {
        return sender;
    }

    @Override
    public double fetchBalance(DB db, int senderId) {
        return balance;
    }

    @Override
    public Wallet[] fetchWallets(DB db, byte[]... ids) {
        return wallets;
    }
}
