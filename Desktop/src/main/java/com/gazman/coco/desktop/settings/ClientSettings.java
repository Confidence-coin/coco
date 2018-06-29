package com.gazman.coco.desktop.settings;

import com.gazman.coco.core.settings.BaseSettings;
import com.gazman.coco.desktop.miner.requests.PoolData;
import com.gazman.lifecycle.Factory;
import org.bitcoinj.core.Base58;

/**
 * Created by Ilya Gazman on 1/27/2018.
 */
public enum ClientSettings {
    instance;
    public String poolWebsite;
    public String poolPublicKey;
    public int poolPort;
    public PoolData defaultPoolData;

    public void init()
    {
        BaseSettings settings = Factory.inject(BaseSettings.class);
        settings.load("client.txt");
        settings.setSaveDefaults(true);

        poolWebsite = settings.readString("POOL_WEBSITE", "127.0.0.1");
        poolPublicKey = settings.readString("POOL_PUBLIC_KEY", "2BJNAY@28LmNk8xbJHS1jePMQzrfiosiip9kXvNMKyFMRMk6RAMg");
        poolPort = settings.readInteger("POOL_PORT", 8081);
        settings.saveDefaults("Generated default values");

        defaultPoolData = new PoolData();
        defaultPoolData.publicKey = Base58.decode(poolPublicKey);
        defaultPoolData.port = poolPort;
        defaultPoolData.host = poolWebsite;
    }
}

