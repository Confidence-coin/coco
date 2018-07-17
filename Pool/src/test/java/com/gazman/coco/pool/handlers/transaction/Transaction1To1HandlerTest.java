package com.gazman.coco.pool.handlers.transaction;

import com.gazman.coco.core.utils.MultiByteInteger;
import com.gazman.coco.core.utils.Utils;
import com.gazman.coco.pool.handlers.transaction.mocks.DBMock;
import com.gazman.coco.pool.handlers.transaction.mocks.InsertCommandMock;
import com.gazman.coco.pool.handlers.transaction.mocks.TransactionsHelperMock;
import com.gazman.lifecycle.Bootstrap;
import com.gazman.lifecycle.Factory;
import com.gazman.lifecycle.signal.SignalsHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by Ilya Gazman on 2/23/2018.
 */
public class Transaction1To1HandlerTest {

    private byte[] senderFullId = new byte[32];
    private byte[] signature = new byte[64];
    private int blockId = 1;

    @Before
    public void init() {
        new Bootstrap() {

            @Override
            protected void initClasses() {
                registerClass(TransactionsHelperMock.class);
                registerClass(DBMock.class);
                registerClass(InsertCommandMock.class);
            }

            @Override
            protected void initSignals(SignalsHelper signalsHelper) {

            }

            @Override
            protected void initRegistrars() {

            }
        }.initialize(getClass());
        new Random().nextBytes(senderFullId);
    }

    @After
    public void cleanUp() {
        TransactionsHelperMock mock = Factory.inject(TransactionsHelperMock.class);
        mock.sender = null;
        mock.wallets = null;
    }

    @Test
    public void negativeAmount() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put(new byte[32]);
        buffer.putDouble(-1);
        buffer.rewind();

