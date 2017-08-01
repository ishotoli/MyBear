package io.mybear.storage.parserHandler;


import io.mybear.common.SharedFunc;
import io.mybear.common.constants.config.StorageGlobal;
import io.mybear.storage.StorageDio;
import io.mybear.storage.StorageFileContext;
import io.mybear.storage.storageNio.StorageClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import static io.mybear.common.constants.TrackerProto.TRACKER_PROTO_CMD_RESP;


/**
 * Created by jamie on 2017/7/12.
 */
public class DownloadFileParserHandler implements ParserHandler<StorageClientInfo, ByteBuffer> {
    public static final int SIZE = 32;
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadFileParserHandler.class);

    static void sendDownloadFileHead(StorageClientInfo c, ByteBuffer data) {
        c.write(data);
    }

    static void sendDownloadFileData(StorageClientInfo c) {
        c.flagData = Boolean.TRUE;
        c.enableWrite(false);
    }

    @Override
    public long handleMetaData(StorageClientInfo con, ByteBuffer nioData) {
        long offset = nioData.getLong(0);
        long downloadFileLength = nioData.getLong(8);
        nioData.position(16);
        byte[] bytes = new byte[16];
        nioData.get(bytes);
        con.fileContext = new StorageFileContext();
        con.fileContext.filenameBuffer = new StringBuilder();
        con.fileContext.end = downloadFileLength;
        con.fileContext.offset = offset;
        return 0;
    }

    @Override
    public void handle(StorageClientInfo con, ByteBuffer nioData) {
        byte[] bytes = new byte[nioData.position()];
        nioData.flip();
        nioData.get(bytes);
        String s = new String(bytes);
        con.fileContext.filenameBuffer.append(s);
    }

    /**
     * @param
     * @param nioData
     * @return
     */
    @Override
    public void handleEnd(StorageClientInfo c, ByteBuffer nioData) {
        byte[] bytes = new byte[nioData.position()];
        nioData.flip();
        nioData.get(bytes);
        String s = new String(bytes);
        c.fileContext.filenameBuffer.append(s);
        c.fileContext.filename = c.fileContext.filenameBuffer.substring(StorageGlobal.g_group_name.length(), c.fileContext.filenameBuffer.length());
        c.fileContext.filename = StorageGlobal.BASE_PATH + c.fileContext.filename;
        if (!SharedFunc.fileExists(c.fileContext.filename)) {
            LOGGER.error("没有这个文件:" + c.fileContext.filename);
            return;
        }
        c.fileContext.dioExecutorService = StorageDio.getThreadIndex(c, 0, 0);
        c.dealFunc = (con) -> {
            try {
                ByteBuffer header;
                if (con.fileContext.fileChannel == null) {//还没打开文件
                    File file = new File(con.fileContext.filename);
                    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                    con.fileContext.randomAccessFile = randomAccessFile;
                    con.fileContext.fileChannel = randomAccessFile.getChannel();
                    //   con.fileContext.fileChannel = FileChannel.open(Paths.get(con.fileContext.filename), StandardOpenOption.READ);
                    long size = con.fileContext.end = con.fileContext.fileChannel.size();
                    header = con.getMyBufferPool().allocateByteBuffer().putLong(size).put(TRACKER_PROTO_CMD_RESP).put((byte) 0);
//                    con.fileContext.done_callback = (co) -> {
//                        co.disableWrite();
//                    };
                    con.dealFunc = StorageDio::dio_read_file;
                    sendDownloadFileHead(con, header);
                    return 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        };
        c.toDownload();
        StorageDio.queuePush(c);
    }

    @Override
    public int getSize() {
        return SIZE;
    }
}
