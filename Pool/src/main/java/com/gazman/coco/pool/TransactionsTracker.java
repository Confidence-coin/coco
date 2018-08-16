package com.gazman.coco.pool;

import com.gazman.coco.core.hash.Sha256Hash;
import com.gazman.coco.core.utils.ByteUtils;
import com.gazman.coco.db.DB;
import com.gazman.coco.db.NullValue;
import com.gazman.coco.pool.blocks.BlockUtils;
import com.gazman.lifecycle.log.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Comparator;
import java.util.TreeMap;

public class TransactionsTracker {

    private TreeMap<TransactionData, TransactionData> hashLists = new TreeMap<>(Comparator
            .comparingInt((TransactionData o) -> o.blockId)
            .thenComparingInt(o -> o.type)
            .reversed()
            .thenComparingInt(o -> o.features));

    private int maxLastItem = 0;
    private Logger logger = Logger.create("transactionsTracker");
    private int blockId = 0;
    private byte[] timedSmartContractsHash;
    private int timedSmartContractsFees;
    private byte[] currentBlockHash;
    private boolean needDefaultItem = true;


    public static void main(String... args) throws InterruptedException {
        new TransactionsTracker().start();
    }

    private void start() throws InterruptedException {
        init();
        //noinspection InfiniteLoopStatement
//        while (true)
        {
            checkForNewTransactions();
            Thread.sleep(1000);
        }
    }

