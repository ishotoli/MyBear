package io.mybear.storage.parserHandler;

import io.mybear.storage.StorageDio;
import io.mybear.storage.storageNio.StorageClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by jamie on 2017/7/12.
 */
public class SetMetaDataParserHandler implements ParserHandler<StorageClientInfo, ByteBuffer> {
    public static final int SIZE = 33;
    private static final Logger LOGGER = LoggerFactory.getLogger(SetMetaDataParserHandler.class);

    @Override
    public long handleMetaData(StorageClientInfo con, ByteBuffer nioData) {
        nioData.flip();
        long filenameLen = nioData.getLong();
        long meta_buff_length = nioData.getLong();
        byte flag = nioData.get();
        byte[] group_name = new byte[nioData.position() - 1];
        nioData.get(group_name);
        nioData.position(0);
        System.out.println("groupName:" + new String(group_name));
        return 0;
    }

    @Override
    public void handle(StorageClientInfo con, ByteBuffer nioData) {
        byte[] bytes = new byte[nioData.position()];
        nioData.flip();
        nioData.get(bytes);
        String s = new String(bytes);
        System.out.println(s);
    }

    @Override
    public void handleEnd(StorageClientInfo con, ByteBuffer nioData) {
        byte[] bytes = new byte[nioData.position()];
        nioData.flip();
        nioData.get(bytes);
        String s = new String(bytes);
        System.out.println(s);
        StorageDio.queuePush(con);
    }

    @Override
    public int getSize() {
        return SIZE;
    }

}
