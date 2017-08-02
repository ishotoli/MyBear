package io.mybear.storage.storageService;

import com.alibaba.fastjson.JSON;
import io.mybear.common.constants.CommonConstant;
import io.mybear.common.constants.SizeOfConstant;
import io.mybear.common.constants.config.FdfsGlobal;
import io.mybear.common.constants.config.StorageGlobal;
import io.mybear.common.trunk.FdfsTrunkFullInfo;
import io.mybear.common.trunk.FdfsTrunkPathInfo;
import io.mybear.common.trunk.TrunkShared;
import io.mybear.common.utils.Base64;
import io.mybear.common.utils.HashUtil;
import io.mybear.common.utils.RandomUtil;
import io.mybear.common.utils.SharedFunc;
import io.mybear.storage.StorageUploadInfo;
import io.mybear.storage.storageNio.StorageClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import static io.mybear.common.constants.CommonConstant.FDFS_FILE_DIST_PATH_ROUND_ROBIN;
import static io.mybear.common.utils.BasicTypeConversionUtil.int2buff;
import static io.mybear.common.utils.BasicTypeConversionUtil.long2buff;
import static io.mybear.storage.storageService.StorageService.FILE_SEPARATOR;

/**
 * Created by jamie on 2017/8/2.
 */
public class GenFilenameUtil {
    private final static Logger log = LoggerFactory.getLogger(GenFilenameUtil.class);
    private static final ReentrantLock lock = new ReentrantLock();

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

    /**
     * 获取文件路径
     *
     * @param filename
     * @param fileNameLen
     * @param trunkPathInfo
     */
    private static void storageGetStorePath(char[] filename, int fileNameLen, FdfsTrunkPathInfo trunkPathInfo) {
        int n;
        if (StorageGlobal.g_file_distribute_path_mode == FDFS_FILE_DIST_PATH_ROUND_ROBIN) {
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
                    StorageGlobal.g_stat_change_count.increment();
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


    private static long combineRandFileSize(long size, long maskedFileSize) {

        int r = (RandomUtil.randomFixedInt() & 0x007FFFFF) | 0x80000000;
        maskedFileSize = (((long) r) << 32) | size;
        return maskedFileSize;
    }

}
