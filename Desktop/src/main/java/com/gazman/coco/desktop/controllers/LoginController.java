package com.gazman.coco.desktop.controllers;

import com.gazman.coco.desktop.popups.PopupBuilder;
import com.gazman.coco.desktop.root.commands.MoveToMainCommand;
import com.gazman.coco.desktop.settings.EncryptionSettings;
import com.gazman.coco.desktop.wallet.WalletModel;
import com.gazman.lifecycle.Factory;
import javafx.scene.control.PasswordField;
import javafx.scene.input.MouseEvent;

/**
 * Created by Ilya Gazman on 6/21/2018.
 */
public class LoginController {
    public PasswordField password;
    private MoveToMainCommand moveToMainCommand = Factory.inject(MoveToMainCommand.class);
    private EncryptionSettings encryptionSettings = Factory.inject(EncryptionSettings.class);
    private WalletModel walletModel = Factory.inject(WalletModel.class);

    public void loginHandler(MouseEvent mouseEvent) {
        if(encryptionSettings.login(PasswordUtils.password(password.getText()), PasswordUtils.PASSWORD_FILE)){
            walletModel.init();
            moveToMainCommand.execute();
        }
        else{
            Factory.inject(PopupBuilder.class)
                    .setMessage("Invalid password")
                    .setTitle("Error")
                    .execute();
        }
    }
}
