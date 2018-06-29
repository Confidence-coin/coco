package org.bitcoinj.core;

import com.gazman.coco.core.utils.ByteUtils;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * Created by Ilya Gazman on 2/26/2018.
 */
public class Base58Test {

    @Test
    public void encodeWithCheckSum() throws Exception {
        Random random = new Random();
        for (int i = 1; i < 100; i++) {
            byte[] bytes = new byte[i];
            random.nextBytes(bytes);
            String data = Base58.encodeWithCheckSum(bytes);
            assertTrue(ByteUtils.equals(bytes, Base58.decode(data)));
        }
    }

    @Test
    public void encode() throws Exception {
        Random random = new Random();
        for (int i = 1; i < 100; i++) {
            byte[] bytes = new byte[i];
            random.nextBytes(bytes);
            String data = Base58.encode(bytes);
            byte[] decode = Base58.decode(data);
            assertTrue(ByteUtils.equals(bytes, decode));
        }
    }

}