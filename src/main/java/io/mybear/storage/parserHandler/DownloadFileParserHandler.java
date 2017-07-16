package io.mybear.storage.parserHandler;


import io.mybear.common.StorageFileContext;
import io.mybear.storage.StorageDio;
import io.mybear.storage.storageNio.FastTaskInfo;
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
public class DownloadFileParserHandler implements ParserHandler<FastTaskInfo, ByteBuffer> {
    public static final int SIZE = 32;
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadFileParserHandler.class);

    @Override
    public long handleMetaData(FastTaskInfo con, ByteBuffer nioData) {
        long offset = nioData.getLong(0);
        long downloadFileLength = nioData.getLong(8);
        nioData.position(16);
        byte[] bytes = new byte[16];
        nioData.get(bytes);
        System.out.println(new String(bytes));
        con.file_context = new StorageFileContext();
        con.context = new StringBuilder();
        return 0;
    }

    @Override
    public void handle(FastTaskInfo con, ByteBuffer nioData) {
        byte[] bytes = new byte[nioData.position()];
        nioData.flip();
        nioData.get(bytes);
        con.context.append(new String(bytes));

    }

    /**
     * @param
     * @param nioData
     * @return
     */
    @Override
    public void handleEnd(FastTaskInfo c, ByteBuffer nioData) {
        //ParserHandler.debug(nioData);
        byte[] bytes = new byte[nioData.position()];
        nioData.flip();
        nioData.get(bytes);
        c.context.append(new String(bytes));
        String filename = c.context.toString();
        System.out.println(filename);
        c.deal_func = (con) -> {
            try {
                ByteBuffer header;
                if (con.file_context.fileChannel == null) {//还没打开文件
                    con.file_context.filename = Paths.get(System.getProperty("user.dir") + "/lib/fastdfs-client-java-1.27-SNAPSHOT.jar");
                    con.file_context.fileChannel = FileChannel.open(con.file_context.filename, StandardOpenOption.READ);
                    long size = con.file_context.end = con.file_context.fileChannel.size();
                    header = con.getMyBufferPool().allocateByteBuffer().putLong(size).put(TRACKER_PROTO_CMD_RESP).put((byte) 0);
                    header.flip();
                    con.getChannel().write(header);
                    if (header.hasRemaining()) {
                        con.writeBuffer = header;
                    } else {
                        con.getMyBufferPool().recycle(header);
                        con.deal_func = StorageDio::dio_read_file;
                        StorageDio.queuePush(con);
                    }
                    return 0;
                }
                //极少可能会运行到这里
                header = (ByteBuffer) con.writeBuffer;
                con.getChannel().write(header);
                if (!header.hasRemaining()) {
                    con.getMyBufferPool().recycle(header);
                    con.deal_func = StorageDio::dio_read_file;
                    StorageDio.queuePush(con);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        };
        StorageDio.queuePush(c);
    }

    @Override
    public int getSize() {
        return SIZE;
    }
}
