package com.gazman.coco.pool.handlers.transaction.mocks;

import com.gazman.coco.db.InsertCommand;

/**
 * Created by Ilya Gazman on 2/24/2018.
 */
public class InsertCommandMock extends InsertCommand {
    @Override
    public int execute() {
        return 1;
    }
}
