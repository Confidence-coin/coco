package com.gazman.coco.core.utils;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ilya Gazman on 1/27/2018.
 */
public class MultiByteIntegerTest {
    @Test
    public void generalTest() throws Exception {

        for (int i = 0; i < 257; i++) {
            testToFromByteArray(i);
        }

        testValueLength(1, 1);
        testValueLength(60, 1);
        testValueLength(64, 2);
        testValueLength(256, 2);
        testValueLength(16383, 2);
        testValueLength(16384, 3);
        testValueLength(4194304, 4);
        testValueLength(1073741823, 4);
    }

    @Test(expected = Error.class)
    public void testMinValue() {
        MultiByteInteger.encode(MultiByteInteger.MIN_SIZE - 1);
    }

    @Test(expected = Error.class)
    public void testMaxValue() {
        MultiByteInteger.encode(MultiByteInteger.MAX_SIZE + 1);
    }

    private void testToFromByteArray(int value) throws IOException {
        byte[] bytes = MultiByteInteger.encode(value);
        assertEquals(MultiByteInteger.parse(bytes), value);
        assertEquals(MultiByteInteger.parse(new ByteArrayInputStream(bytes)), value);
        assertEquals(MultiByteInteger.parse(ByteBuffer.wrap(bytes)), value);

        bytes = MultiByteInteger.encode(value * Utils.COCO_BRONZE);
        assertEquals(MultiByteInteger.parse(bytes), value);
        assertEquals(MultiByteInteger.parse(new ByteArrayInputStream(bytes)), value);
        assertEquals(MultiByteInteger.parse(ByteBuffer.wrap(bytes)), value);
    }

    private void testValueLength(int value, int length) throws IOException {
        assertEquals(MultiByteInteger.encode(value).length, length);
        testToFromByteArray(value);
    }

}