package io.mybear.storage.trunkMgr;

import io.mybear.common.Base64Context;
import io.mybear.common.FdfsStorePaths;
import io.mybear.common.utils.Base64;

import static io.mybear.common.FdfsGlobal.FDFS_FILE_EXT_NAME_MAX_LEN;

/**
 * Created by jamie on 2017/6/21.
 */
public class TrunkShared {

    public static final FdfsStorePaths fdfsStorePaths = new FdfsStorePaths();
    //定义Base64Context
    public static final Base64Context base64Context = new Base64Context();
    public static byte FDFS_TRUNK_STATUS_FREE = 0;
    public static byte FDFS_TRUNK_STATUS_HOLD = 1;
    public static byte FDFS_TRUNK_FILE_TYPE_NONE = '\0';
    public static byte FDFS_TRUNK_FILE_TYPE_REGULAR = 'F';
    public static byte FDFS_TRUNK_FILE_TYPE_LINK = 'L';
    public static int FDFS_STAT_FUNC_STAT = 0;
    public static int FDFS_STAT_FUNC_LSTAT = 1;
    public static int FDFS_TRUNK_FILE_FILE_TYPE_OFFSET = 0;
    public static int FDFS_TRUNK_FILE_ALLOC_SIZE_OFFSET = 1;
    public static int FDFS_TRUNK_FILE_FILE_SIZE_OFFSET = 5;
    public static int FDFS_TRUNK_FILE_FILE_CRC32_OFFSET = 9;
    public static int FDFS_TRUNK_FILE_FILE_MTIME_OFFSET = 13;
    public static int FDFS_TRUNK_FILE_FILE_EXT_NAME_OFFSET = 17;
    public static int FDFS_TRUNK_FILE_HEADER_SIZE = (17 + FDFS_FILE_EXT_NAME_MAX_LEN + 1);


    public static void trunkSharedInit() {
        //初始化Base64
        Base64.base64InitEx(base64Context,0, '-', '_', '.');
    }

    public static FdfsStorePaths getFdfsStorePaths() {
        return fdfsStorePaths;
    }
}
