package com.gazman.coco.desktop.root.commands;

import com.gazman.coco.desktop.ScreensController;
import com.gazman.coco.desktop.controllers.PasswordController;
import com.gazman.coco.desktop.controllers.PasswordUtils;
import com.gazman.coco.desktop.settings.ClientSettings;
import com.gazman.coco.desktop.settings.EncryptionSettings;
import com.gazman.coco.desktop.wallet.WalletModel;
import com.gazman.lifecycle.Factory;
import com.gazman.lifecycle.utils.Command;

import java.io.File;

/**
 * Created by Ilya Gazman on 3/21/2018.
 */
public class MoveToMainCommand implements Command {
    private ScreensController screensController = Factory.inject(ScreensController.class);
    private WalletModel walletModel =  Factory.inject(WalletModel.class);

    @Override
    public void execute() {
        ScreensController.Screen screen;

        if(EncryptionSettings.password != null){
            if(walletModel.getPrivateKey() != null){
                ClientSettings.instance.init();
                screen = screensController.mainScreen;
            }
            else{
                screen = screensController.welcomeScreen;
            }
        }
        else if(new File("settings/" + PasswordUtils.PASSWORD_FILE).exists()){
            screen = screensController.loginScreen;
        }
        else{
            screen = screensController.passwordScreen;
        }

        screen.open();
    }
}
