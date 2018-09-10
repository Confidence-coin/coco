package com.gazman.coco.desktop.wallet;

import com.gazman.coco.core.settings.BaseSettings;
import com.gazman.coco.core.utils.Utils;
import com.gazman.lifecycle.Factory;
import com.gazman.lifecycle.Singleton;
import org.bitcoinj.core.Base58;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by Ilya Gazman on 1/16/2018.
 */
public class WalletModel implements Singleton {



    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private final BaseSettings settingsFile = Factory.inject(BaseSettings.class);
    private byte[] ssk;
    private byte[] publicKey;
    public ArrayList<String> wallets= new ArrayList<>();

    public void init()  {
        wallets.add("sdsd");
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

    private byte[] signToBytes(String data) {
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



    public int getFees() {
        return settingsFile.readInteger("fees", 50000);
    }
//Still working on this code part for the name and stuff//
public String getname() {

        return "";

    }

    public int walletSize() {
        return wallets.size();
    }
    public void CreateWallet(){
        wallets.add(getname());

    }

    public double getBalance() {
        return 0;
    }


}
