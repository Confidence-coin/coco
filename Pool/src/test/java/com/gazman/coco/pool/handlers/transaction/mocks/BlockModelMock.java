package com.gazman.coco.pool.handlers.transaction.mocks;

import com.gazman.coco.db.DB;
import com.gazman.coco.pool.blocks.BlockModel;

/**
 * Created by Ilya Gazman on 2/23/2018.
 */
public class BlockModelMock extends BlockModel {

    public int blockId = 1;

    @Override
    public int findBlockId(DB db, byte[] blockHash) {
        return blockId;
    }
}
