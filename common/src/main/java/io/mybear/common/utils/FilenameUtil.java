package io.mybear.common.utils;

import io.mybear.common.constants.CommonConstant;
import io.mybear.common.constants.SizeOfConstant;
import io.mybear.common.trunk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;

import static io.mybear.common.constants.CommonConstant.*;
import static io.mybear.common.constants.config.FdfsGlobal.FDFS_FILE_EXT_NAME_MAX_LEN;
import static io.mybear.common.trunk.TrunkShared.*;
import static io.mybear.common.utils.BasicTypeConversionUtil.buff2int;
import static io.mybear.common.utils.BasicTypeConversionUtil.buff2long;
import static io.mybear.common.utils.Utils.FDFS_TRUNK_FILE_TRUE_SIZE;
import static io.mybear.common.utils.Utils.IS_TRUNK_FILE;

/**
 * Created by jamie on 2017/7/31.
 */
public class FilenameUtil {
    public static final Logger log = LoggerFactory.getLogger(FilenameUtil.class);

    /**
     * 文件夹的范围是 0~9 和 A~F
     *
     * @param ch
     * @return
     */
    public static boolean IS_UPPER_HEX(char ch) {

        return (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F');
    }

    /**
     * 获取文件名：返回结果类似 CD/00/wKi0hVjqXGeAcyFfAAGSt-FxG-0872.jpg
     *
     * @param logic_filename
     * @param filename_len
     * @param true_filename
     * @return
     */
    public static int storage_split_filename_ex(char[] logic_filename, int filename_len, char[] true_filename) {
        return SPLIT_FILENAME_BODY(logic_filename, filename_len, true_filename, true);
    }

    /**
     * 调用这个函数成功返回之后需要filename_len的需要-4;
     * 如：
     * if(SPLIT_FILENAME_BODY() >= 0）{
     * filename_len -= 4;
     * }
     *
     * @param logic_filename
     * @param filename_len
     * @param true_filename
     * @param check_path_index
     * @return
     */
    private static int SPLIT_FILENAME_BODY(char[] logic_filename, int filename_len, char[] true_filename, boolean check_path_index) {
        String fileName = new String(logic_filename);
        int store_path_index;
        char[] buff = new char[2];
        if (filename_len <= FDFS_LOGIC_FILE_PATH_LEN) {
            log.error(CommonConstant.LOG_FORMAT, "SPLIT_FILENAME_BODY", "", String.format("filename_len: %d is invalid, <= %d", filename_len, FDFS_LOGIC_FILE_PATH_LEN));
            return -1;
        }
        if (logic_filename[0] != FDFS_STORAGE_STORE_PATH_PREFIX_CHAR) { /* version < V1.12 */
            System.arraycopy(logic_filename, 0, true_filename, 0, filename_len + 1);
            return 0;
        }
        if (logic_filename[3] != '/') {
            log.error(CommonConstant.LOG_FORMAT, "SPLIT_FILENAME_BODY", "", String.format("filename: %s is invalid", fileName));
            return -1;
        }
        buff[0] = logic_filename[1];
        buff[1] = logic_filename[2];
        try {
            store_path_index = Integer.parseInt(new String(buff), 16);
        } catch (Exception e) {
            log.error(CommonConstant.LOG_FORMAT, "SPLIT_FILENAME_BODY", "", String.format("filename: %s is invalid", fileName));
            return -1;
        }
        if (check_path_index && (store_path_index < 0 || store_path_index >= 1)) {
            log.error(CommonConstant.LOG_FORMAT, "SPLIT_FILENAME_BODY", "", String.format("filename: %s is invalid invalid store path index: %d", fileName, store_path_index));
            return -1;
        }
        if (logic_filename.length < filename_len) {
            log.error(CommonConstant.LOG_FORMAT, "SPLIT_FILENAME_BODY", "", String.format("filename: %s is invalid invalid filename_len index: %d", fileName, filename_len));
        }
        System.arraycopy(logic_filename, 4, true_filename, 0, filename_len - 4);
        return store_path_index;
    }

    public static File trunk_file_lstat(int store_path_index, char[] true_filename, int filename_len, File pStat,
                                        FdfsTrunkFullInfo pTrunkInfo, FDFSTrunkHeader pTrunkHeader) {
        return trunk_file_do_lstat_func(store_path_index, true_filename, filename_len,
                FDFS_STAT_FUNC_LSTAT, pStat, pTrunkInfo, pTrunkHeader, null);
    }

    private static File trunk_file_do_lstat_func(int store_path_index, char[] true_filename, int filename_len, int stat_func,
                                                 File pStat, FdfsTrunkFullInfo pTrunkInfo, FDFSTrunkHeader pTrunkHeader, Integer pfd) {
        return trunk_file_do_lstat_func_ex(TrunkShared.getFdfsStorePaths(), store_path_index, true_filename, filename_len, stat_func, pStat, pTrunkInfo, pTrunkHeader, pfd);
    }

    private static File trunk_file_do_lstat_func_ex(FdfsStorePaths pStorePaths, int store_path_index, char[] true_filename,
                                                    int filename_len, int stat_func, File pStat, FdfsTrunkFullInfo pTrunkInfo,
                                                    FDFSTrunkHeader pTrunkHeader, Integer pfd) {

        char[] full_filename = new char[MAX_PATH_SIZE];
        char[] buff = new char[128];
        char[] pack_buff = new char[FDFS_TRUNK_FILE_HEADER_SIZE];
        int file_size;
        int buff_len;
        int fd;
        int read_bytes;
        int result;
        int flag = 0;
        pTrunkInfo.getFile().setId(0);
        //not trunk file
        String fileName = String.format("%s/data/%s", pStorePaths.getPaths()[store_path_index], new String(true_filename));
        if (filename_len != FDFS_TRUNK_FILENAME_LENGTH) {
            if (fileName.length() > MAX_PATH_SIZE) {
                flag = MAX_PATH_SIZE;
                System.arraycopy(fileName.toCharArray(), 0, full_filename, 0, MAX_PATH_SIZE);
            } else {
                flag = fileName.length();
                System.arraycopy(fileName.toCharArray(), 0, full_filename, 0, fileName.length());
            }
            if (stat_func == FDFS_STAT_FUNC_STAT) {
                pStat = new File(new String(full_filename, 0, flag));
            } else {
                // @TODO lstat 和 stat是否需要进行区分？？？
                pStat = new File(new String(full_filename, 0, flag));
            }
        }
        Arrays.fill(buff, (char) 0);
        char[] src = new char[true_filename.length - FDFS_TRUE_FILE_PATH_LEN];
        System.arraycopy(true_filename, FDFS_TRUE_FILE_PATH_LEN, src, 0, true_filename.length - FDFS_TRUE_FILE_PATH_LEN);
        buff = Base64.base64_decode_auto(TrunkShared.base64Context, src, FDFS_FILENAME_BASE64_LENGTH, buff);
        buff_len = buff.length;
        file_size = (int) buff2long(buff, SizeOfConstant.SIZE_OF_INT * 2);
        //slave file
        if (!IS_TRUNK_FILE(file_size)) {
            if (fileName.length() > MAX_PATH_SIZE) {
                flag = MAX_PATH_SIZE;
                System.arraycopy(fileName.toCharArray(), 0, full_filename, 0, MAX_PATH_SIZE);
            } else {
                flag = fileName.length();
                System.arraycopy(fileName.toCharArray(), 0, full_filename, 0, fileName.length());
            }
            if (stat_func == FDFS_STAT_FUNC_STAT) {
                pStat = new File(new String(full_filename, 0, flag));
            } else {
                // @TODO lstat 和 stat是否需要进行区分？？？
                pStat = new File(new String(full_filename, 0, flag));
            }
        }
        src = new char[true_filename.length - FDFS_TRUE_FILE_PATH_LEN - FDFS_FILENAME_BASE64_LENGTH];
        System.arraycopy(true_filename, FDFS_TRUE_FILE_PATH_LEN + FDFS_FILENAME_BASE64_LENGTH,
                src, 0, true_filename.length - FDFS_TRUE_FILE_PATH_LEN - FDFS_FILENAME_BASE64_LENGTH);
        trunk_file_info_decode(src, pTrunkInfo.getFile());
        pTrunkHeader.fileSize = FDFS_TRUNK_FILE_TRUE_SIZE(file_size);
        pTrunkHeader.mtime = buff2int(buff, SizeOfConstant.SIZE_OF_INT);
        pTrunkHeader.crc32 = buff2int(buff, SizeOfConstant.SIZE_OF_INT * 4);
        //文件扩展名
        int srcLength = true_filename.length - (filename_len - (FDFS_FILE_EXT_NAME_MAX_LEN + 1));
        int destLength = FDFS_FILE_EXT_NAME_MAX_LEN + 2;
        if (srcLength > destLength) {
            char[] tmpFile = new char[destLength];
            System.arraycopy(true_filename, filename_len - (FDFS_FILE_EXT_NAME_MAX_LEN + 1), tmpFile, 0, destLength);
            pTrunkHeader.formattedExtName = tmpFile;
        } else {
            char[] tmpFile = new char[srcLength];
            System.arraycopy(true_filename, filename_len - (FDFS_FILE_EXT_NAME_MAX_LEN + 1), tmpFile, 0, srcLength);
            pTrunkHeader.formattedExtName = tmpFile;
        }
        pTrunkHeader.allocSize = pTrunkInfo.getFile().getSize();
        pTrunkInfo.getPath().setStorePathIndex(store_path_index);
        pTrunkInfo.getPath().setSubPathHigh(Integer.parseInt(new String(true_filename, 0, 2), 16));
        pTrunkInfo.getPath().setSubPathLow(Integer.parseInt(new String(true_filename, 3, 2), 16));
        //文件全名
        full_filename = trunk_get_full_filename_ex(pStorePaths, pTrunkInfo, full_filename, full_filename.length);
        return null;
    }

    public static char[] trunk_get_full_filename_ex(FdfsStorePaths pStorePaths, FdfsTrunkFullInfo pTrunkInfo,
                                                    char[] full_filename, int length) {

        String short_filename;
        String pStorePath;

        pStorePath = pStorePaths.getPaths()[pTrunkInfo.getPath().getStorePathIndex()];
        short_filename = TRUNK_GET_FILENAME(pTrunkInfo.getFile().getId());
        String fullFileName = String.format("%s/data/" + FDFS_STORAGE_DATA_DIR_FORMAT + "/" + FDFS_STORAGE_DATA_DIR_FORMAT + "/%s",
                pStorePath, pTrunkInfo.getPath().getSubPathHigh(),
                pTrunkInfo.getPath().getSubPathLow(),
                short_filename);
        if (fullFileName.length() > length) {
            System.arraycopy(fullFileName.toCharArray(), 0, full_filename, 0, length);
            return full_filename;
        } else {
            char[] tmp = new char[fullFileName.length()];
            System.arraycopy(fullFileName.toCharArray(), 0, tmp, 0, fullFileName.length());
            return tmp;
        }
    }

    public static String TRUNK_GET_FILENAME(int file_id) {
        return String.format("%06d", file_id);
    }

    private static void trunk_file_info_decode(final char[] src, FdfsTrunkFileInfo pTrunkFile) {
        char[] buff = new char[FDFS_TRUNK_FILE_INFO_LEN];
        buff = Base64.base64_decode_auto(TrunkShared.base64Context, src, FDFS_TRUNK_FILE_INFO_LEN, buff);
        pTrunkFile.setId(buff2int(buff));
        pTrunkFile.setOffset(buff2int(buff, SizeOfConstant.SIZE_OF_INT));
        pTrunkFile.setSize(buff2int(buff, SizeOfConstant.SIZE_OF_INT * 2));
    }

    public static void main(String[] args) {
        char[] logic_filename = "M00/CD/00/wKi0hVjqXGeAcyFfAAGSt-FxG-0872.jpg".toCharArray();
        int filename_len = logic_filename.length;
        System.out.println(filename_len);
        char[] true_filename = new char[128];
        SPLIT_FILENAME_BODY(logic_filename, filename_len, true_filename, true);
        System.out.println(true_filename);
    }
}
