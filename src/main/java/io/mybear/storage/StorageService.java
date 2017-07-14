package io.mybear.storage;

import com.alibaba.fastjson.JSON;
import io.mybear.common.*;
import io.mybear.common.constants.CommonConstant;
import io.mybear.common.constants.SizeOfConstant;
import io.mybear.common.utils.Base64;
import io.mybear.common.utils.HashUtil;
import io.mybear.common.utils.RandomUtil;
import io.mybear.net2.ByteBufferArray;
import io.mybear.storage.trunkMgr.TrunkShared;
import io.mybear.tracker.FdfsSharedFunc;
import io.mybear.tracker.TrackerTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import static io.mybear.common.utils.BasicTypeConversionUtil.*;

/**
 * Created by jamie on 2017/6/21.
 */
public class StorageService {
    private final static Logger log = LoggerFactory.getLogger(StorageService.class);
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
    private static final ReentrantLock lock = new ReentrantLock();
    //文件路径分隔符
    public static final String FILE_SEPARATOR = File.separator;

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
        //                Path file = Paths.get("d:/" + Integer.valueOf(rand.nextInt()).toString().substring(1, 4) +
        // ".jar");
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

    /**
     * 获取文件名
     *
     * @param pClientInfo
     * @param fileSize
     * @param crc32
     * @param fileName
     * @return
     */
    public static int storageGetFilename(StorageClientInfo pClientInfo, int startTime, long fileSize,
                                         int crc32, char[] szFormattedExt, char[] fileName, char[] fullFilename) {

        int fileNameLen;
        int storePathIndex = ((StorageUploadInfo)pClientInfo.getFileContext().getExtraInfo()).getTrunkInfo().getPath()
            .getStorePathIndex();
        String filePathName = null;
        for (int i = 0; i < 10; i++) {
            if ((fileNameLen = storageGenFilename(pClientInfo, fileSize, crc32, szFormattedExt, startTime, fileName))
                < 0) {
                return fileNameLen;
            }
            filePathName = String.format("%s/data/%s", TrunkShared.fdfsStorePaths.getPaths()[storePathIndex], fileName);
            if (!FdfsSharedFunc.fileExists(filePathName)) {
                System.arraycopy(filePathName, 0, fullFilename, 0, filePathName.length());
                break;
            }
            filePathName = "";
        }
        if (filePathName == null || "".equals(filePathName)) {
            log.error("method={},params={},result={}", "storageGetFilename", storePathIndex + " " + fileName,
                "Can't generate uniq filename");
            return -1;
        }
        return filePathName.length();
    }

