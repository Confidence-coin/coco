package com.gazman.coco.desktop.settings;

import com.gazman.coco.core.settings.BaseSettings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Ilya Gazman on 3/28/2018.
 */
public class EncryptionSettings extends BaseSettings {

    public static char[] password;

    public void setPassword(char[] password, String fileName) {
        EncryptionSettings.password = password;
        file = createSettingsFile(fileName);
        setSaveDefaults(true);
        readString("password", "123456");
        save("password updated");
    }

    public boolean login(char[] password, String fileName) {
        EncryptionSettings.password = password;
        load(fileName);
        if (!"123456".equals(readString("password", ""))) {
            EncryptionSettings.password = null;
        }
        return EncryptionSettings.password != null;
    }

    @Override
    public void load(String fileName) {
        file = createSettingsFile(fileName);

        EncryptionHandler encryptionHandler = new EncryptionHandler();
        try (ByteArrayInputStream stream = new ByteArrayInputStream(
                encryptionHandler.decrypt(password, file))) {
            properties.load(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(String comment) {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(saveToByteArray(comment))) {
            EncryptionHandler encryptionHandler = new EncryptionHandler();
            encryptionHandler.encrypt(password, file, stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] saveToByteArray(String comment) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        properties.store(stream, comment);
        byte[] data = stream.toByteArray();
        stream.close();
        return data;
    }
}
