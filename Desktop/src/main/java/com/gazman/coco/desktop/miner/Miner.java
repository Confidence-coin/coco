package com.gazman.coco.desktop.miner;

import com.gazman.coco.core.api.WorkData;

import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Ilya Gazman on 1/20/2018.
 */
public class Miner {

    private static final boolean DEBUG = false;
    private byte[] bestSolution;
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
        int i, loops = 0;

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
            loops++;
            if (loops > speed) {
                if (adjustments > 0) {
                    adjustments--;
                    long deltaTime = System.currentTimeMillis() - blockTime;
                    speed = (int) (speed / (deltaTime / 1000D));
                }
                long currentTime = System.currentTimeMillis();
                callback.onProgress(new LoopData(loops, currentTime - startingTime));

//                System.out.println(Thread.currentThread().getName() + " Loop interval: " +
//                        (currentTime - startingTime) / 1000F + " Speed: " + (loops * 1000 / (currentTime - startingTime)));
                startingTime = currentTime;

                loops = 0;

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
            blobBuffer.putInt(loops);
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

            if(DEBUG){
                if(bestSolution == null){
                    bestSolution = solutionHash.clone();
                }
                else if(compare(bestSolution, solutionHash)){
                    bestSolution = solutionHash.clone();
                    printSolution(solutionHash, loops, blockTime);
                    printSolution(difficulty, loops, blockTime);
                }
                continue;
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
                            printSolution(solutionHash, loops, blockTime);
                            callback.onSolutionFound(loops, blockTime);
                        }
                    } else {
                        break;
                    }
                } else {
                    if (a < 0) {
                        printSolution(solutionHash, loops, blockTime);
                        callback.onSolutionFound(loops, blockTime);
                    } else {
                        if (b > a) {
                            break;
                        } else {
                            printSolution(solutionHash, loops, blockTime);
                            callback.onSolutionFound(loops, blockTime);
                        }
                    }
                }
            }
        }
    }

    private boolean compare(byte[] difficulty, byte[] solutionHash ){
        for (int i = 0; i < 32; i++) {
            byte a = difficulty[i];
            byte b = solutionHash[i];

            if (a == b) {
                continue;
            }
            if (b < 0) {
                if (a < 0) {
                    return b >= a;
                } else {
                    break;
                }
            } else {
                if (a < 0) {
                    return true;
                } else {
                    return b <= a;
                }
            }
        }
        return false;
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
