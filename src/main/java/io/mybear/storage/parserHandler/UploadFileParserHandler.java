package io.mybear.storage.parserHandler;


import io.mybear.common.*;
import io.mybear.storage.StorageDio;
import io.mybear.storage.storageNio.Connection;
import io.mybear.storage.storageNio.StorageClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.CRC32;

import static io.mybear.storage.StorageService.FILE_SEPARATOR;
import static org.csource.fastdfs.ProtoCommon.TRACKER_PROTO_CMD_RESP;


/**
 * Created by jamie on 2017/7/12.
 * <p>
 * STORAGE_PROTO_CMD_UPLOAD_FILE
 */

public class UploadFileParserHandler implements ParserHandler<StorageClientInfo, ByteBuffer> {

    public static final int SIZE = 15;

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFileParserHandler.class);
    static RandomAccessFile memoryMappedFile;
    static CRC32 crc32 = new CRC32();
    static int i = 0;

    static void setGroupName(ByteBuffer byteBuffer, String name) {

        ByteBuffer groupName = ByteBuffer.allocate(16);

        groupName.put(name.getBytes(StandardCharsets.US_ASCII));

        groupName.position(0).limit(16);

        byteBuffer.put(groupName);

    }

    static void setStorageCMDResp(ByteBuffer byteBuffer) {

        byteBuffer.put(8, TRACKER_PROTO_CMD_RESP);

    }

    public static void genFileName(int fileNameLen, char[] encoded, char[] formattedExtName) {
        //测试文件名
        char[] fileName = (String.format("%02X", 12) + FILE_SEPARATOR + String.format("%02X", 13) + FILE_SEPARATOR).toCharArray();
        int fileLen = fileName.length;
        int flag = 0;
        if (fileNameLen > encoded.length) {
            flag = fileName.length;
            fileName = Arrays.copyOf(fileName, flag + encoded.length);
            System.arraycopy(encoded, 0, fileName, flag, encoded.length);
        } else {
            flag = fileName.length;
            fileName = Arrays.copyOf(fileName, flag + fileNameLen);
            System.arraycopy(encoded, 0, fileName, flag, fileNameLen);
            int len = FdfsGlobal.FDFS_FILE_EXT_NAME_MAX_LEN + 1;
            if (formattedExtName.length > len) {
                fileName = Arrays.copyOf(fileName, flag + fileNameLen + len);
                System.arraycopy(formattedExtName, 0, fileName, flag + fileNameLen, len);
            } else {
                fileName = Arrays.copyOf(fileName, flag + fileNameLen + formattedExtName.length);
                System.arraycopy(formattedExtName, 0, fileName, flag + fileNameLen, formattedExtName.length);
            }
        }
        System.out.println(fileName);
    }

    static MessageDigest getMD5CTX() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    //    static {
