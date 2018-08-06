package com.gazman.coco.desktop.miner;

import com.gazman.coco.core.api.WorkData;
import com.gazman.coco.core.utils.ByteUtils;
import com.gazman.coco.core.utils.MiningUtils;

import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Created by Ilya Gazman on 1/20/2018.
 */
class Miner {

    private static final double ADJUSTMENTS_TIME = 1000D;
    private WorkData workData;
    private int minerId;
    private MinerCallback callback;
    private boolean active;

    Miner(WorkData workData, int minerId, MinerCallback callback) {
        this.workData = workData;
        this.minerId = minerId;
        this.callback = callback;
    }

    void start() {
        active = true;
        try {
            mineNow();
        } catch (NoSuchAlgorithmException | DigestException e) {
            e.printStackTrace();
        }
    }

    private void mineNow() throws NoSuchAlgorithmException, DigestException {
        int minerId = this.minerId;
        byte[] step3Hash = workData.step3Hash;
        byte[] difficulty = workData.difficulty;
        Random random = new Random();
        int blob = 0;

        final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        final MessageDigest md5 = MessageDigest.getInstance("MD5");
        final MessageDigest sha384 = MessageDigest.getInstance("SHA-384");
        final MessageDigest sha512 = MessageDigest.getInstance("SHA-512");

        if (step3Hash == null) {
            throw new Error("bad hash");
        }
        byte[] blockAndBlobAndBlobHash = new byte[128];
        byte[] blobHash = new byte[32];
        byte[] blobHash2 = new byte[64];
        byte[] solutionHash = new byte[32];

        ByteBuffer blobBuffer = ByteBuffer.allocate(8);
        long startingTime = System.currentTimeMillis();

        ByteBuffer timeBuffer = ByteBuffer.allocate(40);

        //updateTime
        long blockTime = System.currentTimeMillis();
        timeBuffer.putLong(blockTime);
        timeBuffer.put(step3Hash);
        byte[] timeHash = sha256.digest(timeBuffer.array());
        System.arraycopy(timeHash, 0, blockAndBlobAndBlobHash, 0, 32);
        int speed = 1_000_000;
        int adjustments = 5;

        byte[] seedData = new byte[8];
        ByteBuffer seedBuffer = ByteBuffer.wrap(seedData);

        while (active) {
            blob++;
            if (blob > speed) {
                TimeData timeData = new TimeData(step3Hash, blob, sha256, blockAndBlobAndBlobHash,
                        startingTime, timeBuffer, blockTime, speed, adjustments).invoke();
                blob = timeData.blob;
                startingTime = timeData.startingTime;
                blockTime = timeData.blockTime;
                speed = timeData.speed;
                adjustments = timeData.adjustments;
            }

            MiningUtils.computeBlockHash(minerId, blob, blobBuffer,
                    seedBuffer, random, sha256, sha1, md5, sha384, sha512, blockAndBlobAndBlobHash, blobHash, blobHash2, solutionHash, seedData);

            if (testSolution(difficulty, solutionHash)) {
                printSolution(solutionHash, blob, blockTime);
                submitSolution(blobBuffer, blockTime);
            }
        }
    }

    boolean testSolution(byte[] difficulty, byte[] solutionHash) {
        int i;
        for (i = 0; i < 32; i++) {
            byte a = difficulty[i];
            byte b = solutionHash[i];

            if (a == b) {
                continue;
            }
            return b < 0 ? a < 0 && b < a : a < 0 || b <= a;
        }
        return false;
    }

    private void submitSolution(ByteBuffer loops, long blockTime) {
        loops.rewind();
        callback.onSolutionFound(loops.getLong(), blockTime);
    }

    private void printSolution(byte[] solutionHash, long blob, long blockTime) {
        System.out.print(Thread.currentThread().getName());
        System.out.print(blob + " " + blockTime + " -> ");
        System.out.println(ByteUtils.toByteString(solutionHash));
    }

    void shutDown() {
        active = false;
    }

    private class TimeData {
        byte[] step3Hash;
        int blob;
        MessageDigest sha256;
        byte[] blockAndBlobAndBlobHash;
        long startingTime;
        ByteBuffer timeBuffer;
        long blockTime;
        int speed;
        int adjustments;

        TimeData(byte[] step3Hash, int blob, MessageDigest sha256, byte[] blockAndBlobAndBlobHash, long startingTime, ByteBuffer timeBuffer, long blockTime, int speed, int adjustments) {
            this.step3Hash = step3Hash;
            this.blob = blob;
            this.sha256 = sha256;
            this.blockAndBlobAndBlobHash = blockAndBlobAndBlobHash;
            this.startingTime = startingTime;
            this.timeBuffer = timeBuffer;
            this.blockTime = blockTime;
            this.speed = speed;
            this.adjustments = adjustments;
        }

        TimeData invoke() {
            byte[] timeHash;
            if (adjustments > 0) {
                adjustments--;
                long deltaTime = System.currentTimeMillis() - blockTime;
                speed = (int) (speed / (deltaTime / ADJUSTMENTS_TIME));
            }
            long currentTime = System.currentTimeMillis();
            callback.onProgress(new LoopData(blob, currentTime - startingTime));

//                System.out.println(Thread.currentThread().getName() + " Loop interval: " +
//                        (currentTime - startingTime) / 1000F + " Speed: " + (blob * 1000 / (currentTime - startingTime)));
            startingTime = currentTime;

            blob = 0;

            //Step 4 updateTime
            blockTime = System.currentTimeMillis();
            timeBuffer.rewind();
            timeBuffer.putLong(blockTime);
            timeBuffer.put(step3Hash);
            timeHash = sha256.digest(timeBuffer.array());
            System.arraycopy(timeHash, 0, blockAndBlobAndBlobHash, 0, 32);
            return this;
        }
    }
}
