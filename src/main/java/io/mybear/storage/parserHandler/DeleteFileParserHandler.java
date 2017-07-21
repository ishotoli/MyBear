package io.mybear.storage.parserHandler;

import io.mybear.common.StorageFileContext;
import io.mybear.storage.StorageDio;
import io.mybear.storage.storageNio.StorageClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by jamie on 2017/7/12.
 */
public class DeleteFileParserHandler implements ParserHandler<StorageClientInfo, ByteBuffer> {
    public static final int SIZE = 16;
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFileParserHandler.class);

    @Override
    public long handleMetaData(StorageClientInfo con, ByteBuffer nioData) {
        con.fileContext = new StorageFileContext();
        byte[] group_name = new byte[16];
        nioData.flip();
        nioData.get(group_name);
        System.out.println(new String(group_name));
        return 0;
    }

    @Override
    public void handle(StorageClientInfo con, ByteBuffer nioData) {
        byte[] bytes = new byte[nioData.position()];
        nioData.flip();
        nioData.get(bytes);
        String s = new String(bytes);
        con.fileContext.filename += s;

    }

    @Override
    public void handleEnd(StorageClientInfo con, ByteBuffer nioData) {
        byte[] bytes = new byte[nioData.position()];
        nioData.flip();
        nioData.get(bytes);
        String s = new String(bytes);
        con.fileContext.filename += s;
        System.out.println(con.fileContext.filename);
        StorageDio.queuePush(con);
    }

    @Override
    public int getSize() {
        return SIZE;
    }
}
