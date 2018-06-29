package com.gazman.coco.pool.transactions;

import org.whispersystems.curve25519.Curve25519;

/**
 * Created by Ilya Gazman on 2/10/2018.
 */
public class TransactionsValidator {
    private byte[] signature = new byte[64];

    public boolean validate1To1(Curve25519 cipher, byte[] publicKey, byte[] transaction) {
        byte[] message = new byte[transaction.length - 64];
        System.arraycopy(transaction, 0, signature, 0, 64);
        System.arraycopy(transaction, 64, message, 0, message.length);

        if (!cipher.verifySignature(publicKey, message, signature)) {
            return false;
        }


        return true;
    }

}
