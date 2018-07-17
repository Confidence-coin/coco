package com.gazman.coco.desktop.controllers.send_coins;

import com.gazman.coco.core.api.SummeryData;
import com.gazman.lifecycle.Singleton;

import java.util.ArrayList;

/**
 * Created by Ilya Gazman on 7/15/2018.
 */
public class TransactionsModel implements Singleton {
    public SummeryData summeryData;

    private ArrayList<TransactionData> transactionDatas = new ArrayList<>();

    public void addTransaction(TransactionData transactionData){
        transactionDatas.add(transactionData);
    }

    public ArrayList<TransactionData> getTransactionDatas() {
        return transactionDatas;
    }

    public void clear() {
        summeryData = null;
        getTransactionDatas().clear();
    }
}
