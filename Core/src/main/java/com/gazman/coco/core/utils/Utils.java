/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gazman.coco.core.utils;

import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.JCESecureRandomProvider;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.UUID;

/**
 * Created by Ilya Gazman on 2/9/2018.
 */
public class Utils {


    public static final double COCO_BRONZE = 1D / 100_000_000;
    public static final double COCO_SILVER = COCO_BRONZE * 10_000;
    public static final double COCO_GOLD = COCO_SILVER * 10_000;

    public static double DEFAULT_AND_MINIMUM_FEES = 199 * COCO_BRONZE; // about 50 COCO_GOLD in one block of 16 MB

    public static byte[] uuidToByteArray(String uuidData) {
        UUID uuid = UUID.fromString(uuidData);
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    public static Curve25519 createCipher() {
        return Curve25519.getInstance(Curve25519.BEST, new JCESecureRandomProvider());
    }

    public static String convertStreamToString(InputStream inputStream) {
        try (Scanner scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A")) {
            return scanner.next();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

}
