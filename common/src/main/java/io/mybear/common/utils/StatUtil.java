package io.mybear.common.utils;

/**
 * Created by zkn on 2017/7/23.
 */
public class StatUtil {
    /**
     * 普通文件
     */
    public static final int S_IFREG = 0100000;
    /**
     * 软连接
     */
    public static final int S_IFLNK = 0120000;
    public static final int S_IFMT = 0xF000;

    public static boolean S_ISREG(int st_mode) {

        return (((st_mode) & S_IFMT) == S_IFREG);
    }

    public static boolean S_ISLNK(int st_mode) {

        return (((st_mode) & S_IFMT) == S_IFLNK);
    }

}
