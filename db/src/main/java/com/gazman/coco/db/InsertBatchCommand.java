package com.gazman.coco.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.gazman.coco.db.InsertCommand.applyValue;

/**
 * Created by Ilya Gazman on 9/18/2017.
 */
public class InsertBatchCommand<T> {

    private Connection connection;
    private String tableName;
    private InsertHandler<T> insertHandler;
    private List<T> items;

    public InsertBatchCommand<T> setInsertHandler(InsertHandler<T> insertHandler) {
        this.insertHandler = insertHandler;
        return this;
    }

    public InsertBatchCommand<T> setItems(List<T> items) {
        this.items = items;
        return this;
    }

    public InsertBatchCommand<T> setConnection(Connection connection) {
        this.connection = connection;
        return this;
    }

    public InsertBatchCommand<T> setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public void execute() throws SQLException {
        StringBuilder sql = new StringBuilder()
                .append("insert into ")
                .append(tableName).append("(");

        HashMap<String, Object> values = new HashMap<>();
        ArrayList<String> keys = new ArrayList<>();
        insertHandler.onInsertRow(items.get(0), values);
        keys.addAll(values.keySet());

        boolean isFirst = true;
        for (String key : keys) {
            if (isFirst) {
                isFirst = false;
            } else {
                sql.append(",");
            }
            sql.append(key);
        }
        sql.append(") values(");
        isFirst = true;
        for (int i = 0; i < keys.size(); i++) {
            if (isFirst) {
                isFirst = false;
            } else {
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(")");

        PreparedStatement statement = connection.prepareStatement(sql.toString());
        addRow(values, keys, statement);
        statement.addBatch();
        Iterator<T> iterator = items.iterator();
        iterator.next();
        int count = 1;
        while (iterator.hasNext()) {
            values.clear();
            insertHandler.onInsertRow(iterator.next(), values);
            addRow(values, keys, statement);
            statement.addBatch();
            count++;
            if (count == 10000) {
                statement.executeBatch();
                count = 0;
            }
        }
        if (count > 0) {
            statement.executeBatch();
        }
    }

    private void addRow(HashMap<String, Object> values, ArrayList<String> keys, PreparedStatement statement) throws SQLException {
        for (int i = 0; i < keys.size(); i++) {
            Object value = values.get(keys.get(i));
            applyValue(value, statement, i + 1);
        }
    }

    public interface InsertHandler<T> {
        void onInsertRow(T data, HashMap<String, Object> values);
    }

}
