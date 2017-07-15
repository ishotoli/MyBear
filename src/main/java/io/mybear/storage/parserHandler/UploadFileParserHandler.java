package io.mybear.storage.parserHandler;

import io.mybear.common.StorageFileContext;
import io.mybear.common.StorageUploadInfo;
import io.mybear.storage.StorageDio;
import io.mybear.storage.storageNio.FastTaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ThreadLocalRandom;

import static org.csource.fastdfs.ProtoCommon.TRACKER_PROTO_CMD_RESP;

/**
 * Created by jamie on 2017/7/12.
 * STORAGE_PROTO_CMD_UPLOAD_FILE
 */
public class UploadFileParserHandler implements ParserHandler<FastTaskInfo, ByteBuffer> {
    public static final int SIZE = 15;
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFileParserHandler.class);

    static void setGroupName(ByteBuffer byteBuffer, String name) {
        ByteBuffer groupName = ByteBuffer.allocate(16);
        groupName.put(name.getBytes(StandardCharsets.US_ASCII));
        groupName.position(0).limit(16);
        byteBuffer.put(groupName);
    }

    static void setStorageCMDResp(ByteBuffer byteBuffer) {
        byteBuffer.put(8, TRACKER_PROTO_CMD_RESP);
    }

    @Override
    public long handleMetaData(FastTaskInfo con, ByteBuffer nioData) {
        byte cmd = con.cmd;
        StorageUploadInfo uploadInfo = new StorageUploadInfo();
        con.file_context = new StorageFileContext();
        con.file_context.extra_info = uploadInfo;
        long fileSize = nioData.getLong(1);
        con.file_context.end = fileSize;
        byte[] extName = new byte[6];
        nioData.position(9);
        nioData.get(extName, 0, 6);//6
//        uploadInfo.file_ext_name = extName;
//        uploadInfo.trunk_info = new FDFSTrunkFullInfo();
//        uploadInfo.trunk_info.setStorePathIndex(nioData.get(0));
//        uploadInfo.se = fileSize;
        con.file_context.dioExecutorService = StorageDio.g_dio_contexts[0];
        Path file = Paths.get("d:/" + Integer.valueOf(ThreadLocalRandom.current().nextInt()).toString().substring(1, 5) + ".jar");
        con.file_context.filename = file;
        con.file_context.openFlags = StandardOpenOption.APPEND;
        con.deal_func = StorageDio::dio_write_file;
        con.file_context.done_callback = (c) -> {
            try {
                ByteBuffer res;
                res = ByteBuffer.allocate(1000);
                res.clear();
                res.position(8);
                setStorageCMDResp(res);
                res.position(9);
                res.put((byte) 0);
                setGroupName(res, "Hello");
                setGroupName(res, "ll");
                int limit = res.position();
                int pkgLen = limit - 10;
                res.position(0);
                res.putLong(0, pkgLen);
                res.position(limit);
                res.flip();
                c.writeBuffer = res;
                while (c.getChannel().write(res) > 0) ;
                LOGGER.info("写入完成");
                c.needInit = true;
                c.enableRead();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // System.out.println(res.position());
            return 0;
        };
        return fileSize;
    }

    @Override
    public void handle(FastTaskInfo con, ByteBuffer nioData) {
        ParserHandler.debug(nioData);
    }

    @Override
    public boolean handleEnd(FastTaskInfo con, ByteBuffer nioData) {
        //放进去dio
        StorageDio.queuePush(con);
        return true;
    }

    @Override
    public int getSize() {
        return SIZE;
    }
}
