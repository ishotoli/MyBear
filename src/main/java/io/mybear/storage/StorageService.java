package io.mybear.storage;

import com.alibaba.fastjson.JSON;
import io.mybear.common.*;
import io.mybear.common.constants.CommonConstant;
import io.mybear.common.constants.SizeOfConstant;
import io.mybear.common.utils.Base64;
import io.mybear.common.utils.HashUtil;
import io.mybear.common.utils.RandomUtil;
import io.mybear.storage.storageNio.ByteBufferArray;
import io.mybear.storage.storageNio.FastTaskInfo;
import io.mybear.storage.storageNio.StorageClientInfo;
import io.mybear.storage.trunkMgr.FDFSTrunkHeader;
import io.mybear.storage.trunkMgr.TrunkShared;
import io.mybear.tracker.SharedFunc;
import io.mybear.tracker.TrackerTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import static io.mybear.common.utils.BasicTypeConversionUtil.int2buff;
import static io.mybear.common.utils.BasicTypeConversionUtil.long2buff;


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
    //文件路径分隔符
    public static final String FILE_SEPARATOR = "/";
    public static final int STORAGE_CREATE_FLAG_NONE = 0;
    public static final int STORAGE_CREATE_FLAG_FILE = 1;
    public static final int STORAGE_CREATE_FLAG_LINK = 2;
    public static final int STORAGE_DELETE_FLAG_NONE = 0;
    public static final int STORAGE_DELETE_FLAG_FILE = 1;
    public static final int STORAGE_DELETE_FLAG_LINK = 2;
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);
    private final static Logger log = LoggerFactory.getLogger(StorageService.class);
    private static final ReentrantLock lock = new ReentrantLock();

    public static void STORAGE_nio_notify(FastTaskInfo pTask) {

    }

    public static void STORAGE_accept_loop(int server_sock) {

    }

    public static void FDFS_PROTO_CMD_ACTIVE_TEST(FastTaskInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_DELETE_FILE(FastTaskInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

//    public static void storageProtoCmdSetMetadata(StorageClientInfo clientInfo) {
//        StorageFileContext fileContext = clientInfo.fileContext;
//        StorageSetMetaInfo setmeta = (StorageSetMetaInfo) clientInfo.extraArg;
//        List<String[]> list = MetadataUtil.splitMetadata(setmeta.metaBuff);
//        StringBuilder stringBuilder = new StringBuilder();
//        fileContext.syncFlag = '\0';
//        StringBuilder metaBuff = setmeta.metaBuff;
//        int metaBytes = setmeta.meta_bytes;
//        int result = 0;
//        try {
//            do {
//                if (setmeta.op_flag == STORAGE_SET_METADATA_FLAG_OVERWRITE) {
//                    if (metaBuff.length() == 0) {
//                        if (!SharedFunc.fileExists(fileContext.filename)) {
//                            result = 0;
//                            break;
//                        }
//                        fileContext.syncFlag = StorageSync.STORAGE_OP_TYPE_SOURCE_DELETE_FILE;
//                        if (!SharedFunc.delete(fileContext.filename)) {
//                            LOGGER.error("client ip: %s, delete file %s fail", clientInfo.getChannel().getRemoteAddress(), fileContext.filename);
//                            result = -1;
//                        } else {
//                            result = 0;
//                        }
//                        break;
//                    }
//
//                    if ((result = storage_sort_metadata_buff(meta_buff, \
//                            meta_bytes)) != 0) {
//                        break;
//                    }
//
//                    if (fileExists(pFileContext -> filename)) {
//                        pFileContext -> sync_flag = STORAGE_OP_TYPE_SOURCE_UPDATE_FILE;
//                    } else {
//                        pFileContext -> sync_flag = STORAGE_OP_TYPE_SOURCE_CREATE_FILE;
//                    }
//
//                    result = writeToFile(pFileContext -> filename, meta_buff, meta_bytes);
//                    break;
//                }
//
//                if (meta_bytes == 0) {
//                    result = 0;
//                    break;
//                }
//
//                result = getFileContent(pFileContext -> filename, & file_buff, &file_bytes);
//                if (result == ENOENT) {
//                    if (meta_bytes == 0) {
//                        result = 0;
//                        break;
//                    }
//
//                    if ((result = storage_sort_metadata_buff(meta_buff, \
//                            meta_bytes)) != 0) {
//                        break;
//                    }
//
//                    pFileContext -> sync_flag = STORAGE_OP_TYPE_SOURCE_CREATE_FILE;
//                    result = writeToFile(pFileContext -> filename, meta_buff, meta_bytes);
//                    break;
//                } else if (result != 0) {
//                    break;
//                }
//
//                old_meta_list = fdfs_split_metadata(file_buff, & old_meta_count, &result);
//                if (old_meta_list == NULL) {
//                    free(file_buff);
//                    break;
//                }
//
//                new_meta_list = fdfs_split_metadata(meta_buff, & new_meta_count, &result);
//                if (new_meta_list == NULL) {
//                    free(file_buff);
//                    free(old_meta_list);
//                    break;
//                }
//            } while (false);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }

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

    /**
     * 删除文件
     */
    public static void storageServerDeleteFile(FastTaskInfo pTask) {
        StorageClientInfo pClientInfo;
        StorageFileContext pFileContext;
        char[] p;
        FDFSTrunkHeader trunkHeader;
        char[] group_name = new char[TrackerTypes.FDFS_GROUP_NAME_MAX_LEN + 1];
        char[] true_filename = new char[128];
        char[] filename;
        int filename_len;
        int true_filename_len;
        File stat_buf;
        int result;
        int store_path_index;
        long nInPackLen;

        pClientInfo = (StorageClientInfo) pTask.arg;
        pFileContext = pClientInfo.fileContext;

        nInPackLen = pClientInfo.totalLength - SizeOfConstant.SIZE_OF_TRACKER_HEADER;
        pClientInfo.totalLength = SizeOfConstant.SIZE_OF_TRACKER_HEADER;
        pFileContext.deleteFlag = STORAGE_DELETE_FLAG_NONE;
        if (nInPackLen <= TrackerTypes.FDFS_GROUP_NAME_MAX_LEN) {
            log.error(CommonConstant.LOG_FORMAT, "storageServerDeleteFile", JSON.toJSONString(pTask),
                    "nInPackLen:" + nInPackLen + " < TrackerTypes.FDFS_GROUP_NAME_MAX_LEN "
                            + TrackerTypes.FDFS_GROUP_NAME_MAX_LEN);
            return;
        }
        if (nInPackLen >= pTask.size) {
            log.error(CommonConstant.LOG_FORMAT, "storageServerDeleteFile", JSON.toJSONString(pTask),
                    "nInPackLen:" + nInPackLen + " < pTask.size " + pTask.size);
            return;
        }
        if (TrackerTypes.FDFS_GROUP_NAME_MAX_LEN < SizeOfConstant.SIZE_OF_TRACKER_HEADER) {
            p = new char[pTask.data.length + SizeOfConstant.SIZE_OF_TRACKER_HEADER];
        } else {
            p = new char[pTask.data.length + TrackerTypes.FDFS_GROUP_NAME_MAX_LEN];
        }
        System.arraycopy(pTask.data, 0, p, 0, pTask.data.length);
        System.arraycopy(group_name, 0, p, pTask.data.length, TrackerTypes.FDFS_GROUP_NAME_MAX_LEN);
    }

    /**
     * 获取文件名
     *
     * @param pClientInfo
     * @param fileSize
     * @param crc32
     * @param
     * @return
     */
    public static String storageGetFilename(StorageClientInfo pClientInfo, int startTime, long fileSize, int crc32, char[] szFormattedExt) {
        int fileNameLen;
        int storePathIndex = ((StorageUploadInfo) pClientInfo.fileContext.extra_info).getTrunkInfo().getPath()
                .getStorePathIndex();
        String filePathName = null;
        for (int i = 0; i < 10; i++) {
            String fileName;
            if ("".equals(fileName = storageGenFilename(pClientInfo, fileSize, crc32, szFormattedExt, startTime))) {
                return "";
            }
            if (storePathIndex > TrunkShared.fdfsStorePaths.getCount()) {
                log.error("method={},params={},result={}", "storageGetFilename", storePathIndex + " " + fileName,
                        "storePathIndex的值错误");
                return "";
            }
            if (storePathIndex == TrunkShared.fdfsStorePaths.getCount()) {
                filePathName = String.format("%s/data/%s", TrunkShared.fdfsStorePaths.getPaths()[storePathIndex - 1], new String(fileName));
            }
            if (storePathIndex < TrunkShared.fdfsStorePaths.getCount()) {
                filePathName = String.format("%s/data/%s", TrunkShared.fdfsStorePaths.getPaths()[storePathIndex], new String(fileName));
            }
            if (!SharedFunc.fileExists(filePathName)) {

                break;
            }
            filePathName = "";
        }
        if (filePathName == null || "".equals(filePathName)) {
            log.error("method={},params={},result={}", "storageGetFilename", storePathIndex + " ",
                    "Can't generate uniq filename");
            return "";
        }
        return filePathName;
    }

    /**
     * 生成文件名 storage_service#storage_gen_filename
     *
     * @param pClientInfo
     * @param fileSize
     * @param crc32
     * @return
     */
    public static String storageGenFilename(StorageClientInfo pClientInfo, long fileSize,
                                            int crc32, char[] szFormattedExt, int timeStamp) {
        try {
            int fileNameLen;
            char[] buff = new char[SizeOfConstant.SIZE_OF_INT * 5];
            char[] encoded = new char[SizeOfConstant.SIZE_OF_INT * 8 + 1];
            long maskedFileSize = 0L;
            StorageUploadInfo storageUploadInfo = (StorageUploadInfo) pClientInfo.fileContext.extra_info;
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
                return "";
            }
            if (!storageUploadInfo.isIfSubPathAlloced()) {
                storageGetStorePath(encoded, fileNameLen, pTrunkInfo.getPath());
                storageUploadInfo.setIfSubPathAlloced(true);
            }
            char[] fileNewName = (String.format("%02X", pTrunkInfo.getPath().getSubPathHigh()) + FILE_SEPARATOR + String.format("%02X", pTrunkInfo.getPath().getSubPathLow())
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
            return new String(fileNewName);
        } catch (Exception e) {
            log.error(CommonConstant.LOG_FORMAT, "storageGenFilename",
                    "pFileContext:" + JSON.toJSONString(pClientInfo) + " fileSize "
                            + fileSize + " crc32 " + crc32 + " szFormattedExt " + szFormattedExt
                            + " timeStamp " + timeStamp, "e{}" + e);
            return "";
        }
    }

    private static long combineRandFileSize(long size, long maskedFileSize) {

        int r = (RandomUtil.randomFixedInt() & 0x007FFFFF) | 0x80000000;
        maskedFileSize = (((long) r) << 32) | size;
        return maskedFileSize;
    }

    /**
     * 获取文件路径
     *
     * @param filename
     * @param fileNameLen
     * @param trunkPathInfo
     */
    private static void storageGetStorePath(char[] filename, int fileNameLen, FdfsTrunkPathInfo trunkPathInfo) {
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
