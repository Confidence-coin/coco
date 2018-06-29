package com.gazman.coco.pool.blocks;

import com.gazman.coco.core.block.Wallet;
import com.gazman.coco.core.hash.Sha256Hash;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Ilya Gazman on 1/15/2018.
 */
public class Block implements Cloneable {

    private static final int HEADER_SIZE = 88;

    // Header
    public int blockId;
    public byte[] difficulty;
    public byte[] coreVersion;
    public int domainByeLength;
    public String poolDomain;
    public double transactionsFees;
    public double smartContractsFees;
    public int blockReword;
    public int rewordId;
    public int leadershipRewordId;
    public int smartContractsExecutionHash;
    public byte[] previousBlockHash;

    //Footer
    public byte[] jobId = new byte[16];
    public long minedDate;
    public long blob;
    public byte[] blockHash;

    public byte[] transactions;
    public ArrayList<Wallet> wallets;

    private byte[] preHash() {
        int capacity = HEADER_SIZE + transactions.length;
        if (wallets.size() > 0) {
            capacity += 4 + wallets.size() * 37;
        }
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        computeHeader(buffer);
        buffer.put(transactions);
        if (wallets.size() > 0) {
            buffer.putInt(wallets.size());
            for (Wallet wallet : wallets) {
                buffer.putInt(wallet.id);
                buffer.put(wallet.address);
            }
        }

        return Sha256Hash.hash(buffer.array());
    }

    public byte[] hash() {
        byte[] timeHash = computeTimeHash();
        byte[] blobHash = computeBlobHash("SHA-256");
        byte[] blobHash2;
        int seed = 0;
        for (byte hash : blobHash) {
            seed += hash & 0xFF;
        }
        int algorithm = new Random(seed).nextInt(5);
        switch (algorithm) {
            case 1:
                blobHash2 = computeBlobHash("SHA-1");
                break;
            case 2:
                blobHash2 = computeBlobHash("SHA-384");
                break;
            case 3:
                blobHash2 = computeBlobHash("SHA-512");
                break;
            default: // 0 and 4 are MD5
                blobHash2 = computeBlobHash("MD5");
                break;
        }

        ByteBuffer buffer = ByteBuffer.allocate(blobHash2.length + 64);
        buffer.put(timeHash);
        buffer.put(blobHash);
        buffer.put(blobHash2);

        return Sha256Hash.hash(buffer.array());
    }

    private byte[] computeTimeHash() {
        byte[] blockHash = computeBlockHash();
        ByteBuffer byteBuffer = ByteBuffer.allocate(40);
        byteBuffer.putLong(minedDate);
        byteBuffer.put(blockHash);
        return Sha256Hash.hash(byteBuffer.array());
    }

    private byte[] computeBlobHash(String algorithm) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(blob);
        try {
            return MessageDigest.getInstance(algorithm).digest(buffer.array());
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    private byte[] computeBlockHash() {
        byte[] preHash = preHash();
        ByteBuffer buffer = ByteBuffer.allocate(48);
        buffer.put(preHash);
        buffer.put(jobId);

        return Sha256Hash.hash(buffer.array());
    }

    private void computeHeader(ByteBuffer buffer) {
        buffer.putInt(blockId);
        buffer.put(difficulty);
        buffer.put(coreVersion);
        buffer.putInt(rewordId);
        buffer.put(previousBlockHash);
    }


    @Override
    public Block clone() {
        try {
            return (Block) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
