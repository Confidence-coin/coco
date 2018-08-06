package com.gazman.coco.pool.settings;

import com.gazman.coco.core.settings.BaseSettings;
import com.gazman.coco.core.utils.Utils;
import com.gazman.lifecycle.Factory;
import org.bitcoinj.core.Base58;
import org.whispersystems.curve25519.Curve25519KeyPair;

import java.util.UUID;

/**
 * Created by Ilya Gazman on 1/24/2018.
 */
public final class PoolSettings {

    /**
     * How many times the pool difficulty is easier then block difficulty
     */
    public static final int POOL_DIFFICULTY_MULTIPLIER;
    /**
     * Secret number used to generate workers ids
     */
    public static final byte[] POOL_SECRET;
    /**
     * Max aloud time error when submitting share
     */
    public static final long SHARE_TIME_ERROR;

    /**
     * Wallet id where the reward goes to
     */
    public static final int POOL_MINER_ID;
    /**
     * Pool wallet used for encrypting
     */
    public static final byte[] SECRET_KEY;

    public static final String PUBLIC_KEY;
    public static final String POOL_DOMAIN;

    static {
        BaseSettings settings = Factory.inject(BaseSettings.class);
        settings.load("pool.txt");
        settings.setSaveDefaults(true);

        POOL_DIFFICULTY_MULTIPLIER = settings.readInteger("POOL_DIFFICULTY_MULTIPLIER", 1);

        POOL_SECRET = Utils.uuidToByteArray(settings.readString("POOL_SECRET", UUID.randomUUID()::toString));
        SHARE_TIME_ERROR = settings.readInteger("SHARE_TIME_ERROR", 1000 * 60 * 10);
        POOL_MINER_ID = settings.readInteger("POOL_MINER_ID", 1);
        POOL_DOMAIN = settings.readString("POOL_DOMAIN", "127.0.0.1");
        SECRET_KEY = Base58.decode(settings.readString("SECRET_KEY",
                () -> {
                    Curve25519KeyPair keyPair = Utils.createCipher().generateKeyPair();
                    settings.writeKey("PUBLIC_KEY", Base58.encodeWithCheckSum(keyPair.getPublicKey()));
                    return Base58.encodeWithCheckSum(keyPair.getPrivateKey());
                }
        ));
        PUBLIC_KEY = settings.readStringOrThrow("PUBLIC_KEY");
        settings.saveDefaults("Generated default values");
    }
}
