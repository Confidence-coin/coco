package com.gazman.coco.desktop.miner;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class MinerTest {

    @Test
    public void testSolution() {
        Random random = new Random();
        Miner miner = new Miner(null, 0, null);
        for (int i = 0; i < 1_000_000; i++) {
            byte[] a = new byte[32];
            byte[] b = new byte[32];
            random.nextBytes(a);
            random.nextBytes(b);

            BigInteger aValue = new BigInteger(1, a);
            BigInteger bValue = new BigInteger(1, b);

            boolean minerTest = miner.testSolution(a, b);
            boolean bigIntegerTest = bValue.compareTo(aValue) < 0;
            if(bigIntegerTest != minerTest){
                minerTest = miner.testSolution(a, b);
            }

            assertEquals(minerTest, bigIntegerTest);
        }
    }
}