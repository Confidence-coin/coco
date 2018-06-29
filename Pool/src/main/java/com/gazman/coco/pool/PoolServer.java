package com.gazman.coco.pool;

import com.gazman.coco.core.block.Wallet;
import com.gazman.coco.core.hash.Sha256Hash;
import com.gazman.coco.core.utils.Utils;
import com.gazman.coco.db.DB;
import com.gazman.coco.pool.blocks.Block;
import com.gazman.coco.pool.handlers.status.StatusHandler;
import com.gazman.coco.pool.handlers.transaction.RootTransactionHandler;
import com.gazman.coco.pool.handlers.work.WorkHandler;
import com.gazman.coco.pool.settings.PoolSettings;
import com.sun.net.httpserver.HttpServer;
import org.bitcoinj.core.Base58;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Ilya Gazman on 1/18/2018.
 */
public class PoolServer {

    public static void main(String... args) {
        initialize();
//        while (true)
        {
            HttpServer server = null;
            ExecutorService executor = Executors.newFixedThreadPool(20);
            try {
                server = HttpServer.create(new InetSocketAddress("localhost", 8081), 0);
                System.out.println("Server started on " + server.getAddress().getHostName() + ":" +
                        server.getAddress().getPort());
                server.createContext("/work", new WorkHandler());
                server.createContext("/status", new StatusHandler());
                server.createContext("/transaction", new RootTransactionHandler());
                server.setExecutor(executor);
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
                if (server != null) {
                    try {
                        server.stop(0);
                    } catch (Exception ignore) {
                    }
                    try {
                        executor.shutdown();
                    } catch (Exception ignore) {
                    }
                }
            }
        }
    }

    private static void initialize() {
        System.out.println("public key: " + PoolSettings.PUBLIC_KEY);
        boolean success;
        try (DB db = new DB()) {
            success = db.query("select count(*) from core.blocks", resultSet -> {
                if (!resultSet.next()) {
                    return false;
                }
                int count = resultSet.getInt(1);
                return count != 0 || createGenesisBlock();
            });

        }
        if (!success) {
            throw new Error("Error initializing block chain");
        }
    }

    private static boolean createGenesisBlock() {
        Block block = new Block();
        block.blockId = 94;
        block.difficulty = Base58.decode("111HVkPNMRztH8Y1Q5XmiTczYaRjPnC69AWQcuwK6i");
        block.coreVersion = Utils.uuidToByteArray("cd1c01cc-edd8-429f-b60c-d427e1faf9ee");
        block.rewordId = 1;
        block.previousBlockHash = Sha256Hash.hash("Big bang");
        block.minedDate = 1516625826653L;
        block.blob = -790154574586295207L;
        block.jobId = Utils.uuidToByteArray("15b0e7e1-6ada-4ff7-b23f-a07927503981");
        block.transactions = new byte[0];
        initWallets(block);

//        System.out.println(block.hashUUID(block.jobId));
//        System.exit(1);

        block.blockHash = block.hash();

//        return new ValidateAndSubmitBlock()
//                .setBlock(block)
//                .setValidateTimeStamp(false)
//                .doIt();
        return true;

    }

    private static void initWallets(Block block) {
        block.wallets = new ArrayList<>();
        Wallet wallet = new Wallet();
        wallet.address = Base58.decode("eUqa2@24uKsgKRzkzAwttf8QYhAZNQH8jUi1K1VgL3ptkvSa2Bo");
        wallet.id = 1;
        block.wallets.add(wallet);
    }
}
