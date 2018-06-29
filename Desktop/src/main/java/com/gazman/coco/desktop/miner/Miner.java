package com.gazman.coco.desktop.miner;

import com.gazman.coco.core.api.WorkData;

import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Created by Ilya Gazman on 1/20/2018.
 */
public class Miner {

    private WorkData workData;
    private int minerId;
    private MinerCallback callback;
    private boolean active;

    public Miner(WorkData workData, int minerId, MinerCallback callback) {
        this.workData = workData;
        this.minerId = minerId;
        this.callback = callback;
    }

    public void start() {
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
        int i, blob = 0;

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
                if (adjustments > 0) {
                    adjustments--;
                    long deltaTime = System.currentTimeMillis() - blockTime;
                    speed = (int) (speed / (deltaTime / 1000D));
                }
                callback.onProgress(blob);
                blob = 0;


                long currentTime = System.currentTimeMillis();
                System.out.println(Thread.currentThread().getName() + " Speed: " + (currentTime - startingTime) / 1000F);
                startingTime = currentTime;

                //Step 4 updateTime
                blockTime = System.currentTimeMillis();
                timeBuffer.rewind();
                timeBuffer.putLong(blockTime);
                timeBuffer.put(step3Hash);
                timeHash = sha256.digest(timeBuffer.array());
                System.arraycopy(timeHash, 0, blockAndBlobAndBlobHash, 0, 32);
            }

            // Step 5 hash the blob
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
                    sha256.digest(solutionHash, 0, 32);
                    break;
                case 2:
                    sha384.update(blobBuffer.array());
                    sha384.digest(blobHash2, 0, 48);
                    System.arraycopy(blobHash2, 0, blockAndBlobAndBlobHash, 64, 48);

                    sha256.update(blockAndBlobAndBlobHash, 0, 112);
                    sha256.digest(solutionHash, 0, 32);
                    break;
                case 3:
                    sha512.update(blobBuffer.array());
                    sha512.digest(blobHash2, 0, 64);
                    System.arraycopy(blobHash2, 0, blockAndBlobAndBlobHash, 64, 64);

                    sha256.update(blockAndBlobAndBlobHash, 0, 128);
                    sha256.digest(solutionHash, 0, 32);
                    break;
                default: // 0 and 4 are MD5
                    md5.update(blobBuffer.array());
                    md5.digest(blobHash2, 0, 16);
                    System.arraycopy(blobHash2, 0, blockAndBlobAndBlobHash, 64, 16);

                    sha256.update(blockAndBlobAndBlobHash, 0, 70);
                    sha256.digest(solutionHash, 0, 32);
                    break;
            }

            for (i = 0; i < 32; i++) {
                byte a = difficulty[i];
                byte b = solutionHash[i];

                if (a == b) {
                    continue;
                }
                if (b < 0) {
                    if (a < 0) {
                        if (b < a) {
                            break;
                        } else {
                            callback.onSolutionFound(blob, blockTime);
                            printSolution(solutionHash, blob, blockTime);
                        }
                    } else {
                        break;
                    }
                } else {
                    if (a < 0) {
                        callback.onSolutionFound(blob, blockTime);
                        printSolution(solutionHash, blob, blockTime);
                    } else {
                        if (b > a) {
                            break;
                        } else {
                            callback.onSolutionFound(blob, blockTime);
                            printSolution(solutionHash, blob, blockTime);
                        }
                    }
                }
            }
        }
    }

    private void printSolution(byte[] solutionHash, long blob, long blockTime) {
        boolean first = true;
        System.out.print(Thread.currentThread().getName());
        System.out.print(": ");
        for (byte b : solutionHash) {
            if (!first) {
                System.out.print(", ");
            } else {
                first = false;
            }
            StringBuilder out = new StringBuilder("" + (b & 0xff));
            while (out.length() < 3) {
                out.append(" ");
            }
            System.out.print(out);
        }
        System.out.println(" " + blob + " " + blockTime);
    }

    public void shutDown() {
        active = false;
    }
}
