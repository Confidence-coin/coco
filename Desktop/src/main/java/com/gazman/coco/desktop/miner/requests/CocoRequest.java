package com.gazman.coco.desktop.miner.requests;

import com.gazman.coco.core.api.ErrorData;
import com.gazman.coco.core.hash.Sha256Hash;
import com.gazman.coco.core.utils.GsonHelper;
import com.gazman.coco.desktop.wallet.WalletModel;
import com.gazman.lifecycle.Factory;
import com.gazman.lifecycle.utils.Command;
import okhttp3.*;

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ilya Gazman on 1/27/2018.
 */
@SuppressWarnings("unchecked")
public class CocoRequest<DATA, COCO_REQUEST extends CocoRequest<DATA, ?>> implements Command {
    static final OkHttpClient client;

    static {
        client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .build();
    }

    private static final SecureRandom random;

    static {
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    protected final String host;
    protected final int port;
    private final byte[] publicKey;
    private final long randomNumber;
    private WalletModel walletModel =  Factory.inject(WalletModel.class);
    private Class<DATA> responseClass;
    protected String path = "";
    private Callback<DATA> callback;

    public COCO_REQUEST setCallback(Callback<DATA> callback) {
        this.callback = callback;
        return (COCO_REQUEST) this;
    }

    @SuppressWarnings("ConstantConditions")
    protected HttpUrl.Builder httpUrlBuilder = new HttpUrl.Builder()
            .scheme("http");
    private GsonHelper gsonHelper = Factory.inject(GsonHelper.class);

    public static <T, C extends CocoRequest<T, ?>> C create(Class<T> response, PoolData poolData) {
        return (C) new CocoRequest<T, C>(poolData).setResponseClass(response);
    }

    protected CocoRequest(PoolData poolData) {
        if (random == null) {
            throw new Error("Error initializing random");
        }
        this.randomNumber = random.nextLong();
        this.host = poolData.host;
        this.port = poolData.port;
        this.publicKey = poolData.publicKey;
    }

    protected COCO_REQUEST setResponseClass(Class<DATA> responseClass) {
        this.responseClass = responseClass;
        return (COCO_REQUEST) this;
    }

    public COCO_REQUEST setPath(String path) {
        this.path = path;
        return (COCO_REQUEST) this;
    }

    public COCO_REQUEST addQueryParameter(String name, Object value) {
        httpUrlBuilder.addQueryParameter(name, value.toString());
        return (COCO_REQUEST) this;
    }

    public void execute() {
        addQueryParameter("publicKey", walletModel.getPublicKey());

        httpUrlBuilder
                .host(host)
                .port(port)
                .addPathSegments(path);

        HttpUrl url = httpUrlBuilder.build();
        String normalizedUrl = url.host() + url.query();
        String signature = walletModel.signToString(normalizedUrl);

        Request request = new Request.Builder()
                .url(url)
                .header("Signature", signature)
                .build();

        execute(request);
    }

    protected void execute(Request request) {
        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (body == null) {
                sendError("Connection error");
                return;
            }

            byte[] data;
            try (InputStream inputStream = body.byteStream()) {
                int dataSize = (int) body.contentLength();
                data = new byte[dataSize];
                if (inputStream.read(data) != dataSize) {
                    sendError("Connection error");
                }
            }

            String reposeData = new String(data, Sha256Hash.UTF_8);

            if (response.isSuccessful()) {
                callback.onSuccess(gsonHelper.gson.fromJson(reposeData, responseClass));
            } else {
                callback.onError(gsonHelper.gson.fromJson(reposeData, ErrorData.class));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            sendError("Unexpected error");
        }
    }

    private void sendError(String message) {
        ErrorData errorData = new ErrorData(message, -1);
        callback.onError(errorData);
    }

    private String sign() {
        HttpUrl httpUrl = httpUrlBuilder.build();
        String query = httpUrl.query();
        String signature = walletModel.signToString(query);


        System.out.println("Signature " + signature);
        System.out.println("query " + query);
        System.out.println("public key " + walletModel.getPublicKey());

        return signature;
    }


    public interface Callback<T> {
        void onSuccess(T data);

        void onError(ErrorData errorData);
    }
}
