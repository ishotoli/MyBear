package io.mybear.storage.parserHandler;

import io.mybear.storage.storageNio.StorageClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by jamie on 2017/7/12.
 */
public class ActiveTestParserHandler implements ParserHandler<StorageClientInfo, ByteBuffer> {
    public static final int SIZE = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveTestParserHandler.class);

    @Override
    public long handleMetaData(StorageClientInfo con, ByteBuffer nioData) {
        return 0;
    }

    @Override
    public void handle(StorageClientInfo con, ByteBuffer nioData) {

    }

    @Override
    public void handleEnd(StorageClientInfo con, ByteBuffer nioData) {
        return;
    }

    @Override
    public int getSize() {
        return SIZE;
    }
}
