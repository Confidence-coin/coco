package com.gazman.coco.pool.handlers.transaction;

import com.gazman.coco.core.api.ListTypes;
import com.gazman.coco.core.utils.ByteUtils;
import com.gazman.coco.core.utils.MultiByteInteger;
import com.gazman.coco.core.utils.Utils;
import com.gazman.coco.db.DB;
import com.gazman.coco.db.InsertCommand;
import com.gazman.lifecycle.Factory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Ilya Gazman on 2/14/2018.
 */
public class Transaction1To1Handler implements RootTransactionHandler.TransactionHandler {

    private TransactionsHelper helper = Factory.inject(TransactionsHelper.class);

    @Override
    public String handle(int type, byte[] senderFullId, byte[] signature, int blockId, ByteBuffer buffer, DB db)
            throws IOException {
        byte[] receiverFullId = new byte[32];
        buffer.get(receiverFullId);

        double amount = buffer.getDouble();
        if (amount < 0) {
            return "Negative amount";
        }

        double bronzesAmount = amount / Utils.COCO_BRONZE;
        if (bronzesAmount - (long) bronzesAmount != 0) {
            return "Out of range amount";
        }

        int fees = MultiByteInteger.parse(buffer);
        double feesPerByte = fees * Utils.COCO_BRONZE;
        if (feesPerByte < Utils.DEFAULT_AND_MINIMUM_FEES) {
            return "Fees below minimum " + feesPerByte + " < " + Utils.DEFAULT_AND_MINIMUM_FEES;
        }

        byte features = applyFeatures(amount, feesPerByte);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(signature);

            TransactionsHelper.Wallet[] wallets = helper.fetchWallets(db, senderFullId, receiverFullId);
            if (wallets == null || wallets.length == 0) {
                return "Sender id not found";
            }
            boolean senderIdAtIndex0 = ByteUtils.equals(wallets[0].publicKey, senderFullId);
            if (wallets.length == 1) {
                if (!senderIdAtIndex0) {
                    return "Sender id not found";
                }
            }
            int senderId, receiverId = -1;
            if (senderIdAtIndex0) {
                senderId = wallets[0].id;
                if (wallets.length > 1) {
                    receiverId = wallets[1].id;
                }
            } else if (ByteUtils.equals(wallets[1].publicKey, senderFullId)) {
                senderId = wallets[1].id;
                receiverId = wallets[0].id;
            } else {
                return "Internal error sender id not found";
            }
            if (!MultiByteInteger.isValid(senderId) || senderId == 0) {
                return "internal error in sender id " + senderId;
            }
            if (wallets.length > 1 && ByteUtils.equals(wallets[0].publicKey, wallets[1].publicKey)) {
                return "Error, can't make transaction to your self";
            }

            outputStream.write(MultiByteInteger.encode(senderId));
            if (receiverId != -1) {
                if (!MultiByteInteger.isValid(receiverId) || receiverId == 0) {
                    return "internal error in receiver id " + senderId;
                }
                outputStream.write(MultiByteInteger.encode(receiverId));
            } else {
                outputStream.write(MultiByteInteger.encode(0));
            }

            double balance = helper.fetchBalance(db, senderId);

            if (balance < 0) {
                return "Internal error fetching balance";
            }

            double totalFees = calculateFees(amount, features, senderId, receiverId, feesPerByte);
            if (balance < amount + totalFees) {
                return "Insufficient balance";
            }

            if (ListTypes.isFeatureOn(features, ListTypes.FEATURE_SMBI_AMOUNT)) {
                outputStream.write(MultiByteInteger.encode(amount));
            } else {
                ByteBuffer doubleBuffer = ByteBuffer.allocate(8);
                doubleBuffer.putDouble(amount);
                outputStream.write(doubleBuffer.array());
            }
            if (!ListTypes.isFeatureOn(features, ListTypes.FEATURE_DEFAULT_FEES)) {
                outputStream.write(MultiByteInteger.encode(feesPerByte));
            }

            InsertCommand insertCommand = db.insert("pool.lists")
                    .add("block_id", blockId)
                    .add("features", features & 0xff)
                    .add("type", type)
                    .add("item", outputStream.toByteArray());
            if (receiverId == -1) {
                insertCommand.add("receivers", receiverFullId);
            }
            insertCommand.execute();
        }

        return null;
    }

    private double calculateFees(double amount, byte features, int senderId, int receiverId, double feesPerByte) {
        int totalBytes = 0;
        totalBytes += TransactionsHelper.SIGNATURE_SIZE;
        totalBytes += MultiByteInteger.calculateByteLength(senderId);
        if (receiverId > 0) {
            totalBytes += MultiByteInteger.calculateByteLength(receiverId);
        } else {
            totalBytes += MultiByteInteger.MAX_BYTE_SIZE + TransactionsHelper.KEY_SIZE;
        }

        if (ListTypes.isFeatureOn(features, ListTypes.FEATURE_SMBI_AMOUNT)) {
            totalBytes += MultiByteInteger.calculateByteLength(amount);
        }

        if (!ListTypes.isFeatureOn(features, ListTypes.FEATURE_DEFAULT_FEES)) {
            totalBytes += MultiByteInteger.calculateByteLength(feesPerByte);
        }

        return totalBytes * feesPerByte;
    }

    private byte applyFeatures(double amount, double feesPerByte) {
        byte features = 0;
        if (feesPerByte == Utils.DEFAULT_AND_MINIMUM_FEES) {
            features |= ListTypes.FEATURE_DEFAULT_FEES;
        }
        if (amount == 0) {
            features |= ListTypes.FEATURE_ALL_AMOUNT;
        }
        if (amount < MultiByteInteger.MAX_SIZE * Utils.COCO_BRONZE) {
            features |= ListTypes.FEATURE_SMBI_AMOUNT;
        }
        return features;
    }


}
