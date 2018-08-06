package com.gazman.coco.pool.transactions;

public class TransactionsService implements Runnable {
    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            checkForNewTransactions();

        }
    }

    private void checkForNewTransactions() {

    }
}
