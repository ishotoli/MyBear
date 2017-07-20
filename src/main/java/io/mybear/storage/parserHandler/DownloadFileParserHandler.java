package io.mybear.storage.parserHandler;


import io.mybear.common.StorageFileContext;
import io.mybear.storage.StorageDio;
import io.mybear.storage.storageNio.StorageClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.csource.fastdfs.ProtoCommon.TRACKER_PROTO_CMD_RESP;

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
        System.out.println(new String(bytes));
        con.fileContext = new StorageFileContext();
//        con.context = new StringBuilder();
        return 0;
    }

    @Override
    public void handle(StorageClientInfo con, ByteBuffer nioData) {
//        byte[] bytes = new byte[nioData.position()];
//        nioData.flip();
//        nioData.get(bytes);
//        con.context.append(new String(bytes));

    }

    /**
     * @param
     * @param nioData
     * @return
     */
    @Override
    public void handleEnd(StorageClientInfo c, ByteBuffer nioData) {
        //ParserHandler.debug(nioData);
        byte[] bytes = new byte[nioData.position()];
        nioData.flip();
        nioData.get(bytes);
//        c.context.append(new String(bytes));
//        String filename = c.context.toString();
//        System.out.println(filename);
//        c.packetState=downloadHead;
        c.dealFunc = (con) -> {
            try {
                ByteBuffer header;
                if (con.fileContext.fileChannel == null) {//还没打开文件
                    con.fileContext.filename = System.getProperty("user.dir") + "/lib/fastdfs-client-java-1.27-SNAPSHOT.jar";
                    con.fileContext.fileChannel = FileChannel.open(Paths.get(con.fileContext.filename), StandardOpenOption.READ);
                    long size = con.fileContext.end = con.fileContext.fileChannel.size();
                    header = con.getMyBufferPool().allocateByteBuffer().putLong(size).put(TRACKER_PROTO_CMD_RESP).put((byte) 0);
                    con.fileContext.done_callback = (co) -> {
                        co.disableWrite();
                    };
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