        testFalse(buffer);
    }

    @Test
    public void toSmallAmount() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put(new byte[32]);
        buffer.putDouble(Utils.COCO_BRONZE / 2);
        buffer.rewind();

        testFalse(buffer);
    }

    @Test
    public void outOfRangeAmount() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put(new byte[32]);
        buffer.putDouble(Utils.COCO_BRONZE / 2 + 5);
        buffer.rewind();

        testFalse(buffer);
    }

    @Test
    public void feesBelowMinimum() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put(new byte[32]);
        buffer.putDouble(5);
        buffer.put(MultiByteInteger.encode(Utils.DEFAULT_AND_MINIMUM_FEES - 1 * Utils.COCO_BRONZE));
        buffer.rewind();

        testFalse(buffer);
    }

    @Test
    public void senderIdNotFoundNull() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put(new byte[32]);
        buffer.putDouble(5);
        byte[] bytes = MultiByteInteger.encode(Utils.DEFAULT_AND_MINIMUM_FEES);
        buffer.put(bytes);
        buffer.rewind();

        testFalse(buffer);
    }

    @Test
    public void senderIdNotFoundLength0() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put(new byte[32]);
        buffer.putDouble(5);
        byte[] bytes = MultiByteInteger.encode(Utils.DEFAULT_AND_MINIMUM_FEES);
        buffer.put(bytes);
        buffer.rewind();

        TransactionsHelperMock helperMock = Factory.inject(TransactionsHelperMock.class);
        helperMock.wallets = new TransactionsHelper.Wallet[0];

        testFalse(buffer);
    }

    @Test
    public void senderIdNotFoundLength1() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] receiverFullId = new byte[32];
        new Random().nextBytes(receiverFullId);
        buffer.put(receiverFullId);
        buffer.putDouble(5);
        byte[] bytes = MultiByteInteger.encode(Utils.DEFAULT_AND_MINIMUM_FEES);
        buffer.put(bytes);
        buffer.rewind();

        TransactionsHelperMock helperMock = Factory.inject(TransactionsHelperMock.class);
        helperMock.wallets = new TransactionsHelper.Wallet[1];
        helperMock.wallets[0] = helperMock.new Wallet();
        helperMock.wallets[0].id = 1;
        helperMock.wallets[0].publicKey = receiverFullId;

        testFalse(buffer);
    }

    @Test
    public void senderIdNotFoundLength2() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] receiverFullId = new byte[32];
        new Random().nextBytes(receiverFullId);
        buffer.put(receiverFullId);
        buffer.putDouble(5);
        byte[] bytes = MultiByteInteger.encode(Utils.DEFAULT_AND_MINIMUM_FEES);
        buffer.put(bytes);
        buffer.rewind();

        TransactionsHelperMock helperMock = Factory.inject(TransactionsHelperMock.class);
        helperMock.wallets = new TransactionsHelper.Wallet[2];
        helperMock.wallets[0] = helperMock.new Wallet();
        helperMock.wallets[0].id = 1;
        helperMock.wallets[0].publicKey = receiverFullId;
        helperMock.wallets[1] = helperMock.wallets[0];

        testFalse(buffer);
    }

    @Test
    public void sendToYourself() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] receiverFullId = new byte[32];
        new Random().nextBytes(receiverFullId);
        buffer.put(receiverFullId);
        buffer.putDouble(5);
        byte[] bytes = MultiByteInteger.encode(Utils.DEFAULT_AND_MINIMUM_FEES);
        buffer.put(bytes);
        buffer.rewind();

        TransactionsHelperMock helperMock = Factory.inject(TransactionsHelperMock.class);
        helperMock.wallets = new TransactionsHelper.Wallet[2];
        helperMock.wallets[0] = helperMock.new Wallet();
        helperMock.wallets[0].id = 1;
        helperMock.wallets[0].publicKey = senderFullId;
        helperMock.wallets[1] = helperMock.wallets[0];

        testFalse(buffer);
    }

    @Test
    public void senderIdNotFoundWrongId() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] receiverFullId = new byte[32];
        new Random().nextBytes(receiverFullId);
        buffer.put(receiverFullId);
        buffer.putDouble(5);
        byte[] bytes = MultiByteInteger.encode(Utils.DEFAULT_AND_MINIMUM_FEES);
        buffer.put(bytes);
        buffer.rewind();

        TransactionsHelperMock helperMock = Factory.inject(TransactionsHelperMock.class);
        helperMock.wallets = new TransactionsHelper.Wallet[1];
        helperMock.wallets[0] = helperMock.new Wallet();
        helperMock.wallets[0].id = -9;
        helperMock.wallets[0].publicKey = senderFullId;

        testFalse(buffer);
    }

    @Test
    public void errorFetchingBalance() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] receiverFullId = new byte[32];
        new Random().nextBytes(receiverFullId);
        buffer.put(receiverFullId);
        buffer.putDouble(5);
        byte[] bytes = MultiByteInteger.encode(Utils.DEFAULT_AND_MINIMUM_FEES);
        buffer.put(bytes);
        buffer.rewind();

        TransactionsHelperMock helperMock = Factory.inject(TransactionsHelperMock.class);
        helperMock.wallets = new TransactionsHelper.Wallet[1];
        helperMock.wallets[0] = helperMock.new Wallet();
        helperMock.wallets[0].id = 5;
        helperMock.wallets[0].publicKey = senderFullId;

        helperMock.balance = -1;

        testFalse(buffer);
    }

    @Test
    public void errorSmallBalance0() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] receiverFullId = new byte[32];
        new Random().nextBytes(receiverFullId);
        buffer.put(receiverFullId);
        double amount = 5;
        buffer.putDouble(amount);
        byte[] fees = MultiByteInteger.encode(Utils.DEFAULT_AND_MINIMUM_FEES);
        buffer.put(fees);
        buffer.rewind();

        TransactionsHelperMock helperMock = Factory.inject(TransactionsHelperMock.class);
        helperMock.wallets = new TransactionsHelper.Wallet[1];
        helperMock.wallets[0] = helperMock.new Wallet();
        helperMock.wallets[0].id = 5;
        helperMock.wallets[0].publicKey = senderFullId;

        helperMock.balance = 0;

        testFalse(buffer);
    }

    @Test
    public void errorSmallBalanceAmount() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] receiverFullId = new byte[32];
        new Random().nextBytes(receiverFullId);
        buffer.put(receiverFullId);
        double amount = 5;
        buffer.putDouble(amount);
        double fees = Utils.DEFAULT_AND_MINIMUM_FEES;
        buffer.put(MultiByteInteger.encode(fees));
        buffer.rewind();

        TransactionsHelperMock helperMock = Factory.inject(TransactionsHelperMock.class);
        helperMock.wallets = new TransactionsHelper.Wallet[1];
        helperMock.wallets[0] = helperMock.new Wallet();
        helperMock.wallets[0].id = 5;
        helperMock.wallets[0].publicKey = senderFullId;

        helperMock.balance = amount + 98 * fees;

        testFalse(buffer);
    }

    @Test
    public void trueTest() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] receiverFullId = new byte[32];
        new Random().nextBytes(receiverFullId);
        buffer.put(receiverFullId);
        double amount = 5;
        buffer.putDouble(amount);
        double fees = Utils.DEFAULT_AND_MINIMUM_FEES;
        buffer.put(MultiByteInteger.encode(fees));
        buffer.rewind();

        TransactionsHelperMock helperMock = Factory.inject(TransactionsHelperMock.class);
        helperMock.wallets = new TransactionsHelper.Wallet[1];
        helperMock.wallets[0] = helperMock.new Wallet();
        helperMock.wallets[0].id = 5;
        helperMock.wallets[0].publicKey = senderFullId;

        helperMock.balance = amount + 108 * fees;

        testTrue(buffer);
    }

    private void testFalse(ByteBuffer buffer) throws IOException {
//        String errorMessage = new Transaction1To1Handler().execute(1, senderFullId, signature, blockId, buffer);
//        System.out.println(errorMessage);
//        assertNotNull(errorMessage);
    }

    private void testTrue(ByteBuffer buffer) throws IOException {
//        assertNull(new Transaction1To1Handler().execute(1, senderFullId, signature, blockId, buffer));
    }

}