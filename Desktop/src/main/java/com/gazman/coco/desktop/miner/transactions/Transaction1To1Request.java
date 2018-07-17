package com.gazman.coco.desktop.miner.transactions;

import com.gazman.coco.core.api.ListTypes;
import com.gazman.coco.core.utils.ByteUtils;
import com.gazman.coco.core.utils.MultiByteInteger;
import com.gazman.coco.core.utils.Utils;
import com.gazman.coco.desktop.miner.requests.PoolData;
import com.gazman.coco.desktop.miner.requests.TransactionRequest;

/**
 * Created by Ilya Gazman on 2/22/2018.
 */
public class Transaction1To1Request extends TransactionRequest {

    private byte[] receiverFullId;
    private double amount;
    private double fees;

    public Transaction1To1Request(PoolData poolData) {
        super(poolData, ListTypes.TYPE_1_TO_1);
    }

    public Transaction1To1Request setAmount(double amount) {
        this.amount = amount;
        return this;
    }

    public Transaction1To1Request setReceiverId(byte[] receiverFullId) {
        this.receiverFullId = receiverFullId;
        return this;
    }

    public Transaction1To1Request setFees(double fees) {
        this.fees = fees;
        return this;
    }

    @Override
    protected byte[] onBuildTransactionBody() {
        byte[] amount = ByteUtils.toByteArray(this.amount);
        byte[] feesPerByte = MultiByteInteger.encode(fees > 0 ? this.fees : Utils.DEFAULT_AND_MINIMUM_FEES);

        return ByteUtils.toByteArray(receiverFullId, amount, feesPerByte);
    }
}
