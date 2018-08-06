package com.gazman.coco.db;

import java.lang.reflect.Field;
import java.sql.Connection;

class ConnectionExtractor {

    static Connection extract(Connection connection) throws IllegalAccessException {
        Field poolEntryField = extractField(connection.getClass(), "poolEntry");
        if (poolEntryField == null) {
            return null;
        }
        poolEntryField.setAccessible(true);
        Object poolEntry = poolEntryField.get(connection);
        Field connectionField = extractField(poolEntry.getClass(), "connection");
        if (connectionField == null) {
            return null;
        }
        connectionField.setAccessible(true);

        return (Connection) connectionField.get(poolEntry);
    }

    private static Field extractField(Class aClass, String field) {
        do {
            for (Field declaredField : aClass.getDeclaredFields()) {
                if (declaredField.getName().equals(field)) {
                    return declaredField;
                }
            }
            aClass = aClass.getSuperclass();
        } while (aClass != null);
        return null;
    }
}
