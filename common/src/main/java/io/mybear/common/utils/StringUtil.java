package io.mybear.common.utils;

import io.mybear.common.Stat;
import io.mybear.common.constants.CommonConstant;
import io.mybear.common.constants.SizeOfConstant;
import io.mybear.common.trunk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
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
public class StringUtil {
    public static final Logger log = LoggerFactory.getLogger(StringUtil.class);


    /**
     * 文件夹的范围是 0~9 和 A~F
     *
     * @param ch
     * @return
     */
    public static boolean isUpperHex(char ch) {
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
     * 获取文件名：返回结果类似 CD/00/wKi0hVjqXGeAcyFfAAGSt-FxG-0872.jpg
     *
     * @return
     */
    public static FilenameResultEx storage_split_filename_ex(String logic_filename) {
        FilenameResultEx filenameResult = new FilenameResultEx();
        char[] true_filename = new char[128];
        filenameResult.storePathIndex = SPLIT_FILENAME_BODY(logic_filename.toCharArray(), logic_filename.length(), true_filename, true);
        filenameResult.true_filename = new String(true_filename);
        return filenameResult;
    }

    /**
     * storage_split_filename
     *
     * @return
     */
    public static FilenameResult storage_split_filename(String logic_filename) {
        FilenameResult filenameResult = new FilenameResult();
        char[] true_filename = new char[128];
        int index = SPLIT_FILENAME_BODY(logic_filename.toCharArray(), logic_filename.length(), true_filename, true);
        filenameResult.ppStorePath = TrunkShared.getFdfsStorePaths().getPaths()[index];
        filenameResult.true_filename = new String(true_filename);
        return filenameResult;
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
//            System.arraycopy(logic_filename, 0, true_filename, 0, filename_len + 1);
            System.arraycopy(logic_filename, 0, true_filename, 0, filename_len);
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

    public static int trunk_file_lstat(int store_path_index, char[] true_filename, int filename_len, Stat pStat,
                                       FdfsTrunkFullInfo pTrunkInfo, FDFSTrunkHeader pTrunkHeader) {
        return trunk_file_do_lstat_func(store_path_index, true_filename, filename_len,
                FDFS_STAT_FUNC_LSTAT, pStat, pTrunkInfo, pTrunkHeader, null);
    }

    private static int trunk_file_do_lstat_func(int store_path_index, char[] true_filename, int filename_len, int stat_func,
                                                Stat pStat, FdfsTrunkFullInfo pTrunkInfo, FDFSTrunkHeader pTrunkHeader, Integer pfd) {
        return trunk_file_do_lstat_func_ex(TrunkShared.getFdfsStorePaths(), store_path_index, true_filename, filename_len, stat_func, pStat, pTrunkInfo, pTrunkHeader, pfd);
    }

    private static int trunk_file_do_lstat_func_ex(FdfsStorePaths pStorePaths, int store_path_index, char[] true_filename,
                                                   int filename_len, int stat_func, Stat pStat, FdfsTrunkFullInfo pTrunkInfo,
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
        File tmpFile = null;
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
            tmpFile = new File(new String(full_filename, 0, flag));
            if (stat_func == FDFS_STAT_FUNC_STAT) {
                //判断文件是不是
                if (!tmpFile.isFile()) {
                    log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("这不是一个文件 !文件名%s", new String(full_filename)));
                    return -1;
                }
            } else {
                // @TODO lstat 和 stat是否需要进行区分？？？
                tmpFile = new File(new String(full_filename, 0, flag));
                if (!Files.isSymbolicLink(tmpFile.toPath())) {
                    log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("这不是一个软连接 !文件名%s", new String(full_filename)));
                    return -1;
                }
            }
        }
        Arrays.fill(buff, (char) 0);
        char[] src = new char[true_filename.length - FDFS_TRUE_FILE_PATH_LEN];
        System.arraycopy(true_filename, FDFS_TRUE_FILE_PATH_LEN, src, 0, true_filename.length - FDFS_TRUE_FILE_PATH_LEN);
        char[] buff_bak = Base64.base64_decode_auto(TrunkShared.base64Context, src, FDFS_FILENAME_BASE64_LENGTH, buff);
        buff_len = buff_bak.length;
        //文件的读写标记
        int flag_buff = 0;
        if (buff_len > 128) {
            flag_buff = 128;
            System.arraycopy(buff, 0, buff_bak, 0, buff.length);
        } else {
            flag_buff = buff_len;
            System.arraycopy(buff, 0, buff_bak, 0, buff_len);
        }
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
            tmpFile = new File(new String(full_filename, 0, flag));
            if (stat_func == FDFS_STAT_FUNC_STAT) {
                //判断文件是不是
                if (!tmpFile.isFile()) {
                    log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("这不是一个文件 !文件名%s", new String(full_filename)));
                    return -1;
                }
            } else {
                // @TODO lstat 和 stat是否需要进行区分？？？
                tmpFile = new File(new String(full_filename, 0, flag));
                if (!Files.isSymbolicLink(tmpFile.toPath())) {
                    log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("这不是一个软连接 !文件名%s", new String(full_filename)));
                    return -1;
                }
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
            char[] tmpFileChar = new char[destLength];
            System.arraycopy(true_filename, filename_len - (FDFS_FILE_EXT_NAME_MAX_LEN + 1), tmpFileChar, 0, destLength);
            pTrunkHeader.formattedExtName = tmpFileChar;
        } else {
            char[] tmpFileChar = new char[srcLength];
            System.arraycopy(true_filename, filename_len - (FDFS_FILE_EXT_NAME_MAX_LEN + 1), tmpFileChar, 0, srcLength);
            pTrunkHeader.formattedExtName = tmpFileChar;
        }
        pTrunkHeader.allocSize = pTrunkInfo.getFile().getSize();
        pTrunkInfo.getPath().setStorePathIndex(store_path_index);
        pTrunkInfo.getPath().setSubPathHigh(Integer.parseInt(new String(true_filename, 0, 2), 16));
        pTrunkInfo.getPath().setSubPathLow(Integer.parseInt(new String(true_filename, 3, 2), 16));
        //文件全名
        full_filename = trunk_get_full_filename_ex(pStorePaths, pTrunkInfo, full_filename, full_filename.length);
        String trunkFileName = new String(full_filename);
        RandomAccessFile randomAccessFile = null;
        try {
            //只读模式打开文件
            randomAccessFile = new RandomAccessFile(trunkFileName, "r");
            if (!randomAccessFile.getFD().valid()) {
                return -1;
            }
            randomAccessFile.seek(pTrunkInfo.getFile().getOffset());
            byte[] buff_byte = new byte[128];
            if (randomAccessFile.length() < FDFS_TRUNK_FILE_HEADER_SIZE) {
                log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("读取文件长度出现异常，文件长度小于 %d !文件名%s", FDFS_TRUNK_FILE_HEADER_SIZE, trunkFileName));
                return -1;
            }
            randomAccessFile.read(buff_byte, 0, FDFS_TRUNK_FILE_HEADER_SIZE);
            pTrunkHeader.fileType = buff_byte[FDFS_TRUNK_FILE_FILE_TYPE_OFFSET];
            if (pTrunkHeader.fileType == FDFS_TRUNK_FILE_TYPE_REGULAR) {
                pStat.setSt_mode(StatUtil.S_IFREG);
            } else if (pTrunkHeader.fileType == FDFS_TRUNK_FILE_TYPE_LINK) {
                pStat.setSt_mode(StatUtil.S_IFLNK);
            } else {
                log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("Invalid file type: %d", pTrunkHeader.fileType));
                return -1;
            }
            //包装trunkHeader
            trunk_pack_header(pTrunkHeader, pack_buff);
            //
            char[] compareChar = new char[pack_buff.length];
            System.arraycopy(buff, 0, compareChar, 0, compareChar.length);
            if (!Arrays.equals(pack_buff, buff)) {
                return -1;
            }
            pStat.setSt_size(pTrunkHeader.fileSize);
            pStat.setSt_mtime(pTrunkHeader.mtime);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("只读模式创建文件出现异常!文件名%s", trunkFileName));
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("读取文件跳过相应的字节数出现异常 文件名 %s seek %d!", "只读模式创建文件出现异常!文件名%s", trunkFileName, pTrunkInfo.getFile().getOffset()));
            return -1;
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    private static void trunk_pack_header(FDFSTrunkHeader pTrunkHeader, char[] buff) {
        buff[FDFS_TRUNK_FILE_FILE_TYPE_OFFSET] = (char) pTrunkHeader.fileType;
        BasicTypeConversionUtil.int2buff(pTrunkHeader.allocSize, buff, FDFS_TRUNK_FILE_ALLOC_SIZE_OFFSET);
        BasicTypeConversionUtil.int2buff(pTrunkHeader.fileSize, buff, FDFS_TRUNK_FILE_FILE_SIZE_OFFSET);
        BasicTypeConversionUtil.int2buff(pTrunkHeader.crc32, buff, FDFS_TRUNK_FILE_FILE_CRC32_OFFSET);
        BasicTypeConversionUtil.int2buff(pTrunkHeader.mtime, buff, FDFS_TRUNK_FILE_FILE_MTIME_OFFSET);
        System.arraycopy(pTrunkHeader.formattedExtName, 0, buff, FDFS_TRUNK_FILE_FILE_EXT_NAME_OFFSET, FDFS_FILE_EXT_NAME_MAX_LEN + 1);
    }

    public static char[] trunk_get_full_filename(FdfsTrunkFullInfo pTrunkInfo,
                                                 char[] full_filename, int length) {
        return trunk_get_full_filename_ex(TrunkShared.fdfsStorePaths, pTrunkInfo, full_filename, length);
    }

    /**
     * 返回full_fileName
     *
     * @param pStorePaths
     * @param pTrunkInfo
     * @param full_filename
     * @param length
     * @return
     */
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

    public static boolean IS_TRUNK_FILE_BY_ID(FdfsTrunkFullInfo trunkInfo) {

        return trunkInfo.getFile().getId() > 0;
    }
}