    private void checkForNewTransactions() {
        logger.d("Checking for transactions");
        @SuppressWarnings("unchecked")
        TreeMap<TransactionData, TransactionData> hashLists = (TreeMap<TransactionData, TransactionData>) this.hashLists.clone();
        try (DB db = new DB()) {
            updateBlockData(db);
            if (db.query("select block_id, type, features, transaction_fees, smart_contract_fees, id, " +
                            "smart_contract_hash, transaction_hash from pool.transactions where id > " + maxLastItem,
                    resultSet -> fetchTransactions(hashLists, resultSet)) || needDefaultItem) {
                db.beginTransaction();
                if(hashLists.size() == 0){
                    TreeMap<TransactionData, TransactionData> defaultHashList = createDefaultHashList();
                    updateHashList(db, defaultHashList);
                    updateNextBlock(db, aggregateTransactions(defaultHashList));
                }
                else {
                    updateHashList(db, hashLists);
                    updateNextBlock(db, aggregateTransactions(hashLists));
                    this.hashLists = hashLists;
                }
                needDefaultItem = false;
                db.markTransactionSuccessful();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private TreeMap<TransactionData, TransactionData> createDefaultHashList() {
        @SuppressWarnings("unchecked") TreeMap<TransactionData, TransactionData>
                hashList = (TreeMap<TransactionData, TransactionData>) hashLists.clone();
        TransactionData transactionData = new TransactionData();
        transactionData.transactionsHash = Sha256Hash.hash(new byte[0]);
        transactionData.blockId = blockId;
        hashList.put(transactionData, transactionData);
        return hashList;
    }

    private void updateTimesSmartContractsHash() {
        timedSmartContractsHash = new byte[0];
        timedSmartContractsFees = 0;
    }

    private boolean fetchTransactions(TreeMap<TransactionData, TransactionData> hashLists, ResultSet resultSet) throws SQLException {
        TransactionData fetchedTransaction = new TransactionData();
        TransactionData currentTransaction = null;
        int itemsFound = 0;

        while (resultSet.next()) {
            itemsFound++;
            fetchedTransaction.blockId = resultSet.getInt("block_id");
            fetchedTransaction.type = resultSet.getInt("type");
            fetchedTransaction.features = (byte) resultSet.getInt("features");
            fetchedTransaction.transactionsFees = resultSet.getDouble("transaction_fees");
            fetchedTransaction.smartContractsFees = resultSet.getDouble("smart_contract_fees");
            fetchedTransaction.lastItemId = resultSet.getInt("id");
            fetchedTransaction.smartContractsHash = resultSet.getBytes("smart_contract_hash");
            fetchedTransaction.transactionsHash = resultSet.getBytes("transaction_hash");
            if (!fetchedTransaction.equals(currentTransaction)) {
                currentTransaction = hashLists.computeIfAbsent(fetchedTransaction, transactionData -> fetchedTransaction.clone());
            }
            if (fetchedTransaction.transactionsHash != currentTransaction.transactionsHash) {
                currentTransaction.lastItemId = fetchedTransaction.lastItemId;
                currentTransaction.transactionsHash = Sha256Hash.hash(currentTransaction.transactionsHash, fetchedTransaction.transactionsHash);
                currentTransaction.transactionsFees += fetchedTransaction.transactionsFees;

                if (fetchedTransaction.smartContractsHash != null) {
                    currentTransaction.smartContractsHash = Sha256Hash.hash(currentTransaction.smartContractsHash, fetchedTransaction.smartContractsHash);
                    currentTransaction.smartContractsFees += fetchedTransaction.smartContractsFees;
                }
            }
        }
        logger.d("Found", itemsFound, "items");
        return itemsFound > 0;
    }

    private void updateBlockData(DB db) {
        if (!db.query("select id, block_hash from core.blocks order by id desc", resultSet -> {
            if (resultSet.next()) {
                int blockId = resultSet.getInt("id");
                if(blockId != this.blockId){
                    db.truncateTable("pool.next_block");
                    updateTimesSmartContractsHash();
                }
                byte[] blockHash = resultSet.getBytes("block_hash");
                this.blockId = blockId;
                this.currentBlockHash = blockHash;
            }
            else{
                this.blockId = 93;
                this.currentBlockHash = Sha256Hash.hash("Big Bang".getBytes(StandardCharsets.UTF_8));
            }

            return true;
        })) {
            throw new Error("error fetching blockId");
        }
    }

    private TransactionData aggregateTransactions(TreeMap<TransactionData, TransactionData> map) {
        TransactionData result = new TransactionData();
        for (TransactionData transactionData : map.keySet()) {
            if (result.transactionsHash == null) {
                result.transactionsHash = transactionData.transactionsHash;
            } else {
                byte[] concatenatedBytes = ByteUtils.toByteArray(
                        result.transactionsHash, transactionData.transactionsHash);
                result.transactionsHash = Sha256Hash.hash(concatenatedBytes);
            }
        }
        return result;
    }

    private void updateNextBlock(DB db, TransactionData transactionData) throws IOException {
        byte[] smartContractsExecutionHash = Sha256Hash.hash(transactionData.smartContractsHash, timedSmartContractsHash);
        db.insert("pool.next_block")
                .add("transactions_fees", transactionData.transactionsFees)
                .add("smart_contracts_fees", transactionData.smartContractsFees + timedSmartContractsFees)
                .add("smart_contracts_execution_hash", smartContractsExecutionHash)
                .add("footer_hash", computeFooterHash(transactionData, smartContractsExecutionHash))
                .add("block_id", blockId + 1)
                .execute();
    }

    private byte[] computeFooterHash(TransactionData transactionData, byte[] smartContractsExecutionHash) throws IOException {
        byte[] header = BlockUtils.computeHeaderHash(blockId + 1,
                transactionData.transactionsFees, transactionData.smartContractsFees,
                smartContractsExecutionHash, currentBlockHash);

        return Sha256Hash.hash(header, transactionData.transactionsHash);
    }

    private void updateHashList(DB db, TreeMap<TransactionData, TransactionData> hashLists) {
        db.truncateTable("pool.transactions_hash_lists");
        db.insertBatch("pool.transactions_hash_lists", hashLists.keySet(), (data, values) -> {
            values.put("last_item_id", data.lastItemId);
            values.put("block_id", data.blockId);
            values.put("type", data.type);
            values.put("features", data.features);
            values.put("smart_contracts_fees", data.smartContractsFees);
            Object smartContractsHash = data.smartContractsHash;
            if(smartContractsHash == null){
                smartContractsHash = new NullValue(Types.ARRAY);
            }
            values.put("smart_contracts_hash", smartContractsHash);
            values.put("transactions_fees", data.transactionsFees);
            values.put("transactions_hash", data.transactionsHash);
        });
    }

    private void init() {
        try (DB db = new DB()) {
                db.query("select last_item_id, block_id, type, features, smart_contracts_fees, " +
                        "smart_contracts_hash,transactions_fees, transactions_hash from pool.transactions_hash_lists", resultSet -> {
                while (resultSet.next()) {
                    TransactionData transactionData = new TransactionData();

                    transactionData.lastItemId = resultSet.getInt("last_item_id");
                    transactionData.blockId = resultSet.getInt("block_id");
                    transactionData.type = resultSet.getInt("type");
                    transactionData.features = resultSet.getByte("features");
                    transactionData.smartContractsFees = resultSet.getDouble("smart_contracts_fees");
                    transactionData.smartContractsHash = resultSet.getBytes("smart_contracts_hash");
                    transactionData.transactionsFees = resultSet.getDouble("transactions_fees");
                    transactionData.transactionsHash = resultSet.getBytes("transactions_hash");

                    if(transactionData.transactionsFees != 0){
                        hashLists.put(transactionData, transactionData);
                    }
                    if (transactionData.lastItemId > maxLastItem) {
                        maxLastItem = transactionData.lastItemId;
                    }
                    needDefaultItem = false;
                }
                return true;
            });
        }
    }

    private class TransactionData implements Cloneable {
        double smartContractsFees;
        double transactionsFees;
        byte[] smartContractsHash;
        int blockId, type, lastItemId;
        byte features;
        byte[] transactionsHash;

        @Override
        public int hashCode() {

            return blockId + (type << 8) + features;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TransactionData) {
                TransactionData transactionData = (TransactionData) obj;
                return transactionData.blockId == blockId &&
                        transactionData.features == features &&
                        transactionData.type == type;
            }
            return super.equals(obj);
        }

        @Override
        public TransactionData clone() {
            try {
                return (TransactionData) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new Error(e);
            }
        }
    }
}
