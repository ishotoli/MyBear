package io.mybear.storage;

import io.mybear.common.FastTaskInfo;
import io.mybear.net2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.csource.fastdfs.ProtoCommon.*;

/**
 * Created by jamie on 2017/6/21.
 */
public class StorageNio implements NIOHandler<FastTaskInfo> {
    public static final int PORT = 23000;
    public static final StorageNio SINGLE = new StorageNio();
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageNio.class);

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
    public void handle(FastTaskInfo con, ByteBufferArray nioData) {
        //处理粘包
        byte cmd = con.cmd;
        long totalPacketSize = con.getOffset();
        long lenght = con.getLength();
        switch (cmd) {
            case FDFS_PROTO_CMD_ACTIVE_TEST:
            case STORAGE_PROTO_CMD_DELETE_FILE:
            case STORAGE_PROTO_CMD_SET_METADATA:
            case STORAGE_PROTO_CMD_DOWNLOAD_FILE:
            case STORAGE_PROTO_CMD_GET_METADATA:
            case STORAGE_PROTO_CMD_TRUNCATE_FILE:
            case STORAGE_PROTO_CMD_QUERY_FILE_INFO: {
                if (totalPacketSize < lenght) {
                    return;
                }
                switch (cmd) {
                    case FDFS_PROTO_CMD_ACTIVE_TEST:
                        StorageService.FDFS_PROTO_CMD_ACTIVE_TEST(con, nioData);
                        return;
                    case STORAGE_PROTO_CMD_DELETE_FILE:
                        StorageService.STORAGE_PROTO_CMD_DELETE_FILE(con, nioData);
                        return;
                    case STORAGE_PROTO_CMD_SET_METADATA:
                        StorageService.STORAGE_PROTO_CMD_SET_METADATA(con, nioData);
                        return;
                    case STORAGE_PROTO_CMD_DOWNLOAD_FILE:
                        StorageService.STORAGE_PROTO_CMD_SET_METADATA(con, nioData);
                        return;
                    case STORAGE_PROTO_CMD_GET_METADATA:
                        StorageService.STORAGE_PROTO_CMD_SET_METADATA(con, nioData);
                        return;
                    case STORAGE_PROTO_CMD_TRUNCATE_FILE:
                        StorageService.STORAGE_PROTO_CMD_SET_METADATA(con, nioData);
                        return;
                    case STORAGE_PROTO_CMD_QUERY_FILE_INFO: {
                        StorageService.STORAGE_PROTO_CMD_QUERY_FILE_INFO(con, nioData);
                        return;
                    }
                    default:
                        System.out.println(cmd);
                        break;
                }
            }
            case STORAGE_PROTO_CMD_UPLOAD_FILE: {
                if (totalPacketSize < 25) {
                    return;
                }
                StorageService.STORAGE_PROTO_CMD_UPLOAD_FILE(con, nioData);
                return;
            }
            case STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE: {
                if (totalPacketSize < 48) {
                    return;
                }
                StorageService.STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE(con, nioData);
                return;
            }

            case STORAGE_PROTO_CMD_UPLOAD_APPENDER_FILE: {
                if (totalPacketSize < 17) {
                    return;
                }
                StorageService.STORAGE_PROTO_CMD_UPLOAD_APPENDER_FILE(con, nioData);
                return;
            }
            case STORAGE_PROTO_CMD_APPEND_FILE: {
                if (totalPacketSize < 32) {
                    return;
                }
                StorageService.STORAGE_PROTO_CMD_APPEND_FILE(con, nioData);
                return;
            }
            case STORAGE_PROTO_CMD_MODIFY_FILE: {
                if (totalPacketSize < 32) {
                    return;
                }
                StorageService.STORAGE_PROTO_CMD_MODIFY_FILE(con, nioData);
                return;
            }
            default: {
                System.out.println(cmd);
                break;
            }

        }
    }

}