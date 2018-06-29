package com.gazman.coco.desktop.miner;

import com.gazman.coco.core.api.WorkData;
import com.gazman.coco.desktop.miner.requests.CocoRequest;
import com.gazman.coco.desktop.miner.requests.PoolData;
import com.gazman.coco.desktop.settings.ClientSettings;
import com.gazman.lifecycle.utils.Command;
import org.bitcoinj.core.Base58;

/**
 * Created by Ilya Gazman on 1/27/2018.
 */
public class GetWorkRequest implements Command {

    private CocoRequest.Callback<WorkData> callback;
    private long blob;
    private long minedDate;
    private int workId = -1;

    public GetWorkRequest setWorkId(int workId) {
        this.workId = workId;
        return this;
    }

    public GetWorkRequest setBlob(long blob) {
        this.blob = blob;
        return this;
    }

    public GetWorkRequest setMinedDate(long minedDate) {
        this.minedDate = minedDate;
        return this;
    }

    public GetWorkRequest setCallback(CocoRequest.Callback<WorkData> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void execute() {
        PoolData poolData = new PoolData();
        poolData.host = ClientSettings.instance.poolWebsite;
        poolData.port = ClientSettings.instance.poolPort;
        poolData.publicKey = Base58.decode(ClientSettings.instance.poolPublicKey);

        CocoRequest<WorkData, ?> request = CocoRequest.create(WorkData.class, poolData)
                .setCallback(callback)
                .setPath("work");
        if (workId != -1) {
            request.addQueryParameter("workId", workId)
                    .addQueryParameter("minedDate", minedDate)
                    .addQueryParameter("blob", blob);
        }
        request.execute();
    }
}
