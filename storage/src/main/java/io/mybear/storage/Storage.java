//package io.mybear.storage;
//
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.net.InetSocketAddress;
//import java.nio.ByteBuffer;
//import java.nio.channels.*;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardOpenOption;
//import java.util.Iterator;
//import java.util.Random;
//
//import static io.mybear.common.constants.Proto.TRACKER_PROTO_CMD_RESP;
//import static org.csource.fastdfs.ProtoCommon.*;
//
//
//public class Storage {
//    private static final Logger logger = LoggerFactory.getLogger(Storage.class);
//
//    static ByteBuffer buildHeader(long len, byte cmd, int state) {
//        return ByteBuffer.allocate(90000).putLong(len).put(cmd).put((byte) state);
//    }
//
//    static void setGroupName(ByteBuffer byteBuffer, String name) {
//        ByteBuffer groupName = ByteBuffer.allocate(16);
//        groupName.put(name.getBytes(StandardCharsets.US_ASCII));
//        groupName.position(0).limit(16);
//        byteBuffer.put(groupName);
//    }
//
//    static void setStorageCMDResp(ByteBuffer byteBuffer) {
//        byteBuffer.put(8, TRACKER_PROTO_CMD_RESP);
//    }
//
//    static String getGroupName(ByteBuffer byteBuffer) {
//        byte[] bytes = new byte[16];
//        byteBuffer.get(bytes);
//        return new String(bytes);
//    }
//
//    public static void main(String args[]) throws Exception {
//        Selector selector = Selector.open();
//        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
//        serverSocketChannel.configureBlocking(false);
//        InetSocketAddress address = new InetSocketAddress(23000);
//        serverSocketChannel.socket().bind(address);
//        System.out.println("started at " + address);
//        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
//        ByteBuffer byteBuffer = ByteBuffer.allocate(900000);
//        String resIp = "127.0.0.1";
//        String resGroupName = "Hello";
//        int resPort = 23000;
//        int resIndex = 1;
//        Random rand = new Random();
//        while (true) {
//            selector.select();
//            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
//            while (iter.hasNext()) {
//                SelectionKey selectedKey = iter.next();
//                if ((selectedKey.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
//                    ServerSocketChannel serverChannel = (ServerSocketChannel) selectedKey.channel();
//                    SocketChannel socketChannel = serverChannel.accept();
//                    socketChannel.configureBlocking(false);
//                    socketChannel.register(selector, SelectionKey.OP_READ);
//                } else if ((selectedKey.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
//                    System.out.println("read");
//                    SocketChannel socketChannel = (SocketChannel) selectedKey.channel();
//                    byteBuffer.clear();
//                    socketChannel.read(byteBuffer);
//                    ByteBuffer res = null;
//                    switch (byteBuffer.get(8)) {
//                        case STORAGE_PROTO_CMD_UPLOAD_FILE: {
//                            long bodyLen = byteBuffer.getLong(0);
//                            int storePathIndex = byteBuffer.get(10);
//                            long fileSize = byteBuffer.getLong(11);
//                            long extName = byteBuffer.getLong(19) >> 8;//6
//                            Path file = Paths.get("d:/" + Integer.valueOf(rand.nextInt()).toString().substring(1, 4) + ".jar");
//                            Files.createFile(file);
//                            ByteChannel byteChannel = Files.newByteChannel(file, StandardOpenOption.APPEND);
//                            byteBuffer.limit(byteBuffer.position());
//                            byteBuffer.position(25);
//                            byteChannel.write(byteBuffer);
//                            byteBuffer.clear();
//                            while (socketChannel.read(byteBuffer) > 0) {
//                                byteBuffer.flip();
//                                byteChannel.write(byteBuffer);
//                                byteBuffer.clear();
//                            }
//                            byteBuffer.clear();
//                            byteChannel.close();
//                            res = byteBuffer;
//                            res.position(8);
//                            setStorageCMDResp(res);
//                            res.position(9);
//                            res.put((byte) 0);
//                            setGroupName(res, "Hello");
//                            setGroupName(res, file.toString());
//                            int limit = res.position();
//                            int pkgLen = limit - 10;
//                            res.position(0);
//                            res.putLong(0, pkgLen);
//                            res.position(limit);
//                            System.out.println(res.position());
//                            selectedKey.attach(res);
//                            break;
//                        }
//                        case STORAGE_PROTO_CMD_DOWNLOAD_FILE: {
//                            byteBuffer.position(0);
//                            long pkglen = byteBuffer.getLong(0);
//                            byteBuffer.position(10);
//                            long offset = byteBuffer.getLong(10);
//                            byteBuffer.position(18);
//                            long downloadFileLength = byteBuffer.getLong(18);
//                            byteBuffer.position(26);
//                            String groupName = getGroupName(byteBuffer);
//                            String fileName = getGroupName(byteBuffer);//暂定，之后需要修改
//                            ByteBuffer header = buildHeader(downloadFileLength, TRACKER_PROTO_CMD_RESP, 0);
//                            header.flip();
//                            socketChannel.write(header);
//                            byteBuffer.limit((int) downloadFileLength);
//                            ByteChannel byteChannel = Files.newByteChannel(Paths.get(System.getProperty("user.dir") + "/lib/fastdfs-client-java-1.27-SNAPSHOT.jar"), StandardOpenOption.READ);
//                            byteChannel.read(byteBuffer);
//                            byteBuffer.flip();
//                            while (socketChannel.write(byteBuffer) > 0) ;
//                            byteBuffer.clear();
//                            byteChannel.close();
//                            break;
//                        }
//                        case STORAGE_PROTO_CMD_DELETE_FILE: {
//                            byteBuffer.position(0);
//                            long pkgLen = byteBuffer.getLong(0);
//                            byteBuffer.position(10);
//                            String groupName = getGroupName(byteBuffer);
//                            String fileName = getGroupName(byteBuffer);//之后还要修改;
//                            logger.info("pkgLen:%s;groupName:%s;fileName:%s", pkgLen, groupName, fileName);
//                            ByteBuffer header = buildHeader(0, TRACKER_PROTO_CMD_RESP, 0);
//                            header.flip();
//                            while (socketChannel.write(header) > 0) ;
//                            break;
//                        }
//                        case STORAGE_PROTO_CMD_SET_METADATA: {
//                            byteBuffer.position(10);
//                            long length = byteBuffer.getLong(10);
//                            byteBuffer.position(18);
//                            long metaBufferLength = byteBuffer.getLong(18);
//                            byteBuffer.position(26);
//                            switch (byteBuffer.get(26)) {
//                                case STORAGE_SET_METADATA_FLAG_OVERWRITE: {
//
//                                    break;
//                                }
//                                default:
//                                    break;
//                            }
//                            byteBuffer.position(27);
//                            String groupName = getGroupName(byteBuffer);
//                            String fileName = getGroupName(byteBuffer);//之后还要修改;
//                            //metaBuffer 暂时不接收
//                            ByteBuffer header = buildHeader(0, TRACKER_PROTO_CMD_RESP, 0);
//                            header.flip();
//                            while (socketChannel.write(header) > 0) ;
//                            break;
//                        }
//                        case STORAGE_PROTO_CMD_GET_METADATA: {
//                            break;
//                        }
//                        case STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE: {
//                            long bodyLen = byteBuffer.getLong(0);
//                            int storePathIndex = byteBuffer.get(10);
//                            long fileSize = byteBuffer.getLong(11);
//                            long extName = byteBuffer.getLong(19) >> 8;//6
//                            Path file = Paths.get("d:/" + Integer.valueOf(rand.nextInt()).toString().substring(1, 4) + ".jar");
//                            Files.createFile(file);
//                            ByteChannel byteChannel = Files.newByteChannel(file, StandardOpenOption.APPEND);
//                            byteBuffer.limit(byteBuffer.position());
//                            byteBuffer.position(25);
//                            byteChannel.write(byteBuffer);
//                            byteBuffer.clear();
//                            while (socketChannel.read(byteBuffer) > 0) {
//                                byteBuffer.flip();
//                                byteChannel.write(byteBuffer);
//                                byteBuffer.clear();
//                            }
//                            byteBuffer.clear();
//                            byteChannel.close();
//                            res = byteBuffer;
//                            res.position(8);
//                            setStorageCMDResp(res);
//                            res.position(9);
//                            res.put((byte) 0);
//                            setGroupName(res, "Hello");
//                            setGroupName(res, file.toString());
//                            int limit = res.position();
//                            int pkgLen = limit - 10;
//                            res.position(0);
//                            res.putLong(0, pkgLen);
//                            res.position(limit);
//                            System.out.println(res.position());
//                            selectedKey.attach(res);
//                            break;
//                        }
//                        case STORAGE_PROTO_CMD_QUERY_FILE_INFO: {
//                            break;
//                        }
//                        /** 创建一个支持断点续传的文件 */
//                        case STORAGE_PROTO_CMD_UPLOAD_APPENDER_FILE: {
//                            break;
//                        } // create
//                        // appender
//                        // file
//                        /** 对文件断点续传(附加在原来为上传完全的文件后面) */
//                        case STORAGE_PROTO_CMD_APPEND_FILE: {
//                            break;
//                        } // append file
//                        /** 修改支持断点续传的文件 */
//                        case STORAGE_PROTO_CMD_MODIFY_FILE: {
//                            break;
//                        } // modify
//                        // appender
//                        // file
//                        /** 清空支持断点的文件 */
//                        case STORAGE_PROTO_CMD_TRUNCATE_FILE: {
//                            break;
//                        } // truncate
//                        // appender
//                        // file
//                        default: {
//                            break;
//                        }
//                    }
//                    if (res != null) {
//                        res.flip();
//                        selectedKey.attach(res);
//                        selectedKey.interestOps((selectedKey.interestOps() & ~SelectionKey.OP_READ) | SelectionKey.OP_WRITE);
//                    }
//                } else if ((selectedKey.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
//                    System.out.println("write");
//                    ByteBuffer buffer = (ByteBuffer) selectedKey.attachment();
//                    SocketChannel socketChannel = (SocketChannel) selectedKey.channel();
//                    if (buffer != null) {
//                        int writed = socketChannel.write(buffer);
//                        System.out.println("writed " + writed);
//                        if (buffer.hasRemaining()) {
//                            System.out.println(" not write finished ,bind to session ,remains " + buffer.remaining());
//                            buffer.flip();
//                            selectedKey.attach(buffer);
//                            selectedKey.interestOps(selectedKey.interestOps() | SelectionKey.OP_WRITE);
//                        } else {
//                            System.out.println(" block write finished ");
//                            selectedKey.attach(null);
//                            selectedKey.interestOps((selectedKey.interestOps() & ~SelectionKey.OP_WRITE) | SelectionKey.OP_READ);
//                        }
//                    }
//
//                }
//                iter.remove();
//            }
//
//        }
//    }
//}