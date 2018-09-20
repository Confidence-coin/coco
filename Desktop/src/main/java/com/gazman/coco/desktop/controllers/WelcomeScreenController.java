package com.gazman.coco.desktop.controllers;

import com.gazman.coco.desktop.root.BaseController;
import com.gazman.coco.desktop.root.commands.MoveToMainCommand;
import com.gazman.coco.desktop.wallet.WalletModel;
import com.gazman.lifecycle.Factory;

/**
 * Created by Ilya Gazman on 3/8/2018.
 */
public class WelcomeScreenController extends BaseController {
    private WalletModel walletModel = Factory.inject(WalletModel.class);


    public void createWallet() {
        walletModel.generateKey();
        walletModel.myWallets.add(Factory.inject(WalletModel.WalletData.class));
        Factory.inject(MoveToMainCommand.class).execute();
    }

    public void restoreWallet() {
        screens.restoreWalletScreen.open();
    }
}
