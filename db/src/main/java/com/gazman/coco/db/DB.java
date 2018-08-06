package com.gazman.coco.db;

import com.gazman.lifecycle.Factory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by Ilya Gazman on 6/19/2017.
 */
public class DB implements Closeable {

    private final static HikariDataSource dataSource;
    private Connection connection;
    private boolean transactionStarted;

    static {
        HikariConfig config = new HikariConfig();
        //noinspection SpellCheckingInspection
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
        config.setUsername("postgres");
        //noinspection SpellCheckingInspection
        config.setPassword("mysecretpass");
        config.setConnectionTestQuery("select 1");
        config.setAutoCommit(true);

        dataSource = new HikariDataSource(config);
    }

    public void beginTransaction() {
        try {
            getConnection().setAutoCommit(false);
            transactionStarted = true;
        } catch (SQLException e) {
            onException(e);
        }
    }

    public void markTransactionSuccessful() {
        try {
            getConnection().commit();
            transactionStarted = false;
        } catch (SQLException e) {
            onException(e);
        }
    }

    public boolean query(String sql, StatementHandler handler) {
        try (
                Statement statement = getConnection().createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            return !resultSet.isClosed() && handler.handle(resultSet);
        } catch (SQLException e) {
            onException(e);
        }
        return false;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = dataSource.getConnection();
            connection.setAutoCommit(true);
        }
        return connection;
    }

    @Override
    public void close() {
        try {
            if (transactionStarted) {
                transactionStarted = false;
                rollback();
            }
            connection.close();
            connection = null;
        } catch (SQLException e) {
            onException(e);
        }
    }

    public void rollback() {
        try {
            transactionStarted = false;
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            close();
        }
    }

    public InsertCommand insert(String tableName) {
        try {
            return Factory.inject(InsertCommand.class)
                    .setConnection(getConnection())
                    .setSqlExceptionHandler(DB.this::onException)
                    .setTableName(tableName);
        } catch (SQLException e) {
            onException(e);
        }
        return Factory.inject(InsertCommand.class);
    }

    public <T> void insertBatch(String tableName, List<T> items,
                                InsertBatchCommand.InsertHandler<T> handler) {
        try {
            new InsertBatchCommand<T>()
                    .setTableName(tableName)
                    .setConnection(getConnection())
                    .setItems(items)
                    .setInsertHandler(handler)
                    .execute();
        } catch (SQLException e) {
            onException(e);
        }
    }

    /**
     * Executes all the scripts in the file, stops if one script fails
     * return true if scripts are found and all successfully executed
     */
    public boolean execute(File sqlScript) {
        boolean result = false;
        for (String script : loadScripts(sqlScript)) {
            if (execute(script)) {
                result = true;
            } else {
                break;
            }
        }
        return result;
    }

    public boolean execute(String sql) {
        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sql);
            return true;
        } catch (SQLException e) {
            onException(e);
        }
        return false;
    }

    public boolean execute(String sql, StatementHandler handler) {
        try (Statement statement = getConnection().createStatement()) {
            if (!statement.execute(sql)) {
                //noinspection StatementWithEmptyBody
                while (!statement.getMoreResults() && statement.getUpdateCount() != -1)
                    ;
            }
            ResultSet resultSet = statement.getResultSet();
            if (resultSet != null) {
                return handler.handle(resultSet);
            }
        } catch (SQLException e) {
            onException(e);
            return false;
        }
        return true;
    }

    public void loadFromCsvFile(Object context, String tableName, String resourcePath) {
        loadFromCsvFile(context.getClass(), tableName, resourcePath);
    }

    public void loadFromCsvFile(Class<?> context, String tableName, String resourcePath) {
        InputStream inputStream = context.getClassLoader().getResourceAsStream(resourcePath);

        try {
            Connection connection = ConnectionExtractor.extract(getConnection());
            if (connection == null) {
                throw new Error("Failed extracting connection");
            }
            CopyManager copyManager = new CopyManager((BaseConnection) connection);
            copyManager.copyIn("copy " + tableName + " from stdin delimiter E'\\t' CSV HEADER", inputStream);
        } catch (SQLException | IOException e) {
            onException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public interface StatementHandler {
        boolean handle(ResultSet resultSet) throws SQLException;
    }

    private void onException(Exception e) {
        e.printStackTrace();
    }

    private String[] loadScripts(File sqlScript) {
        String script = null;
        try {
            script = new String(Files.readAllBytes(sqlScript.toPath()), StandardCharsets.UTF_8).trim();
            if (script.endsWith(";")) {
                script = script.substring(0, script.length() - 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (script == null) {
            return new String[0];
        }
        return script.split(";");
    }

}
