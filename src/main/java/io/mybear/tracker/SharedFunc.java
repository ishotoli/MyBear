package io.mybear.tracker;

import java.io.File;

/**
 * Created by jamie on 2017/6/21.
 */
public class SharedFunc {

    /**
     * 判断文件是否存在
     *
     * @param fileName
     * @return
     */
    public static boolean fileExists(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    public static boolean delete(String fileName) {
        File file = new File(fileName);
        return file.delete();
    }
    /**
     * 转换路径 如果最后一个字符为 '/' 则转换为 '\0'
     *
     * @param filePath
     */
    public static char[] chopPath(char[] filePath) {

        int lastIndex;
        if (filePath == null || filePath.length == 0) {
            return null;
        }
        lastIndex = filePath.length - 1;
        if (filePath[lastIndex] == File.separatorChar) {
            char[] tmp = new char[lastIndex];
            System.arraycopy(filePath, 0, tmp, 0, lastIndex);
            return tmp;
        }
        return filePath;
    }

    /**
     * 验证是不是目录
     *
     * @param path
     * @return
     */
    public static boolean isDir(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            return true;
        }
        return false;
    }
}
