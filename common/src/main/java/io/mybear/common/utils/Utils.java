package io.mybear.common.utils;


import static io.mybear.common.constants.CommonConstant.*;

public final class Utils {
    public static long parseBytes(String pStr, int default_unit_bytes) {
        if (pStr == null) return 0;
        pStr = pStr.trim();
        if ("".equals(pStr)) return 0;
        if (Character.isDigit(pStr.charAt(pStr.length() - 1))) return Long.parseLong(pStr);
        long res = Long.parseLong(pStr.substring(0, pStr.length() - 2));
        char c = pStr.charAt(pStr.length() - 2);
        switch (c) {
            case 'G':
            case 'g':
                return res * 1024 * 1024 * 1024;
            case 'M':
            case 'm':
                return res * 1024 * 1024;
            case 'K':
            case 'k':
                return res * 1024;
            default:
                return default_unit_bytes * res;
        }
    }

    public static final boolean IS_APPENDER_FILE(int file_size) {
        return ((file_size & FDFS_APPENDER_FILE_SIZE) != 0);
    }

    public static final boolean IS_TRUNK_FILE(long file_size) {
        return ((file_size & FDFS_TRUNK_FILE_MARK_SIZE) != 0);
    }

    public static final boolean IS_SLAVE_FILE(int filename_len, int file_size) {
        return ((filename_len > FDFS_TRUNK_LOGIC_FILENAME_LENGTH) ||
                (filename_len > FDFS_NORMAL_LOGIC_FILENAME_LENGTH &&
                        !IS_TRUNK_FILE(file_size)));
    }

    public static final int FDFS_TRUNK_FILE_TRUE_SIZE(int file_size) {
        return (file_size & 0xFFFFFFFF);
    }

}
