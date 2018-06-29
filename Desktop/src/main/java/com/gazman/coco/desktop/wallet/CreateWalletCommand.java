package com.gazman.coco.desktop.wallet;

import com.gazman.coco.core.utils.Utils;
import com.gazman.lifecycle.utils.Command;
import org.bitcoinj.core.Base58;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

/**
 * Created by Ilya Gazman on 1/16/2018.
 */
public class CreateWalletCommand implements Command {
    @Override
    public void execute() {
        Curve25519 cipher = Utils.createCipher();
        Curve25519KeyPair keyPair = cipher.generateKeyPair();
        String key = Base58.encode(keyPair.getPrivateKey());
        byte[] restoredKey = Base58.decode(key.replace("9", "8"));
        System.out.println(key);
        System.out.println(Base58.encode(keyPair.getPublicKey()));


    }
}
