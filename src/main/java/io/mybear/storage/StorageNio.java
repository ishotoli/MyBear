package io.mybear.storage;

import io.mybear.common.FastTaskInfo;
import io.mybear.net2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

import static io.mybear.storage.Storage.setGroupName;
import static io.mybear.storage.Storage.setStorageCMDResp;
import static org.csource.fastdfs.ProtoCommon.*;

/**
 * Created by jamie on 2017/6/21.
 */
public class StorageNio implements NIOHandler<FastTaskInfo> {
    public static final int PORT = 23000;
    public static final StorageNio SINGLE = new StorageNio();
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageNio.class);
    Random rand = new Random();

    public static void main(String[] args) throws Exception {
        // Business Executor ，用来执行那些耗时的任务
        NameableExecutor businessExecutor = ExecutorUtil.create("BusinessExecutor", 10);
        // 定时器Executor，用来执行定时任务
        NamebleScheduledExecutor timerExecutor = ExecutorUtil.createSheduledExecute("Timer", 5);

        SharedBufferPool sharedPool = new SharedBufferPool(1024 * 1024 * 100, 1024);
        new NetSystem(sharedPool, businessExecutor, timerExecutor);
        // Reactor pool
        NIOReactorPool reactorPool = new NIOReactorPool("Reactor Pool", 5, sharedPool);
        NIOConnector connector = new NIOConnector("NIOConnector", reactorPool);
        connector.start();
        NetSystem.getInstance().setConnector(connector);
        NetSystem.getInstance().setNetConfig(new SystemConfig());


        ConnectionFactory frontFactory = new ConnectionFactory() {
            @Override
            protected Connection makeConnection(SocketChannel channel) throws IOException {
                return new FastTaskInfo(channel);
            }

            @Override
            protected NIOHandler getNIOHandler() {
                return SINGLE;
            }
        };
        NIOAcceptor server = new NIOAcceptor("Server", "127.0.0.1", PORT, frontFactory, reactorPool);
        server.start();
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
            case STORAGE_PROTO_CMD_DOWNLOAD_FILE:
            case STORAGE_PROTO_CMD_GET_METADATA:
            case STORAGE_PROTO_CMD_TRUNCATE_FILE:
            case STORAGE_PROTO_CMD_QUERY_FILE_INFO:

            case STORAGE_PROTO_CMD_UPLOAD_FILE: {
                int storePathIndex = nioData.get(0);
                long fileSize = nioData.getLong(1);
                int extName = (nioData.getChar(9) << 4) | (nioData.getChar(11) << 2) | (nioData.getChar(13));
                try {
//                    File file = new File("d:/" + Integer.valueOf(rand.nextInt()).toString().substring(1, 4) + ".jar");
//                    file.createNewFile();
//                    con.setFileChannel(FileChannel.open((file, StandardOpenOption.APPEND));
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