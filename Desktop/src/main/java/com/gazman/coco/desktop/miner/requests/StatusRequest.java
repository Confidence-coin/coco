package com.gazman.coco.desktop.miner.requests;

import com.gazman.coco.core.api.StatusData;
import com.gazman.coco.desktop.settings.ClientSettings;
import com.gazman.lifecycle.utils.Command;

/**
 * Created by Ilya Gazman on 2/25/2018.
 */
public class StatusRequest implements Command {

    private CocoRequest.Callback<StatusData> callback;

    public StatusRequest setCallback(CocoRequest.Callback<StatusData> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void execute() {
        CocoRequest.create(StatusData.class, ClientSettings.instance.defaultPoolData)
                .setCallback(callback)
                .setPath("status")
                .execute();
    }
}
