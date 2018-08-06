package com.gazman.coco.core.utils;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ilya Gazman on 1/29/2018.
 */
public class ByteUtilsTest {
    @Test
    public void equals() {
        byte[] bytes = new byte[100];
        new Random().nextBytes(bytes);
        assertTrue(ByteUtils.equals(bytes, bytes.clone()));
    }

    @Test
    public void bigIntegerTest() {
        BigInteger value = new BigInteger("10547363686064609352314090266141472461989948074031");
        while (!value.equals(BigInteger.ZERO)) {
            byte[] bytes = ByteUtils.toByteArray(value, 32);
            assertEquals(value, ByteUtils.toBigInteger(bytes));
            value = value.divide(BigInteger.TEN);
        }
    }

    @Test(expected = Error.class)
    public void bigIntegerTestNegativeValue() {
        BigInteger value = new BigInteger("-10547363686064609352314090266141472461989948074031");
        ByteUtils.toByteArray(value);
    }

    @Test
    public void compare() {
        byte[] left = new byte[]{1, 2, 4};
        byte[] right = new byte[]{1, 2, 3};
        assertEquals(ByteUtils.compare(left, right), 1);

        left = new byte[]{0, 1, 2, 3};
        right = new byte[]{1, 2, 3};
        assertEquals(ByteUtils.compare(left, right), 1);

        left = new byte[]{1, 2, 3};
        right = new byte[]{1, 2, 4};
        assertEquals(ByteUtils.compare(left, right), -1);

        left = new byte[]{1, 2, 3};
        right = new byte[]{1, 2, 3};
        assertEquals(ByteUtils.compare(left, right), 0);

        left = new byte[]{1, 2, 3, 0};
        right = new byte[]{1, 2, 3, 0, 0};
        assertEquals(ByteUtils.compare(left, right), 0);
    }

    @Test
    public void toByteStringSigned() {
        byte[] bytes = new byte[]{-1, (byte) 254, 5};
        assertEquals("-1-25", ByteUtils.toByteString(bytes, true).replaceAll(" ", ""));
    }

    @Test
    public void toByteStringUnsigned() {
        byte[] bytes = new byte[]{-1, (byte) 254, 5};
        assertEquals("2552545", ByteUtils.toByteString(bytes, false).replaceAll(" ", ""));
    }

    @Test
    public void verifyUUIDBytesCanBeReconstructedBackToOriginalUUID() {
        UUID uuid1 = UUID.randomUUID();
        byte[] uBytes = ByteUtils.toByteArray(uuid1);
        UUID uuid2 = ByteUtils.toUUID(uBytes);
        assertEquals(uuid1, uuid2);
    }

    @Test
    public void verifyNameUUIDFromBytesMethodDoesNotRecreateOriginalUUID() {
        UUID uuid1 = UUID.randomUUID();
        byte[] uBytes = ByteUtils.toByteArray(uuid1);
        UUID uuid2 = UUID.nameUUIDFromBytes(uBytes);
        assertNotEquals(uuid1, uuid2);
    }
}