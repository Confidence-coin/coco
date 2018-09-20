package com.gazman.coco.desktop.wallet;

import com.gazman.coco.core.settings.BaseSettings;
import com.gazman.coco.core.utils.Utils;
import com.gazman.coco.desktop.controllers.WalletCellController;
import com.gazman.lifecycle.Factory;
import com.gazman.lifecycle.Singleton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private ArrayList<WalletData> wallets = new ArrayList<>();
    public ObservableList<WalletData> myWallets = FXCollections.observableArrayList();
    private WalletData selectedWallet=Factory.inject(WalletData.class);


    public void init() {
        wallets.add(initializeWalletData());
        settingsFile.load("walletname.txt");
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


    public int walletSize() {
        return wallets.size();
    }

    public void createWallet() {
        wallets.add(initializeWalletData());
        myWallets.add(initializeWalletData());

    }

    public double getBalance() {
        return 0;
    }


    public WalletData initializeWalletData() {
        WalletData data = new WalletData();
        data.setName("Wallet" + walletSize());
        return data;
    }

    public String getSelectedWalletName() {



     return     selectedWallet.getName();
    }


    public class WalletData  {
        String name;


        public String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }
    }


}



