package com.gazman.coco.pool.blocks;

import com.gazman.coco.core.utils.ByteUtils;
import com.gazman.coco.core.utils.MultiByteInteger;
import com.gazman.coco.db.DB;
import com.gazman.coco.pool.settings.PoolSettings;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

public class BlockBuilder {
    private static final byte[] VERSION_UUID = ByteUtils.toByteArray(UUID.fromString("38cc90f9-163d-4a18-9878-51d0c8666efa"));
    private static final int LEADERSHIP_REWARD_ID = 1;

    private ByteArrayOutputStream outputStream;
    private byte[] byteBuffer = new byte[32];
    private ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);
    private int blockId;
    private String domain;
    private int rewardId;
    private byte[] smartContractsExecutionHash;
    private byte[] previousBlockHash;
    private byte[] headerAndListsHash;
    private byte[] jobId;
    private long minedDate;
    private long blob;
    private byte[] blockHash;
    private byte[][] lists;
    private double transactionsFees;
    private double smartContractsFees;

    public byte[] build(){
        outputStream = new ByteArrayOutputStream();

        try(DB db = new DB()){
            writeHeader(db);
            writeLists(db);
            writeFooter(db);
        }

        return outputStream.toByteArray();
    }

    private void writeLists(DB db) {
        write(lists.length);
    }

    private void writeFooter(DB db) {
        write(headerAndListsHash);
        write(jobId);
        write(minedDate);
        write(blob);
        write(blockHash);
    }

    private void writeHeader(DB db) {
        if(!loadLastBlockData(db)){
            throw new Error("Failed load last block data");
        }
        int blockId = fetchBlockId(db);
        write(blockId);
        write(VERSION_UUID);
        write(PoolSettings.POOL_DOMAIN);
        write(transactionsFees);
        write(smartContractsFees);
        write(calculateBlockReward(blockId));
        write(PoolSettings.POOL_MINER_ID);
        write(LEADERSHIP_REWARD_ID);
        write(smartContractsExecutionHash);
        write(previousBlockHash);
    }

    private boolean loadLastBlockData(DB db) {
        return db.query("select transactions_fees, smart_contracts_fees from pool.fees", resultSet -> {
            if(!resultSet.next()) {
                return false;
            }
            transactionsFees = resultSet.getDouble("transactions_fees");
            smartContractsFees = resultSet.getDouble("smart_contracts_fees");
            smartContractsExecutionHash = resultSet.getBytes("smart_contracts_execution_hash");
            return true;
        });
    }

    private int fetchBlockId(DB db) {
        final int[] blockId = new int[1];
        db.query("select max(id) from core.blocks", resultSet -> {
            if (resultSet.next()){
                blockId[0] = resultSet.getInt(1);
            }
            return false;
        });
        return blockId[0];
    }

    private int calculateBlockReward(int blockId) {
        return 250 - blockId / 3188;
    }

    private void write(int value) {
        byte[] bytes = MultiByteInteger.encode(value);
        outputStream.write(bytes, 0, bytes.length);
    }

    private void write(long value) {
        buffer.rewind();
        buffer.putLong(value);
        outputStream.write(byteBuffer, 0, 8);
    }

    private void write(double value) {
        buffer.rewind();
        buffer.putDouble(value);
        outputStream.write(byteBuffer, 0, 8);
    }

    private void write(String value) {
        byte[] bytes = value.getBytes(Charset.forName("UTF-8"));
        write(MultiByteInteger.encode(bytes.length));
        write(bytes);
    }

    private void write(byte[] value) {
        outputStream.write(value, 0, value.length);
    }
}
