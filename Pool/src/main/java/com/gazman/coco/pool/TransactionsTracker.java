package com.gazman.coco.pool;

import com.gazman.coco.core.hash.Sha256Hash;
import com.gazman.coco.db.DB;
import com.gazman.coco.db.InsertCommand;
import com.gazman.lifecycle.log.Logger;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class TransactionsTracker {
    private HashMap<ListData, ListData> map = new HashMap<>();
    private int maxLastItem = 0;
    private Logger logger = Logger.create("transactionsTracker");


    public static void main(String... args) throws InterruptedException {
        new TransactionsTracker().start();
    }

    private void start() throws InterruptedException {
        init();
        //noinspection InfiniteLoopStatement
        while (true) {
            checkForNewTransactions();
            Thread.sleep(1000 * 10);
        }
    }

    private void checkForNewTransactions() {
        logger.d("Checking for transactions");
        try (DB db = new DB()) {
            if (db.query("select id, block_id, type, features, hash_code from pool.lists where id > " + maxLastItem, resultSet -> {
                ListData fetchedList = new ListData();
                ListData currentList = null;
                int itemsFound = 0;

                while (resultSet.next()) {
                    itemsFound++;
                    fetchedList.blockId = resultSet.getInt("block_id");
                    fetchedList.type = resultSet.getInt("type");
                    fetchedList.features = (byte) resultSet.getInt("features");
                    fetchedList.hash = resultSet.getBytes("hash");
                    fetchedList.lastItemId = resultSet.getInt("last_item_id");
                    if (!fetchedList.equals(currentList)) {
                        currentList = map.computeIfAbsent(fetchedList, listData -> fetchedList.clone());
                    }
                    if (fetchedList.hash != currentList.hash) {
                        ByteBuffer buffer = ByteBuffer.allocate(64);
                        buffer.put(currentList.hash);
                        buffer.put(fetchedList.hash);
                        currentList.hash = Sha256Hash.hash(buffer.array());
                        currentList.lastItemId = fetchedList.lastItemId;
                    }
                }
                logger.d("Found", itemsFound, "items");
                return itemsFound > 0;
            })) {
                updateHashList(db);
            }
        }
    }

    private void updateHashList(DB db) {
        db.beginTransaction();
        db.execute("truncate table pool.hash_lists");
        InsertCommand insertCommand = db.insert("pool.hash_lists");
        for (ListData value : map.values()) {
            insertCommand.add("block_id", value.blockId);
            insertCommand.add("type", value.type);
            insertCommand.add("features", value.features);
            insertCommand.add("hash", value.hash);
            insertCommand.add("last_item_id", value.lastItemId);
        }
        insertCommand.execute();
        db.markTransactionSuccessful();
    }

    private void init() {
        try (DB db = new DB()) {
            db.query("select block_id, type, features, last_item_id, hash from pool.hash_lists", resultSet -> {
                while (resultSet.next()) {
                    ListData listData = new ListData();
                    listData.blockId = resultSet.getInt("block_id");
                    listData.type = resultSet.getInt("type");
                    listData.features = (byte) resultSet.getInt("features");
                    listData.lastItemId = resultSet.getInt("last_item_id");
                    listData.hash = resultSet.getBytes("hash");
                    map.put(listData, listData);
                    if (listData.lastItemId > maxLastItem) {
                        maxLastItem = listData.lastItemId;
                    }
                }
                return true;
            });
        }
    }

    private class ListData implements Cloneable {
        int blockId, type, lastItemId;
        byte features;
        byte[] hash;

        @Override
        public int hashCode() {

            return blockId + (type << 8) + features;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ListData) {
                ListData listData = (ListData) obj;
                return listData.blockId == blockId &&
                        listData.features == features &&
                        listData.type == type;
            }
            return super.equals(obj);
        }

        @Override
        public ListData clone() {
            try {
                return (ListData) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new Error(e);
            }
        }
    }
}
