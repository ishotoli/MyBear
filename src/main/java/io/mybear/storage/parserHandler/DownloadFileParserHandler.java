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

    @Override
    public boolean handleEnd(FastTaskInfo con, ByteBuffer nioData) {
        ParserHandler.debug(nioData);
        byte[] bytes = new byte[nioData.position()];
        nioData.flip();
        nioData.get(bytes);
        con.context.append(new String(bytes));
        String filename = con.context.toString();
        System.out.println(filename);
        try {
            con.file_context.filename = Paths.get(System.getProperty("user.dir") + "/lib/fastdfs-client-java-1.27-SNAPSHOT.jar");
            con.file_context.fileChannel = FileChannel.open(con.file_context.filename, StandardOpenOption.READ);
            long size = con.file_context.end = con.file_context.fileChannel.size();
            nioData.clear();
            ByteBuffer byteBuffer = nioData;
            ByteBuffer header = byteBuffer.putLong(size).put(TRACKER_PROTO_CMD_RESP).put((byte) 0);
            header.flip();
            while (con.getChannel().write(byteBuffer) > 0) ;
            con.deal_func = StorageDio::dio_read_file;
            StorageDio.queuePush(con);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public int getSize() {
        return SIZE;
    }
}
