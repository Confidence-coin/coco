package com.gazman.coco.core.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Ilya Gazman on 1/27/2018.
 */
public class BaseSettings {
    protected File file;
    protected Properties properties = new Properties();
    private boolean wasNull;
    private boolean saveDefaults;
    private int defaults;

    public void setSaveDefaults(boolean saveDefaults) {
        this.saveDefaults = saveDefaults;
    }

    public void load(String fileName) {
        file = createSettingsFile(fileName);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readString(String key, DefaultLoader loader) {
        String value = properties.getProperty(key, null);
        if (value != null) {
            return value;
        }
        return readString(key, loader.load());
    }

    public String readStringOrThrow(String key) {
        String value = properties.getProperty(key, null);
        if (value != null) {
            return value;
        }
        throw new Error("Oops");
    }

    public String readString(String key, String defaultVale) {
        String value = properties.getProperty(key, null);
        if (value == null) {
            value = defaultVale;
            if (defaultVale != null && saveDefaults) {
                defaults++;
                properties.setProperty(key, defaultVale);
            }
        }
        wasNull = value == null;
        return value;
    }

    public int readInteger(String key, int defaultValue) {
        String value = readString(key, defaultValue + "");
        return Integer.parseInt(value);
    }

    public boolean wasNull() {
        return wasNull;
    }

    public void save(String comment) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            properties.store(fileOutputStream, comment);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDefaults(String comment) {
        if (defaults > 0) {
            defaults = 0;
            save(comment);
        }
    }

    protected File createSettingsFile(String fileName) {
        File root = new File(System.getProperty("user.dir") + File.separator + "settings" + File.separator);
        if (!root.exists() && !root.mkdirs()) {
            throw new Error("Error creating settings directory " + root);
        }
        File settings = new File(root, fileName);
        try {
            if (!settings.exists() && !settings.createNewFile()) {
                throw new Error("Error creating settings file at " + settings.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return settings;
    }

    public void writeKey(String key, String value) {
        properties.setProperty(key, value);
    }

    public interface DefaultLoader {

        String load();
    }
}
