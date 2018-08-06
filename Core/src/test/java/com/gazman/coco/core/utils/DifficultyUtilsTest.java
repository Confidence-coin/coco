package com.gazman.coco.core.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class DifficultyUtilsTest {

    @Test
    public void getPoolDifficulty() {


        byte[] poolDifficulty = DifficultyUtils.getPoolDifficulty(Long.MAX_VALUE);
        System.out.println(ByteUtils.toByteString(poolDifficulty));

        poolDifficulty = DifficultyUtils.getPoolDifficulty(1);
        System.out.println(ByteUtils.toByteString(poolDifficulty));
    }
}