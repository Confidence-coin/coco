package com.gazman.coco.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ilya Gazman on 9/18/2017.
 */
public class InsertCommand {
    private Connection connection;
    private String tableName;
    private SqlExceptionHandler sqlExceptionHandler;
    private HashMap<String, Object> values = new HashMap<>();
    private ArrayList<String> keys = new ArrayList<>();
    private String returningString;
    private DB.StatementHandler statementHandler;
    private String conflictString;

    public InsertCommand setConflictString(String conflictString) {
        this.conflictString = conflictString;
        return this;
    }

    public InsertCommand setSqlExceptionHandler(SqlExceptionHandler sqlExceptionHandler) {
        this.sqlExceptionHandler = sqlExceptionHandler;
        return this;
    }

    public InsertCommand setConnection(Connection connection) {
        this.connection = connection;
        return this;
    }

    public InsertCommand setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public InsertCommand add(String key, Object value) {
        if (values.put(key, value) != null) {
            throw new Error("Duplicate key");
        }
        keys.add(key);
        return this;
    }

    public InsertCommand setReturningString(String returningString, DB.StatementHandler statementHandler) {
        this.returningString = returningString;
        this.statementHandler = statementHandler;
        return this;
    }

    public int execute() {
        if (connection == null) {
            return -1;
        }
        StringBuilder sql = new StringBuilder()
                .append("insert into ")
                .append(tableName).append("(");
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
        if (returningString != null) {
            sql.append(" returning ")
                    .append(returningString);
        }
        if (conflictString != null) {
            sql.append(conflictString);
        }
        try {
            PreparedStatement statement = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < keys.size(); i++) {
                Object value = values.get(keys.get(i));
                applyValue(value, statement, i + 1);
            }
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("insert failed, no rows affected.");
            }
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (returningString != null) {
                    statementHandler.handle(resultSet);
                    return 0;
                }
                if (resultSet.next()) {
                    if (resultSet.getMetaData().getColumnType(1) == Types.INTEGER) {
                        return resultSet.getInt(1);
                    }
                } else {
                    sqlExceptionHandler.onException(new SQLException("insert failed, no ID obtained."));
                }
            }
        } catch (SQLException e) {
            sqlExceptionHandler.onException(e);
        }
        return -1;
    }

    static void applyValue(Object value, PreparedStatement preparedStatement, int index) throws SQLException {
        if (value instanceof String) {
            preparedStatement.setString(index, (String) value);
        } else if (value instanceof Integer) {
            preparedStatement.setInt(index, (int) value);
        } else if (value instanceof Long) {
            preparedStatement.setLong(index, (long) value);
        } else if (value instanceof Double) {
            preparedStatement.setDouble(index, (double) value);
        } else if (value instanceof Boolean) {
            preparedStatement.setBoolean(index, (boolean) value);
        } else if (value instanceof Byte) {
            preparedStatement.setByte(index, (byte) value);
        } else if (value instanceof byte[]) {
            preparedStatement.setBytes(index, (byte[]) value);
        } else {
            throw new Error(value.getClass().getSimpleName() + " not implemented");
        }
    }
}
