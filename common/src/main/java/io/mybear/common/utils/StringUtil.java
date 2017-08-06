package io.mybear.common.utils;

import io.mybear.common.constants.CommonConstant;
import io.mybear.common.trunk.TrunkShared;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.mybear.common.constants.CommonConstant.FDFS_LOGIC_FILE_PATH_LEN;
import static io.mybear.common.constants.CommonConstant.FDFS_STORAGE_STORE_PATH_PREFIX_CHAR;

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

    /**
     * @param logic_filename
     * @param filename_len
     * @param true_filename
     * @return
     */
    public static FilenameResultEx storage_split_filename_no_check(String logic_filename) {
        char[] true_filename = new char[128];
        FilenameResultEx result = new FilenameResultEx();
        int index = SPLIT_FILENAME_BODY(logic_filename.toCharArray(), logic_filename.length(), true_filename, false);
        result.true_filename = new String(true_filename);
        result.storePathIndex = index;
        return result;
    }

    public final static byte[] char2byte(char[] c) {
        byte[] bytes = new byte[c.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) c[i];
        }
        return bytes;
    }

    public final static char[] byte2char(byte[] c) {
        return new String(c).toCharArray();
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
