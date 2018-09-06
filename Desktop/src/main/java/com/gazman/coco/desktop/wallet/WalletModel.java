package com.gazman.coco.desktop.wallet;

import com.gazman.coco.core.settings.BaseSettings;
import com.gazman.coco.core.utils.Utils;
import com.gazman.lifecycle.Factory;
import com.gazman.lifecycle.Singleton;
import org.bitcoinj.core.Base58;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ilya Gazman on 1/16/2018.
 */
public class WalletModel implements Singleton {


    String name;
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private final BaseSettings settingsFile = Factory.inject(BaseSettings.class);
    public byte[] ssk;
    private byte[] publicKey;
    public Map<byte[], String> Wallets = Factory.inject(HashMap.class);

    public void init() {
        settingsFile.load("wallet.txt");
        String sskData = settingsFile.readString("ssk", (String) null);
        if (sskData != null) {
            ssk = Base58.decode(sskData);
        }
        String publicKeyData = settingsFile.readString("publicKey", (String) null);
        if (publicKeyData != null) {
            publicKey = Base58.decode(publicKeyData);
        }
    }

    public String signToString(String data) {
        byte[] signature = signToBytes(data);
        return Base58.encode(signature);
    }

    public byte[] signToBytes(String data) {
        return signToBytes(data.getBytes(UTF_8));
    }

    public byte[] signToBytes(byte[] data) {
        Curve25519 cipher = Utils.createCipher();
        return cipher.calculateSignature(ssk, data);
    }

    public String getPublicKey() {
        return Base58.encodeWithCheckSum((publicKey));
    }

    public byte[] getPublicKeyBytes() {
        return publicKey;
    }

    public String getPrivateKey() {
        if (ssk == null) {
            return null;
        }
        return Base58.encodeWithCheckSum(ssk);
    }

    public void generateKey() {
        Curve25519 cipher = Utils.createCipher();
        Curve25519KeyPair keyPair = cipher.generateKeyPair();
        ssk = keyPair.getPrivateKey();
        publicKey = keyPair.getPublicKey();
        settingsFile.writeKey("ssk", getPrivateKey());
        settingsFile.writeKey("publicKey", getPublicKey());
        settingsFile.save("Key was generated");
    }

    public void generateKey(byte[] ssk) {
        Curve25519 cipher = Utils.createCipher();
        Curve25519KeyPair keyPair = cipher.generateKeyPair();
        ssk = keyPair.getPrivateKey();
        publicKey = keyPair.getPublicKey();
        settingsFile.writeKey("ssk", getPrivateKey());
        settingsFile.writeKey("publicKey", getPublicKey());
        settingsFile.save("Key was generated");
    }

    public int getFees() {
        return settingsFile.readInteger("fees", 50000);
    }

    public String getname() {
        this.name = name;
        return name;

    }

    public int WalletSize() {
        int size = Wallets.size();
        return size;
    }

    public double getBalance() {
        return 0;
    }
}
