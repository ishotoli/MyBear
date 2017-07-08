package io.mybear.storage;

import io.mybear.common.FastTaskInfo;
import io.mybear.net2.ByteBufferArray;
import io.mybear.net2.Connection;
import io.mybear.net2.NIOHandler;
import io.mybear.net2.ReactorBufferPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ThreadLocalRandom;

import static io.mybear.net2.Connection.TaskType.Download;
import static io.mybear.net2.Connection.TaskType.Upload;
import static io.mybear.storage.Storage.setGroupName;
import static io.mybear.storage.Storage.setStorageCMDResp;
import static org.csource.fastdfs.ProtoCommon.*;

/**
 * Created by jamie on 2017/6/21.
 */
public class StorageNio implements NIOHandler<FastTaskInfo> {
    public static final int PORT = 23000;

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageNio.class);

    static String getGroupName(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[16];
        byteBuffer.get(bytes);
        return new String(bytes);
    }

    @Override
    public void onConnected(FastTaskInfo con) throws IOException {

    }

    @Override
    public void onConnectFailed(FastTaskInfo con, Throwable e) {

    }

    @Override
    public void onClosed(FastTaskInfo con, String reason) {
        System.out.println(reason);

    }

    @Override
    public void handle(FastTaskInfo con, ByteBuffer nioData) {
        byte cmd = con.cmd;
        switch (cmd) {
            case FDFS_PROTO_CMD_ACTIVE_TEST: {

            }
            case STORAGE_PROTO_CMD_DELETE_FILE:
            case STORAGE_PROTO_CMD_SET_METADATA:
            case STORAGE_PROTO_CMD_DOWNLOAD_FILE:
            case STORAGE_PROTO_CMD_GET_METADATA:
            case STORAGE_PROTO_CMD_TRUNCATE_FILE:
            case STORAGE_PROTO_CMD_QUERY_FILE_INFO:

            case STORAGE_PROTO_CMD_UPLOAD_FILE: {

                break;

            }
            case STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE: {

            }

            case STORAGE_PROTO_CMD_UPLOAD_APPENDER_FILE: {

            }
            case STORAGE_PROTO_CMD_APPEND_FILE: {

            }
            case STORAGE_PROTO_CMD_MODIFY_FILE: {

            }
            default: {
                System.out.println(cmd);
                break;
            }

        }
    }

    @Override
    public void handleEnd(FastTaskInfo con, ByteBuffer nioData) {
        System.out.println("=> " + con.length);
        ReactorBufferPool pool = con.getMyBufferPool();
        ByteBufferArray byteBufferArray = pool.allocate();
        ByteBuffer res = byteBufferArray.addNewBuffer();
        res.position(8);
        setStorageCMDResp(res);
        res.position(9);
        res.put((byte) 0);
        setGroupName(res, "Hello");
        setGroupName(res, "d:/1112".toString());
        int limit = res.position();
        int pkgLen = limit - 10;
        res.position(0);
        res.putLong(0, pkgLen);
        res.position(limit);
        con.write(byteBufferArray);
        System.out.println("完全读取");
    }

    @Override
    public void handleMetaData(FastTaskInfo con, ByteBuffer nioData) {
        byte cmd = con.cmd;
        switch (cmd) {
            case FDFS_PROTO_CMD_ACTIVE_TEST: {

            }
            case STORAGE_PROTO_CMD_DELETE_FILE:
            case STORAGE_PROTO_CMD_SET_METADATA:
            case STORAGE_PROTO_CMD_DOWNLOAD_FILE: {
                nioData.position(0);
                long offset = nioData.getLong(0);
                nioData.position(8);
                long downloadFileLength = nioData.getLong(8);
                nioData.position(16);
                String groupName = getGroupName(nioData);
                // String fileName = getGroupName(nioData);//暂定，之后需要修改
                try {
                    con.downloadChannel = FileChannel.open(Paths.get(System.getProperty("user.dir") + "/lib/fastdfs-client-java-1.27-SNAPSHOT.jar"), StandardOpenOption.READ);
                    System.out.println("size: " + con.downloadChannel.size());
                    con.packetState = Connection.PacketState.packet;
                    con.setTaskType(Download);
                    long size = con.downloadChannel.size();
                    ByteBufferArray byteBufferArray = con.getMyBufferPool().allocate();
                    ByteBuffer byteBuffer = byteBufferArray.addNewBuffer();
                    ByteBuffer header = byteBuffer.putLong(size).put(TRACKER_PROTO_CMD_RESP).put((byte) 0);
                    con.write(byteBufferArray);
                    con.disableRead();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case STORAGE_PROTO_CMD_GET_METADATA:
            case STORAGE_PROTO_CMD_TRUNCATE_FILE:
            case STORAGE_PROTO_CMD_QUERY_FILE_INFO:

            case STORAGE_PROTO_CMD_UPLOAD_FILE: {
                try {
                    int storePathIndex = nioData.get(0);
                    long fileSize = nioData.getLong(1);
                    int extName = (nioData.getChar(9) << 4) | (nioData.getChar(11) << 2) | (nioData.getChar(13));
                    con.uploadFileSize = fileSize;
                    con.setTaskType(Upload);
                    String fileName = Long.valueOf(ThreadLocalRandom.current().nextLong()).toString();
                    Path path = Paths.get("d:/" + fileName + ".jar");
                    Files.createFile(path);
                    con.uploadChannel = FileChannel.open(path, StandardOpenOption.APPEND);
                    con.packetState = Connection.PacketState.packet;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            }
            case STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE: {

            }

            case STORAGE_PROTO_CMD_UPLOAD_APPENDER_FILE: {

            }
            case STORAGE_PROTO_CMD_APPEND_FILE: {

            }
            case STORAGE_PROTO_CMD_MODIFY_FILE: {

            }
            default: {
                System.out.println(cmd);
                break;
            }

        }
    }
}