    /**
     * 生成文件名 storage_service#storage_gen_filename
     *
     * @param pClientInfo
     * @param fileSize
     * @param crc32
     * @return
     */
    public static int storageGenFilename(StorageClientInfo pClientInfo, long fileSize,
                                         int crc32, char[] szFormattedExt, int timeStamp, char[] fileName) {
        try {
            int fileNameLen;
            char[] buff = new char[SizeOfConstant.SIZE_OF_INT * 5];
            char[] encoded = new char[SizeOfConstant.SIZE_OF_INT * 8 + 1];
            long maskedFileSize = 0L;
            StorageUploadInfo storageUploadInfo = (StorageUploadInfo)pClientInfo.getFileContext().getExtraInfo();
            FdfsTrunkFullInfo pTrunkInfo = storageUploadInfo.getTrunkInfo();
            //@TODO 这里需要做 g_server_id_in_filename的取值 和 htonl的转换
            //int2buff(htonl(g_server_id_in_filename),buff);
            int2buff(0, buff);
            int2buff(timeStamp, buff, SizeOfConstant.SIZE_OF_INT);
            if ((fileSize >> 32) != 0) {
                maskedFileSize = fileSize;
            } else {
                maskedFileSize = combineRandFileSize(fileSize, maskedFileSize);
            }
            long2buff(maskedFileSize, buff, SizeOfConstant.SIZE_OF_INT * 2);
            int2buff(crc32, buff, SizeOfConstant.SIZE_OF_INT * 4);
            //需要定义一个全局的Base64Context
            fileNameLen = Base64.base64EncodeEx(TrunkShared.base64Context, buff, SizeOfConstant.SIZE_OF_INT * 5,
                encoded, false);
            if (fileNameLen < 0) {
                return fileNameLen;
            }
            if (!storageUploadInfo.isIfSubPathAlloced()) {
                storageGetStorePath(encoded, fileNameLen, pTrunkInfo.getPath());
                storageUploadInfo.setIfSubPathAlloced(true);
            }
            char[] fileNewName = (String.format("%02X", 12) + FILE_SEPARATOR + String.format("%02X", 13)
                + FILE_SEPARATOR).toCharArray();
            int fileLen = fileNewName.length;
            int flag = 0;
            if (fileNameLen > encoded.length) {
                flag = fileNewName.length;
                fileNewName = Arrays.copyOf(fileNewName, flag + encoded.length);
                System.arraycopy(encoded, 0, fileNewName, flag, encoded.length);
            } else {
                flag = fileNewName.length;
                fileNewName = Arrays.copyOf(fileNewName, flag + fileNameLen);
                System.arraycopy(encoded, 0, fileNewName, flag, fileNameLen);
                int len = FdfsGlobal.FDFS_FILE_EXT_NAME_MAX_LEN + 1;
                if (szFormattedExt.length > len) {
                    fileNewName = Arrays.copyOf(fileNewName, flag + fileNameLen + len);
                    System.arraycopy(szFormattedExt, 0, fileNewName, flag + fileNameLen, len);
                } else {
                    fileNewName = Arrays.copyOf(fileNewName, flag + fileNameLen + szFormattedExt.length);
                    System.arraycopy(szFormattedExt, 0, fileNewName, flag + fileNameLen, szFormattedExt.length);
                }
            }
            fileNameLen += fileLen + FdfsGlobal.FDFS_FILE_EXT_NAME_MAX_LEN + 1;
            System.arraycopy(fileNewName, 0, fileName, 0, fileNewName.length);
            return fileNameLen;
        } catch (Exception e) {
            log.error(CommonConstant.LOG_FORMAT, "storageGenFilename",
                "pFileContext:" + JSON.toJSONString(pClientInfo) + " fileSize "
                    + fileSize + " crc32 " + crc32 + " szFormattedExt " + szFormattedExt
                    + " timeStamp " + timeStamp + "fileName" + fileName, "e{}" + e);
            return -1;
        }
    }

    private static long combineRandFileSize(long size, long maskedFileSize) {

        int r = (RandomUtil.randomFixedInt() & 0x007FFFFF) | 0x80000000;
        maskedFileSize = (((long)r) << 32) | size;
        return maskedFileSize;
    }

    /**
     * 获取文件路径
     *
     * @param filename
     * @param fileNameLen
     * @param trunkPathInfo
     */
    private static void storageGetStorePath(char[] filename, int fileNameLen, FDFSTrunkPathInfo trunkPathInfo) {
        int n;
        if (StorageGlobal.g_file_distribute_path_mode == TrackerTypes.FDFS_FILE_DIST_PATH_ROUND_ROBIN) {
            trunkPathInfo.setSubPathHigh(StorageGlobal.g_dist_path_index_high);
            trunkPathInfo.setSubPathLow(StorageGlobal.g_dist_path_index_low);
            if (++StorageGlobal.g_dist_write_file_count >= StorageGlobal.g_file_distribute_rotate_count) {
                StorageGlobal.g_dist_write_file_count = 0;
                lock.lock();
                try {
                    ++StorageGlobal.g_dist_path_index_low;
                    if (StorageGlobal.g_dist_path_index_low >= StorageGlobal.g_subdir_count_per_path) {  //rotate
                        StorageGlobal.g_dist_path_index_high++;
                        if (StorageGlobal.g_dist_path_index_high >= StorageGlobal.g_subdir_count_per_path)  //rotate
                        {
                            StorageGlobal.g_dist_path_index_high = 0;
                        }
                        StorageGlobal.g_dist_path_index_low = 0;
                    }
                    ++StorageGlobal.g_stat_change_count;
                } finally {
                    lock.unlock();
                }
            }
        }  //random
        else {
            n = HashUtil.PJWHash(filename, fileNameLen) % (1 << 16);
            trunkPathInfo.setSubPathHigh(((n >> 8) & 0xFF) % StorageGlobal.g_subdir_count_per_path);
            trunkPathInfo.setSubPathLow((n & 0xFF) % StorageGlobal.g_subdir_count_per_path);
        }
    }

    public static void main(String[] args) {
        int c = 200;
        System.out.println(String.format("%02X", c));
    }
}
