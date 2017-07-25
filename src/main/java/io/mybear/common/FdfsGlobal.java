package io.mybear.common;

import io.mybear.common.constants.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static io.mybear.common.FdfsDefine.FDFS_STORAGE_SERVER_DEF_PORT;
import static org.csource.fastdfs.ClientGlobal.DEFAULT_CONNECT_TIMEOUT;
import static org.csource.fastdfs.ClientGlobal.DEFAULT_NETWORK_TIMEOUT;

/**
 * Created by jamie on 2017/6/21.
 */
public class FdfsGlobal {

    public static final int FDFS_FILE_EXT_NAME_MAX_LEN = 6;
    public static Logger log = LoggerFactory.getLogger(FdfsGlobal.class);
    public static int g_fdfs_connect_timeout = DEFAULT_CONNECT_TIMEOUT;
    public static int g_fdfs_network_timeout = DEFAULT_NETWORK_TIMEOUT;

    public static int g_server_port = FDFS_STORAGE_SERVER_DEF_PORT;

    public static boolean g_use_access_log = false;    //if log to access log
    public static boolean g_rotate_access_log = false; //if rotate the access log every day
    public static boolean g_rotate_error_log = false;  //if rotate the error log every day
    public static boolean g_use_storage_id = false;
    public static int g_log_file_keep_days = 0;
    public static String g_fdfs_base_path = File.separatorChar + "tmp";

    /**
     * 检验文件名是否合法
     *
     * @param filename
     * @param len
     * @return
     */
    public static int fdfs_check_data_filename(final char[] filename, final int len) {
        if (filename == null || filename.length < 7) {
            log.error(CommonConstant.LOG_FORMAT, "fdfs_check_data_filename", "", String.format("the filename is null or the filename is lower 7"));
            return -1;
        }
        String fileName = new String(filename);
        if (len < 6) {
            log.error(CommonConstant.LOG_FORMAT, "fdfs_check_data_filename", "", String.format("the length=%d of filename %s is too short", fileName, len));
            return -1;
        }
        if (!CommonDefine.IS_UPPER_HEX(filename[0]) || !CommonDefine.IS_UPPER_HEX(filename[1]) || filename[2] != '/' ||
                !CommonDefine.IS_UPPER_HEX(filename[3]) || !CommonDefine.IS_UPPER_HEX(filename[4]) || filename[5] != '/') {
            log.error(CommonConstant.LOG_FORMAT, "fdfs_check_data_filename", "", String.format("the format of filename  %s is invalid", fileName));
            return -1;
        }
        if (fileName.indexOf('/', 6) > 0) {
            log.error(CommonConstant.LOG_FORMAT, "fdfs_check_data_filename", "", String.format("the format of filename  %s is invalid", fileName));
            return -1;
        }
        return 0;
    }
}
