package com.gazman.coco.core.utils;

import java.math.BigInteger;

public class DifficultyUtils {

    public static byte[] getPoolDifficulty(long difficulty) {
        byte[] baseDifficulty = BigInteger.valueOf(2).pow(233).divide(BigInteger.valueOf(difficulty)).toByteArray();
        byte[] desiredDifficulty = new byte[32];
        System.arraycopy(baseDifficulty, 0, desiredDifficulty, desiredDifficulty.length - baseDifficulty.length, baseDifficulty.length);
        return desiredDifficulty;
    }
}
