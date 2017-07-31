package io.mybear.common;

import io.mybear.common.constants.config.FdfsGlobal;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

/**
 * Created by zkn on 2017/7/12.
 */
public class TestGenFileName {

    private static final String FILE_SEPARATOR = File.separator;

    @Test
    public void testGenFileName() {
        int fileNameLen = 5;
        char[] encoded = "hello".toCharArray();
        char[] formattedExtName = "niha".toCharArray();
        //测试文件名
        char[] fileName = (String.format("%02X", 12) + FILE_SEPARATOR + String.format("%02X", 13) + FILE_SEPARATOR).toCharArray();
        int fileLen = fileName.length;
        int flag = 0;
        if (fileNameLen > encoded.length) {
            flag = fileName.length;
            fileName = Arrays.copyOf(fileName, flag + encoded.length);
            System.arraycopy(encoded, 0, fileName, flag, encoded.length);
        } else {
            flag = fileName.length;
            fileName = Arrays.copyOf(fileName, flag + fileNameLen);
            System.arraycopy(encoded, 0, fileName, flag, fileNameLen);
            int len = FdfsGlobal.FDFS_FILE_EXT_NAME_MAX_LEN + 1;
            if (formattedExtName.length > len) {
                fileName = Arrays.copyOf(fileName, flag + fileNameLen + len);
                System.arraycopy(formattedExtName, 0, fileName, flag + fileNameLen, len);
            } else {
                fileName = Arrays.copyOf(fileName, flag + fileNameLen + formattedExtName.length);
                System.arraycopy(formattedExtName, 0, fileName, flag + fileNameLen, formattedExtName.length);
            }
        }
        System.out.println(fileName);
    }
}
