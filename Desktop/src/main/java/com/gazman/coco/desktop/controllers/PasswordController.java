package com.gazman.coco.desktop.controllers;

import com.gazman.coco.desktop.popups.PopupBuilder;
import com.gazman.coco.desktop.root.commands.MoveToMainCommand;
import com.gazman.coco.desktop.settings.EncryptionSettings;
import com.gazman.coco.desktop.wallet.WalletModel;
import com.gazman.lifecycle.Factory;
import javafx.scene.control.PasswordField;

import static com.gazman.coco.desktop.controllers.PasswordUtils.PASSWORD_FILE;

/**
 * Created by Ilya Gazman on 3/18/2018.
 */
public class PasswordController {

    public PasswordField confirmPassword;
    public PasswordField password;


    private WalletModel walletModel = Factory.inject(WalletModel.class);
    private EncryptionSettings encryptionSettings = Factory.inject(EncryptionSettings.class);

    public void continueHandler() {
        String newPassword = password.getText();
        String confirmedPassword = password.getText();
        String errorMessage = validatePassword(newPassword, confirmedPassword);
        if (errorMessage == null) {
            encryptionSettings.setPassword(PasswordUtils.password(newPassword), PASSWORD_FILE);
            walletModel.init();
            Factory.inject(MoveToMainCommand.class).execute();
        } else {
            Factory.inject(PopupBuilder.class)
                    .setMessage(errorMessage)
                    .setTitle("Error")
                    .execute();

        }
    }

    private String validatePassword(String newPassword, String confirmedPassword) {
        if (!newPassword.equals(confirmedPassword)) {
            return "Password mismatch";
        }

        if (newPassword.length() < 6) {
            return "Password needs to be attlist 6 digits long";
        }

        return null;
    }
}
