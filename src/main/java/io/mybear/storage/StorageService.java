package io.mybear.storage;

import io.mybear.common.FastTaskInfo;
import io.mybear.net2.ByteBufferArray;

import java.nio.file.Path;

/**
 * Created by jamie on 2017/6/21.
 */
public class StorageService {
    public static final String ACCESS_LOG_ACTION_UPLOAD_FILE = "upload";
    public static final String ACCESS_LOG_ACTION_DOWNLOAD_FILE = "download";
    public static final String ACCESS_LOG_ACTION_DELETE_FILE = "delete";
    public static final String ACCESS_LOG_ACTION_GET_METADATA = "get_metadata";
    public static final String ACCESS_LOG_ACTION_SET_METADATA = "set_metadata";
    public static final String ACCESS_LOG_ACTION_MODIFY_FILE = "modify";
    public static final String ACCESS_LOG_ACTION_APPEND_FILE = "append";
    public static final String ACCESS_LOG_ACTION_TRUNCATE_FILE = "truncate";
    public static final String ACCESS_LOG_ACTION_QUERY_FILE = "status";
    static boolean h = false;

    public static void STORAGE_nio_notify(FastTaskInfo pTask) {

    }

    public static void STORAGE_accept_loop(int server_sock) {

    }

    public static void FDFS_PROTO_CMD_ACTIVE_TEST(FastTaskInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_DELETE_FILE(FastTaskInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_SET_METADATA(FastTaskInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_DOWNLOAD_FILE(FastTaskInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_GET_METADATA(FastTaskInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_TRUNCATE_FILE(FastTaskInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_QUERY_FILE_INFO(FastTaskInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_UPLOAD_FILE(FastTaskInfo taskInfo, ByteBufferArray byteBufferArray) {
//        long len = taskInfo.getOffset();
//        System.out.println("len:" + len);
//        System.out.println("taskInfo:" + taskInfo.getLength());
//
//        if (taskInfo.getLength() < (len)) {
//            if (h == false) {
//                h = true;
//            } else {
//                return;
//            }
//
//            try {
//                Random rand = new Random();
//                Path file = Paths.get("d:/" + Integer.valueOf(rand.nextInt()).toString().substring(1, 4) + ".jar");
//                Files.createFile(file);
//                ByteChannel byteChannel = Files.newByteChannel(file, StandardOpenOption.APPEND);
//                ByteBuffer byteBuffer = byteBufferArray.getBlock(0);
//                byteBuffer.limit(byteBuffer.position());
//
//                byteBuffer.position(25);
//                byteChannel.write(byteBuffer);
//                for (int i = 1; i < byteBufferArray.getBlockCount(); i++) {
//                    byteBuffer = byteBufferArray.getBlock(i);
//                    byteBuffer.flip();
//                    byteChannel.write(byteBuffer);
//                }
//                byteChannel.close();
//
//////                ByteBufferArray r = taskInfo.getMyBufferPool().allocate();
////                ByteBuffer res = r.addNewBuffer();
////                res.clear();
////                res.position(8);
////                setStorageCMDResp(res);
////                res.position(9);
////                res.put((byte) 0);
////                setGroupName(res, "Hello");
////                setGroupName(res, file.toString());
////                int limit = res.position();
////                int pkgLen = limit - 10;
////                res.position(0);
////                res.putLong(0, pkgLen);
////                res.position(limit);
////                taskInfo.write(r);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    public static void STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE(FastTaskInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_UPLOAD_APPENDER_FILE(FastTaskInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_APPEND_FILE(FastTaskInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_MODIFY_FILE(FastTaskInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    int storageServiceInit() {
        return 0;
    }

    void storageServiceDestroy() {

    }

    int fdfsStatFileSyncFunc(Object args) {
        return 0;
    }

    int storageDealTask(FastTaskInfo pTask) {
        return 0;
    }

    int storageTerminateThreads() {
        return 0;
    }

    int storageGetStoragePathIndex(int[] store_path_index) {
        return 0;
    }

    void storageGetStorePath(final Path filename, final int filename_len, int[] sub_path_high, int[] sub_path_low) {

    }

}
