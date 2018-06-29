package com.gazman.coco.pool.handlers.transaction.mocks;

import com.gazman.coco.db.DB;
import com.gazman.coco.db.InsertBatchCommand;
import com.gazman.coco.db.InsertCommand;
import com.gazman.lifecycle.Factory;
import com.gazman.lifecycle.signal.SignalsBag;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Ilya Gazman on 2/24/2018.
 */
public class DBMock extends DB {
    @Override
    public void beginTransaction() {

    }

    @Override
    public void markTransactionSuccessful() {

    }

    @Override
    public boolean query(String sql, StatementHandler handler) {
        return true;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return SignalsBag.log(Connection.class, "test");
    }

    @Override
    public void close() {

    }

    @Override
    public void rollback() {

    }

    @Override
    public InsertCommand insert(String tableName) {
        return Factory.inject(InsertCommand.class);
    }

    @Override
    public <T> void insertBatch(String tableName, List<T> items, InsertBatchCommand.InsertHandler<T> handler) {

    }

    @Override
    public boolean execute(String sql) {
        return true;
    }

    @Override
    public boolean execute(String sql, StatementHandler handler) {
        return true;
    }
}
