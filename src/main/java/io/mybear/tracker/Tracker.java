package io.mybear.tracker;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static org.csource.fastdfs.ProtoCommon.*;


public class Tracker {

    static ByteBuffer buildHeader(long len, byte cmd, int state) {
        return ByteBuffer.allocate(90000).putLong(len).put(cmd).put((byte) state);
    }

    static void setIP(ByteBuffer byteBuffer, String ip) {
        ByteBuffer ipBuffer = ByteBuffer.allocate(15);
        ipBuffer.put(ip.getBytes(StandardCharsets.US_ASCII));
        ipBuffer.position(0).limit(15);
        byteBuffer.put(ipBuffer);
    }

    static String getIP(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[15];
        byteBuffer.get(bytes);
        return new String(bytes);
    }

    static void setGroupName(ByteBuffer byteBuffer, String name) {
        ByteBuffer groupName = ByteBuffer.allocate(16);
        groupName.put(name.getBytes(StandardCharsets.US_ASCII));
        groupName.position(0).limit(16);
        byteBuffer.put(groupName);
    }

    static String getGroupName(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[16];
        byteBuffer.get(bytes);
        return new String(bytes);
    }

    static void setPort(ByteBuffer byteBuffer, int port) {
        byteBuffer.putLong(port);
    }

    static void getPort(ByteBuffer byteBuffer, int port) {
        byteBuffer.getLong(port);
    }

    static void setStorePathIndex(ByteBuffer byteBuffer, int index) {
        byteBuffer.put((byte) index);
    }

    static int getStorePathIndex(ByteBuffer byteBuffer) {
        return byteBuffer.get();
    }

    public static void main(String args[]) throws Exception {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        InetSocketAddress address = new InetSocketAddress(22122);
        serverSocketChannel.socket().bind(address);
        System.out.println("started at " + address);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer byteBuffer = ByteBuffer.allocate(900000);
        String resIp = "127.0.0.1";
        String resGroupName = "Hello";
        int resPort = 23000;
        int resIndex = 0;
        while (true) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey selectedKey = iter.next();
                if (!selectedKey.isValid()) continue;
                if ((selectedKey.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                    ServerSocketChannel serverChannel = (ServerSocketChannel) selectedKey.channel();
                    SocketChannel socketChannel = serverChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if ((selectedKey.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                    System.out.println("read");
                    SocketChannel socketChannel = (SocketChannel) selectedKey.channel();
                    byteBuffer.clear();
                    socketChannel.read(byteBuffer);
                    ByteBuffer res = null;
                    switch (byteBuffer.get(8)) {
                        case TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE: {
                            res = buildHeader(40, TRACKER_PROTO_CMD_RESP, 0);
                            setGroupName(res, resGroupName);
                            setIP(res, resIp);
                            setPort(res, resPort);
                            setStorePathIndex(res, resIndex);
                            break;
                        }
                        case TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ONE: {
                            String groupName = getGroupName(byteBuffer);
                            res = buildHeader(40, TRACKER_PROTO_CMD_RESP, 0);
                            setIP(res, resIp);
                            setPort(res, resPort);
                            setStorePathIndex(res, resIndex);
                            break;
                        }
                        case TRACKER_PROTO_CMD_SERVER_LIST_GROUP: {
                            break;
                        }
                        case TRACKER_PROTO_CMD_SERVER_LIST_STORAGE: {
                            break;
                        }
                        case TRACKER_PROTO_CMD_SERVER_DELETE_STORAGE: {
                            break;
                        }
                        case TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ONE: {
                            /**
                             pkg format:
                             Header
                             FDFS_GROUP_NAME_MAX_LEN bytes: group_name
                             remain bytes: filename
                             **/
                            byteBuffer.position(0);
                            long pkglen = byteBuffer.getLong(0);
                            byteBuffer.position(10);
                            String groupName = getGroupName(byteBuffer);
                            String fileName = getGroupName(byteBuffer);//暂定，之后需要修改
                            res = buildHeader(0, TRACKER_PROTO_CMD_RESP, 0);
                            res.position(26);
                            setIP(res, "127.0.0.1");
                            setPort(res, 23000);
                            setIP(res, "127.0.0.2");
                            res.putLong(0, 54);
                            break;
                        }
                        case TRACKER_PROTO_CMD_SERVICE_QUERY_UPDATE: {
                            byteBuffer.position(0);
                            long pkglen = byteBuffer.getLong(0);
                            byteBuffer.position(10);
                            String groupName = getGroupName(byteBuffer);
                            String fileName = getGroupName(byteBuffer);//暂定，之后需要修改
                            res = buildHeader(0, TRACKER_PROTO_CMD_RESP, 0);
                            res.position(26);
                            setIP(res, "127.0.0.1");
                            setPort(res, 23000);
                            setIP(res, "127.0.0.2");
                            res.putLong(0, 54);
                            break;
                        }
                        case TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ALL: {
                            break;
                        }
                        case TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ALL: {
                            break;
                        }
                        case TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ALL: {
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                    if (res != null) {
                        res.flip();
                        selectedKey.attach(res);
                        selectedKey.interestOps((selectedKey.interestOps() & ~SelectionKey.OP_READ) | SelectionKey.OP_WRITE);
                    }
                } else if ((selectedKey.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
                    System.out.println("write");
                    ByteBuffer buffer = (ByteBuffer) selectedKey.attachment();
                    SocketChannel socketChannel = (SocketChannel) selectedKey.channel();
                    if (buffer != null) {
                        int writed = socketChannel.write(buffer);
                        System.out.println("writed " + writed);
                        if (buffer.hasRemaining()) {
                            System.out.println(" not write finished ,bind to session ,remains " + buffer.remaining());
                            buffer.flip();
                            selectedKey.attach(buffer);
                            selectedKey.interestOps(selectedKey.interestOps() | SelectionKey.OP_WRITE);
                        } else {
                            System.out.println(" block write finished ");
                            selectedKey.attach(null);
                            selectedKey.interestOps((selectedKey.interestOps() & ~SelectionKey.OP_WRITE) | SelectionKey.OP_READ);
                        }
                    }

                }
                iter.remove();
            }

        }
    }
}