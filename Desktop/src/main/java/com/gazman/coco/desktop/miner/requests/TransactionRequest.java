package com.gazman.coco.desktop.miner.requests;

import com.gazman.coco.core.utils.ByteUtils;
import com.gazman.coco.core.utils.MultiByteInteger;
import com.gazman.coco.desktop.wallet.WalletModel;
import com.gazman.lifecycle.Factory;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Ilya Gazman on 2/22/2018.
 */
public abstract class TransactionRequest extends CocoRequest<String, TransactionRequest> {

    public static final MediaType BINARY_DATA = MediaType.parse("application/octet-stream");
    private WalletModel walletModel =  Factory.inject(WalletModel.class);
    private int type;
    private byte[] blockHash;

    protected TransactionRequest(PoolData poolData, int type, byte[] blockHash) {
        super(poolData);
        this.type = type;
        this.blockHash = blockHash;
        setResponseClass(String.class);
    }


    @Override
    public void execute() {
        httpUrlBuilder
                .host(host)
                .port(port)
                .addPathSegments(path);

        byte[] data = buildTransaction();
        RequestBody body = RequestBody.create(BINARY_DATA, data);
        Request request = new Request.Builder()
                .url(httpUrlBuilder.build())
                .post(body)
                .build();

        execute(request);
    }

    private byte[] buildTransaction() {
        byte[] body = buildTransactionBody();
        byte[] length = MultiByteInteger.encode(body.length);
        byte[] signature = walletModel.signToBytes(body);

        return ByteUtils.toByteArray(signature, length, body);
    }

    private byte[] buildTransactionBody() {
        byte[] publicKey = walletModel.getPublicKeyBytes();
        byte[] type = MultiByteInteger.encode(this.type);

        return ByteUtils.toByteArray(publicKey, type, blockHash, onBuildTransactionBody());
    }

    protected abstract byte[] onBuildTransactionBody();
}
