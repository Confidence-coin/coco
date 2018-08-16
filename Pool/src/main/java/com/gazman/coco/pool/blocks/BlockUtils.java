package com.gazman.coco.pool.blocks;

import com.gazman.coco.pool.settings.PoolSettings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.gazman.coco.core.utils.ByteUtils.toByteArray;

public class BlockUtils {

    public static void main(String... args) {
        System.out.println(UUID.randomUUID());
    }

    public static byte[] computeHeaderHash(int blockId, double transactionFees,
                                           double smartContractFees,
                                           byte[] smartContractsExecutionHash,
                                           byte[] previousHash) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        stream.write(toByteArray(blockId));
        stream.write(getCoreVersion(blockId));
        stream.write(PoolSettings.POOL_DOMAIN.length());
        stream.write(PoolSettings.POOL_DOMAIN.getBytes(StandardCharsets.UTF_8));
        stream.write(toByteArray(transactionFees));
        stream.write(toByteArray(smartContractFees));
        stream.write(toByteArray(calculateReword(blockId)));
        stream.write(toByteArray(PoolSettings.POOL_MINER_ID));
        stream.write(toByteArray(getLeadershipRewardId(blockId)));
        stream.write(smartContractsExecutionHash);
        stream.write(previousHash);

        return stream.toByteArray();
    }

    private static int calculateReword(int blockId) {
        return 250 - blockId / 3188;
    }

    private static int getLeadershipRewardId(int blockId) {
        if (blockId < 94) {
            return -1;
        }
        return 1;
    }

    private static byte[] getCoreVersion(int blockId) {
        if (blockId < 94) {
            return toByteArray(UUID.fromString("944f716c-6c6b-4b0c-8249-4ecc9987f96b"));
        }
        return toByteArray(UUID.fromString("96684085-9b8d-4946-8a61-08df6e1bd0d0"));
    }
}
