package com.gazman.coco.desktop.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Ilya Gazman on 7/16/2018.
 */
public class CoinUtilsTest {

    @Test
    public void toCoinsString() {
        assertEquals("10 Gold", CoinUtils.toCoinsString(10));
        assertEquals("10,000 Gold", CoinUtils.toCoinsString(10000));
        assertEquals("10 Gold and 1,000 Silver", CoinUtils.toCoinsString(10.1));
        assertEquals("1,000 Silver", CoinUtils.toCoinsString(0.1));
        assertEquals("1,000 Silver and 12 Bronze", CoinUtils.toCoinsString(0.10000012));
        assertEquals("1,000,000 Gold, 1,000 Silver and 12 Bronze", CoinUtils.toCoinsString(1_000_000.10000012));
        assertEquals("0", CoinUtils.toCoinsString(0));
    }
}