package io.mybear.storage.parserHandler;

import io.mybear.storage.storageNio.StorageClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static io.mybear.common.constants.TrackerProto.FDFS_PROTO_CMD_ACTIVE_TEST;

/**
 * Created by jamie on 2017/7/12.
 * FDFS_PROTO_CMD_ACTIVE_TEST
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
        assert (false);
        con.close(String.format("cmd=%d, client ip: %s, package size %d is not correct, expect length 0", FDFS_PROTO_CMD_ACTIVE_TEST, con.getHost(), con.getLength()));
    }

    @Override
    public void handleEnd(StorageClientInfo con, ByteBuffer nioData) {
        assert (false);
        con.close(String.format("cmd=%d, client ip: %s, package size %d is not correct, expect length 0", FDFS_PROTO_CMD_ACTIVE_TEST, con.getHost(), con.getLength()));
        return;
    }

    @Override
    public int getSize() {
        return SIZE;
    }
}
