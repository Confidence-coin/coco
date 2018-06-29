package com.gazman.coco.db;

import java.sql.SQLException;

/**
 * Created by Ilya Gazman on 9/18/2017.
 */
public interface SqlExceptionHandler {

    void onException(SQLException e);
}
