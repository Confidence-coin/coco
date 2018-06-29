package com.gazman.coco.pool.handlers;

import com.gazman.coco.core.api.ErrorData;
import com.gazman.coco.core.hash.Sha256Hash;
import com.gazman.coco.core.utils.GsonHelper;
import com.gazman.coco.core.utils.Utils;
import com.gazman.coco.pool.settings.PoolSettings;
import com.gazman.lifecycle.Factory;
import com.sun.istack.internal.Nullable;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bitcoinj.core.Base58;
import org.whispersystems.curve25519.Curve25519;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;

/**
 * Created by Ilya Gazman on 2/13/2018.
 */
public abstract class BaseHandler implements HttpHandler {

    private GsonHelper gsonHelper = Factory.inject(GsonHelper.class);

    protected @Nullable
    HashMap<String, String> parseParams(HttpExchange exchange) {
        URI requestURI = exchange.getRequestURI();
        if (requestURI == null) {
            return null;
        }
        String query = requestURI.getQuery();
        if (query == null) {
            return null;
        }

        HashMap<String, String> params = new HashMap<>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length > 1) {
                params.put(pair[0].toLowerCase(), pair[1]);
            }
        }
        return params;
    }

    protected long readLong(HashMap<String, String> params, String key, long defaultValue) {
        String value = readString(params, key);
        try {
            if (value != null) {
                return Long.parseLong(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    protected int readInt(HashMap<String, String> params, String key, int defaultValue) {
        String value = readString(params, key);
        try {
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    protected String readString(HashMap<String, String> params, String key) {
        return params.get(key.toLowerCase());
    }

    protected byte[] readBase58String(HashMap<String, String> params, String key) {
        String value = readString(params, key);
        try {
            if (value != null) {
                return Base58.decode(value);
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    protected void sendError(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, new ErrorData(message, 400), 400);
    }

    protected void sendResponse(HttpExchange exchange, Object dataObject, int code) throws IOException {
        String messageData = gsonHelper.gson.toJson(dataObject);
        byte[] data = messageData.getBytes(Sha256Hash.UTF_8);
        Curve25519 cipher = Utils.createCipher();
        byte[] signatureData = cipher.calculateSignature(PoolSettings.SECRET_KEY, data);
        String signature = Base58.encode(signatureData);

        exchange.setAttribute("ContentType", "application/json");
        exchange.setAttribute("Signature", signature);
        exchange.sendResponseHeaders(code, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }
}
