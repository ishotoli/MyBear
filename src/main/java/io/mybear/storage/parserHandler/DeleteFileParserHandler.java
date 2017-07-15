package io.mybear.storage.parserHandler;

import io.mybear.storage.StorageDio;
import io.mybear.storage.storageNio.FastTaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by jamie on 2017/7/12.
 */
public class DeleteFileParserHandler implements ParserHandler<FastTaskInfo, ByteBuffer> {
    public static final int SIZE = 15;
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFileParserHandler.class);

    @Override
    public long handleMetaData(FastTaskInfo con, ByteBuffer nioData) {
        byte[] group_name = new byte[16];
        nioData.position(0);
        nioData.get(group_name, 0, 16);
        return con.length - SIZE;
    }

    @Override
    public void handle(FastTaskInfo con, ByteBuffer nioData) {


    }

    @Override
    public boolean handleEnd(FastTaskInfo con, ByteBuffer nioData) {
        StorageDio.queuePush(con);
        return false;
    }

    @Override
    public int getSize() {
        return SIZE;
    }
}
