package com.gazman.coco.core.utils;

import com.gazman.lifecycle.Singleton;
import com.google.gson.*;
import org.bitcoinj.core.Base58;

import java.lang.reflect.Type;

/**
 * Created by Ilya Gazman on 2/25/2018.
 */
public class GsonHelper implements Singleton {

    public final Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class,
            new ByteArrayToBase64TypeAdapter()).create();

    private class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base58.decode(json.getAsString());
        }

        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base58.encode(src));
        }
    }
}
