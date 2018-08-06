package com.gazman.coco.core.utils;

import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class MiningUtils {

    public static byte[] computeBlockHash(byte[] headerAndTransactionsHash, long longBlob, long miningDate) throws NoSuchAlgorithmException, DigestException {
        final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        final MessageDigest md5 = MessageDigest.getInstance("MD5");
        final MessageDigest sha384 = MessageDigest.getInstance("SHA-384");
        final MessageDigest sha512 = MessageDigest.getInstance("SHA-512");

        if (headerAndTransactionsHash == null) {
            throw new Error("bad hash");
        }
        byte[] blockAndBlobAndBlobHash = new byte[128];
        byte[] blobHash = new byte[32];
        byte[] blobHash2 = new byte[64];
        byte[] solutionHash = new byte[32];

        ByteBuffer blobBuffer = ByteBuffer.allocate(8);
        blobBuffer.putLong(longBlob);
        blobBuffer.rewind();
        int minerId = blobBuffer.getInt();
        int blob = blobBuffer.getInt();

        ByteBuffer timeBuffer = ByteBuffer.allocate(40);

        //updateTime
        timeBuffer.putLong(miningDate);
        timeBuffer.put(headerAndTransactionsHash);
        byte[] timeHash = sha256.digest(timeBuffer.array());
        System.arraycopy(timeHash, 0, blockAndBlobAndBlobHash, 0, 32);

        byte[] seedData = new byte[8];
        ByteBuffer seedBuffer = ByteBuffer.wrap(seedData);


        computeBlockHash(minerId, blob, blobBuffer, seedBuffer, new Random(),
                sha256, sha1, md5, sha384, sha512, blockAndBlobAndBlobHash, blobHash, blobHash2, solutionHash, seedData);

        return solutionHash;
    }

    public static void computeBlockHash(int minerId,
                                        int blob, ByteBuffer blobBuffer, ByteBuffer seedBuffer, Random random,
                                        MessageDigest sha256,
                                        MessageDigest sha1,
                                        MessageDigest md5,
                                        MessageDigest sha384,
                                        MessageDigest sha512,
                                        byte[] blockAndBlobAndBlobHash,
                                        byte[] blobHash,
                                        byte[] blobHash2,
                                        byte[] solutionHash,
                                        byte[] seedData) throws DigestException {
        int i;
        blobBuffer.rewind();
        blobBuffer.putInt(minerId);
        blobBuffer.putInt(blob);
        sha256.update(blobBuffer.array());
        sha256.digest(blobHash, 0, 32);

        // step 6 convert blob hash to long
        for (i = 0; i < seedData.length; i++) {
            seedData[i] += blobHash[i * 4];
            seedData[i] += blobHash[i * 4 + 1];
            seedData[i] += blobHash[i * 4 + 2];
            seedData[i] += blobHash[i * 4 + 3];
        }
        seedBuffer.rewind();

        // step 7 compute next(5) with presided random
        random.setSeed(seedBuffer.getLong());
        int algorithm = random.nextInt(5);


        // the reset of the steps
        System.arraycopy(blobHash, 0, blockAndBlobAndBlobHash, 32, 32);
        switch (algorithm) {
            case 1:
                sha1.update(blobBuffer.array());
                sha1.digest(blobHash2, 0, 20);
                System.arraycopy(blobHash2, 0, blockAndBlobAndBlobHash, 64, 20);

                sha256.update(blockAndBlobAndBlobHash, 0, 84);
                break;
            case 2:
                sha384.update(blobBuffer.array());
                sha384.digest(blobHash2, 0, 48);
                System.arraycopy(blobHash2, 0, blockAndBlobAndBlobHash, 64, 48);

                sha256.update(blockAndBlobAndBlobHash, 0, 112);
                break;
            case 3:
                sha512.update(blobBuffer.array());
                sha512.digest(blobHash2, 0, 64);
                System.arraycopy(blobHash2, 0, blockAndBlobAndBlobHash, 64, 64);

                sha256.update(blockAndBlobAndBlobHash, 0, 128);
                break;
            default: // 0 and 4 are MD5
                md5.update(blobBuffer.array());
                md5.digest(blobHash2, 0, 16);
                System.arraycopy(blobHash2, 0, blockAndBlobAndBlobHash, 64, 16);

                sha256.update(blockAndBlobAndBlobHash, 0, 70);
                break;
        }
        sha256.digest(solutionHash, 0, 32);
    }
}
