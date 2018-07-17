package com.gazman.coco.pool.handlers.transaction;

import com.gazman.coco.core.utils.MultiByteInteger;
import com.gazman.coco.core.utils.Utils;
import com.gazman.coco.pool.handlers.transaction.mocks.BlockModelMock;
import com.gazman.coco.pool.handlers.transaction.mocks.Transaction1To1HandlerMock;
import com.gazman.lifecycle.Bootstrap;
import com.gazman.lifecycle.Factory;
import com.gazman.lifecycle.signal.SignalsHelper;
import org.junit.Before;
import org.junit.Test;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by Ilya Gazman on 2/23/2018.
 */
public class RootTransactionHandlerTest {

    private RootTransactionHandler rootTransactionHandler;
    private Random random = new Random();

    @Before
    public void setup() {
        new Bootstrap() {

            @Override
            protected void initClasses() {
                registerClass(BlockModelMock.class);
                registerClass(Transaction1To1HandlerMock.class);
            }

            @Override
            protected void initSignals(SignalsHelper signalsHelper) {

            }

            @Override
            protected void initRegistrars() {

            }
        }.initialize(getClass());

//        rootTransactionHandler = new RootTransactionHandler() {
//            @Override
//            public String onHandle(InputStream inputStream) throws IOException {
//                return super.onHandle(inputStream);
//            }
//        };

        Factory.inject(BlockModelMock.class).blockId = 1;
    }

    @Test
    public void checkNull() throws Exception {
        assertNotNull(rootTransactionHandler.onHandle(null,null, false));
    }

    @Test
    public void emptyStream() throws Exception {
        InputStream stream = new ByteArrayInputStream(new byte[0]);
        assertNotNull(rootTransactionHandler.onHandle(null, stream, false));
    }

    @Test
    public void randomStream() throws Exception {
        byte[] bytes = new byte[1024 * 1024 * 10];
        random.nextBytes(bytes);
        InputStream stream = new ByteArrayInputStream(bytes);
        assertNotNull(rootTransactionHandler.onHandle(null, stream, false));
    }

    @Test
    public void dummyData() throws Exception {
        byte[] data = new byte[1000];
        assertNotNull(rootTransactionHandler.onHandle(null, buildStream(data), false));
    }

    @Test
    public void incorrectType() throws Exception {
        byte[] data = new byte[120];
        setType(data, 90);
        assertNotNull(rootTransactionHandler.onHandle(null, buildStream(data), false));
    }

    @Test
    public void incorrectBlockHash() throws Exception {
        Factory.inject(BlockModelMock.class).blockId = -1;
        byte[] data = new byte[120];
        setType(data, 1);

        assertNotNull(rootTransactionHandler.onHandle(null, buildStream(data), false));
    }

    @Test
    public void trueTest() throws Exception {
        byte[] data = new byte[120];
        setType(data, 1);

        assertNull(rootTransactionHandler.onHandle(null, buildStream(data), false));
    }

    private void setType(byte[] data, int type) {
        byte[] typeData = MultiByteInteger.encode(type);
        System.arraycopy(typeData, 0, data, 32, typeData.length);
    }

    private InputStream buildStream(byte[] data) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Curve25519 cipher = Utils.createCipher();
        Curve25519KeyPair keyPair = cipher.generateKeyPair();
        System.arraycopy(keyPair.getPublicKey(), 0, data, 0, 32);

        byte[] signature = cipher.calculateSignature(keyPair.getPrivateKey(), data);
        outputStream.write(signature);
        outputStream.write(MultiByteInteger.encode(data.length));
        outputStream.write(data);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

}