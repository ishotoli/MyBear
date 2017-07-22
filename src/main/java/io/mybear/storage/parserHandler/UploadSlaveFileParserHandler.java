package io.mybear.storage.parserHandler;

import io.mybear.common.FdfsTrunkFullInfo;
import io.mybear.common.FdfsTrunkPathInfo;
import io.mybear.common.StorageFileContext;
import io.mybear.common.StorageUploadInfo;
import io.mybear.storage.StorageDio;
import io.mybear.storage.storageNio.StorageClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by jamie on 2017/7/12.
 */
public class UploadSlaveFileParserHandler implements ParserHandler<StorageClientInfo, ByteBuffer> {
    public static final int SIZE = 38;
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadSlaveFileParserHandler.class);

    @Override
    public long handleMetaData(StorageClientInfo con, ByteBuffer nioData) {
        StorageUploadInfo uploadInfo = new StorageUploadInfo();
        con.fileContext = new StorageFileContext();
        con.fileContext.extra_info = uploadInfo;
        con.fileContext.dioExecutorService = StorageDio.getThreadIndex(con, 0, 0);
        FdfsTrunkFullInfo trunkFullInfo = new FdfsTrunkFullInfo();
        uploadInfo.setTrunkInfo(trunkFullInfo);
        FdfsTrunkPathInfo pathInfo = new FdfsTrunkPathInfo();
        trunkFullInfo.setPath(pathInfo);
        long master_filename_len = nioData.getLong(0);
        long file_size = nioData.getLong(8);
        byte[] prefix = new byte[16];
        nioData.position(16);
        nioData.get(prefix, 0, 6);
        System.out.println(new String(prefix));
        nioData.position(32);
        byte[] bytes = new byte[6];
        nioData.get(bytes);
        System.out.println(new String(bytes));
        return file_size;
    }

    @Override
    public void handle(StorageClientInfo con, ByteBuffer nioData) {
        byte[] bytes = new byte[nioData.position()];
        nioData.get(bytes);
        con.fileContext.filename += new String(bytes);

    }

    @Override
    public void handleEnd(StorageClientInfo con, ByteBuffer nioData) {
        byte[] bytes = new byte[nioData.position()];
        nioData.flip();
        nioData.get(bytes);
        con.fileContext.filename += new String(bytes);
        System.out.println(con.fileContext.filename);
        StorageDio.queuePush(con);
    }

    @Override
    public int getSize() {
        return SIZE;
    }
}
