package com.gazman.coco.pool.handlers;


import com.gazman.coco.core.utils.Utils;
import com.gazman.coco.pool.settings.PoolSettings;
import com.sun.net.httpserver.HttpExchange;
import org.bitcoinj.core.Base58;
import org.whispersystems.curve25519.Curve25519;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Ilya Gazman on 1/19/2018.
 */
public abstract class ParamsHandler extends BaseHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HashMap<String, String> params = parseParams(exchange);
        byte[] signature = readSignature(exchange);
        byte[] publicKey = readBase58String(params, "publicKey");
        byte[] message = getNormalizedUrl(exchange).getBytes("UTF-8");

        Curve25519 cipher = Utils.createCipher();
        if (!cipher.verifySignature(publicKey, message, signature)) {
            sendError(exchange, "Bad signature");
        }

        try {
            String error = onHandle(exchange, publicKey, params);
            if (error != null) {
                sendError(exchange, error);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            sendError(exchange, "Internal error");
        }
    }

    protected abstract String onHandle(HttpExchange exchange, byte[] publicKey, HashMap<String, String> params) throws Exception;

    private String getNormalizedUrl(HttpExchange httpExchange) {
        return PoolSettings.POOL_DOMAIN + httpExchange.getRequestURI().getQuery();
    }

    private byte[] readSignature(HttpExchange httpExchange) {
        String signature = httpExchange.getRequestHeaders().getFirst("signature");
        return Base58.decode(signature);
    }

}