//        try {
//            RandomAccessFile memoryMappedFile = new RandomAccessFile("d:/sss" + "0", "rw");
//            out = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 64 * 1024);
//            md5 = MessageDigest.getInstance("MD5");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    /*
      * 字节数组转16进制字符串
      */
    public static String bytes2HexString(byte[] b) {
        String r = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            r += hex.toUpperCase();
        }
        return r;
    }

    public static void upload(Connection con, SocketChannel channel) {
        // ((StorageClientInfo)con).deal_func.apply((StorageClientInfo) con);
        StorageDio.queuePush(con);
    }

    /**
     * 在MappedByteBuffer释放后再对它进行读操作的话就会引发jvm crash，在并发情况下很容易发生
     * <p>
     * 正在释放时另一个线程正开始读取，于是crash就发生了。所以为了系统稳定性释放前一般需要检
     * <p>
     * 查是否还有线程在读或写
     *
     * @param mappedByteBuffer
     */

    public static void unmap(final MappedByteBuffer mappedByteBuffer) {
        try {
            if (mappedByteBuffer == null) {
                return;
            }
            mappedByteBuffer.force();
            AccessController.doPrivileged(new PrivilegedAction<Object>() {

                @Override

                @SuppressWarnings("restriction")

                public Object run() {
                    try {
                        Method getCleanerMethod = mappedByteBuffer.getClass().getMethod("cleaner", new Class[0]);
                        getCleanerMethod.setAccessible(true);
                        sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(mappedByteBuffer, new Object[0]);
                        cleaner.clean();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("clean MappedByteBuffer completed");
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override

    public long handleMetaData(StorageClientInfo con, ByteBuffer nioData) {
        byte cmd = con.getCmd();
        StorageUploadInfo uploadInfo = new StorageUploadInfo();
        con.fileContext = new StorageFileContext();
        con.fileContext.extra_info = uploadInfo;
        FdfsTrunkFullInfo trunkFullInfo = new FdfsTrunkFullInfo();
        uploadInfo.setTrunkInfo(trunkFullInfo);
        FDFSTrunkPathInfo pathInfo = new FDFSTrunkPathInfo();
        trunkFullInfo.setPath(pathInfo);
        uploadInfo.getTrunkInfo().getPath().setStorePathIndex(nioData.get(0));
        long fileSize = nioData.getLong(1);
        con.fileContext.end = fileSize;
        LOGGER.debug("fileSize:" + fileSize);
        char[] extName = uploadInfo.getFileExtName();
        nioData.position(9);
        byte[] bytes = new byte[6];
        nioData.get(bytes, 0, 6);
        // con.fileContext.dioExecutorService = StorageDio.getThreadIndex(con, nioData.get(0), 0);
        con.fileContext.dioExecutorService = StorageDio.g_dio_contexts[0];
        int crc32 = 123456;
        char[] name = new char[256];
        char[] fileName = new char[256];
        //StorageService.storageGetFilename(con, (int) TimeUtil.currentTimeMillis(), fileSize, crc32, new String(bytes).toCharArray(), name, fileName);
        con.fileContext.filename = Paths.get("d:/" + Integer.valueOf(ThreadLocalRandom.current().nextInt()).toString().substring(1, 5) + ".jar");
        con.fileContext.openFlags = StandardOpenOption.APPEND;
        con.fileContext.done_callback = (c) -> {
            ByteBuffer res;
            res = c.getMyBufferPool().allocateByteBuffer();
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
            c.write(res);
        };

        return fileSize;

    }

    @Override

    public void handle(StorageClientInfo con, ByteBuffer nioData) {
        nioData.position(0);
        // CharBuffer charBuffer = nioData.asCharBuffer();
        ParserHandler.debug(nioData);

    }

    @Override

    public void handleEnd(StorageClientInfo con, ByteBuffer nioData) {
        boolean calc_cra32 = true;
        con.dealFunc = StorageDio::dio_write_file;
        try {
            con.toUpload();
        } catch (IOException e) {
            e.printStackTrace();
        }
//                con.deal_func = (c) -> {
//                    try {
//                        ByteBuffer header;
//                        if (c.fileContext.fileChannel == null) {//还没打开文件
//
//                            LOGGER.info("建立文件开始");
//                            c.fileContext.fileChannel = new RandomAccessFile(con.fileContext.filename.toString(), "rw").getChannel();
//                            // Mapping a file into memory
//                            LOGGER.info("建立文件结束");
//                            out.clear();
//                            out.limit((int) c.fileContext.end % out.capacity());
//                            c.readBuffer = out;
//                        }
//                        c.deal_func = (conn) -> {
//                            if (!conn.getChannel().isOpen()) return 0;
//                            try {
//                                long got = 0;
//                                int pos;
//
//                                    int position = con.readBuffer.position();
//                                    pos = (int) position;
////                                    con.readBuffer.flip();
////                                    crc32.update(con.readBuffer);
//                                    con.readBuffer.position((int) position);
//                                    con.readBuffer.limit(con.readBuffer.capacity());
//                                    con.readBuffer.flip();
////                                    md5.update(con.readBuffer);
//                                    con.readBuffer.position((int) position);
//                                    con.readBuffer.limit(con.readBuffer.capacity());
//                                    con.readBuffer.flip();
//                                    ((StorageClientInfo) con).fileContext.fileChannel.write(con.readBuffer);
//                                    con.readBuffer.clear();
//                                long end = ((StorageClientInfo) con).fileContext.end;
//                                i++;
//                                //LOGGER.debug(Long.toUnsignedString(((StorageClientInfo) con).fileContext.offset));
//                                if (((StorageClientInfo) con).fileContext.offset >= end) {
//                                    ((StorageClientInfo) con).flagData = Boolean.FALSE;
//                                    LOGGER.debug(String.valueOf(pos));
//                                    LOGGER.debug("CRC32:" + crc32.getValue());
//                                    LOGGER.debug("MD5:" + bytes2HexString(md5.digest()));
//                                    con.fileContext.fileChannel.close();
//                                    con.readBuffer.clear();
//                                    LOGGER.debug("{}", i);
//                                    //  ((StorageClientInfo) con).fileContext.fileChannel.close();
//                                    crc32.reset();
//                                    md5.reset();
//                                    con.readBuffer = null;
//                                    ((StorageClientInfo) con).fileContext.done_callback.apply(con);
//                                    c.deal_func = null;
//                                } else {
//                                    ((StorageClientInfo) con).flagData = Boolean.TRUE;
//                                }
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            return 0;
//                        };
//                        StorageDio.queuePush(con);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return 0;
//                };


    }

    @Override
    public int getSize() {
        return SIZE;
    }
}