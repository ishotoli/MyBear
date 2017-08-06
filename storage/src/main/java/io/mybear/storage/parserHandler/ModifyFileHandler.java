package io.mybear.storage.parserHandler;

import io.mybear.storage.storageNio.StorageClientInfo;

import java.nio.ByteBuffer;

/**
 * Created by jamie on 2017/7/25.
 */
public class ModifyFileHandler implements ParserHandler<StorageClientInfo, ByteBuffer> {
    @Override
    public long handleMetaData(StorageClientInfo con, ByteBuffer nioData) {
        return 0;
    }

    @Override
    public void handle(StorageClientInfo con, ByteBuffer nioData) {

    }

    @Override
    public void handleEnd(StorageClientInfo con, ByteBuffer nioData) {

    }

    @Override
    public int getSize() {
        return 0;
    }
}
