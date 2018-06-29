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

package com.gazman.coco.core.hash;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256Hash {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static MessageDigest createDigest() {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return digest;
    }

    public static byte[] hash(byte[] data) {
        return createDigest().digest(data);
    }

    public static byte[] hash(byte[] data, int offset, int length) {
        MessageDigest messageDigest = createDigest();
        messageDigest.update(data, offset, length);
        return messageDigest.digest();
    }

    public static byte[] hash(String data) {
        return hash(data.getBytes(UTF_8));
    }
}
