package io.mybear.tracker;


import static io.mybear.common.FdfsGlobal.FDFS_FILE_EXT_NAME_MAX_LEN;

/**
 * Created by jamie on 2017/6/21.
 */
public class TrackerTypes {
    public static final int FDFS_ONE_MB = (1024 * 1024);
    public static final int FDFS_GROUP_NAME_MAX_LEN = 16;
    public static final int FDFS_MAX_SERVERS_EACH_GROUP = 32;
    public static final int FDFS_MAX_GROUPS = 512;
    public static final int FDFS_MAX_TRACKERS = 16;

    public static final int FDFS_MAX_META_NAME_LEN = 64;
    public static final int FDFS_MAX_META_VALUE_LEN = 256;

    public static final int FDFS_FILE_PREFIX_MAX_LEN = 16;
    public static final int FDFS_LOGIC_FILE_PATH_LEN = 10;
    public static final int FDFS_TRUE_FILE_PATH_LEN = 6;
    public static final int FDFS_FILENAME_BASE64_LENGTH = 27;
    public static final int FDFS_TRUNK_FILE_INFO_LEN = 16;
    public static final int FDFS_MAX_SERVER_ID = ((1 << 24) - 1);

    public static final int FDFS_ID_TYPE_SERVER_ID = 1;
    public static final int FDFS_ID_TYPE_IP_ADDRESS = 2;

    public static final int FDFS_NORMAL_LOGIC_FILENAME_LENGTH = (FDFS_LOGIC_FILE_PATH_LEN + FDFS_FILENAME_BASE64_LENGTH + FDFS_FILE_EXT_NAME_MAX_LEN + 1);

    public static final int FDFS_TRUNK_FILENAME_LENGTH = (FDFS_TRUE_FILE_PATH_LEN +
            FDFS_FILENAME_BASE64_LENGTH + FDFS_TRUNK_FILE_INFO_LEN +
            1 + FDFS_FILE_EXT_NAME_MAX_LEN);
    public static final int FDFS_TRUNK_LOGIC_FILENAME_LENGTH = (FDFS_TRUNK_FILENAME_LENGTH +
            (FDFS_LOGIC_FILE_PATH_LEN - FDFS_TRUE_FILE_PATH_LEN));

    public static final int FDFS_VERSION_SIZE = 6;

    //status order is important!
    public static final int FDFS_STORAGE_STATUS_INIT = 0;
    public static final int FDFS_STORAGE_STATUS_WAIT_SYNC = 1;
    public static final int FDFS_STORAGE_STATUS_SYNCING = 2;
    public static final int FDFS_STORAGE_STATUS_IP_CHANGED = 3;
    public static final int FDFS_STORAGE_STATUS_DELETED = 4;
    public static final int FDFS_STORAGE_STATUS_OFFLINE = 5;
    public static final int FDFS_STORAGE_STATUS_ONLINE = 6;
    public static final int FDFS_STORAGE_STATUS_ACTIVE = 7;
    public static final int FDFS_STORAGE_STATUS_RECOVERY = 9;
    public static final int FDFS_STORAGE_STATUS_NONE = 99;

    //which group to upload file
    public static final int FDFS_STORE_LOOKUP_ROUND_ROBIN = 0; //round robin
    public static final int FDFS_STORE_LOOKUP_SPEC_GROUP = 1; //specify group
    public static final int FDFS_STORE_LOOKUP_LOAD_BALANCE = 2;//load balance

    //which server to upload file
    public static final int FDFS_STORE_SERVER_ROUND_ROBIN = 0;  //round robin
    public static final int FDFS_STORE_SERVER_FIRST_BY_IP = 1; //the first server order by ip
    public static final int FDFS_STORE_SERVER_FIRST_BY_PRI = 2; //the first server order by priority

    //which server to download file
    public static final int FDFS_DOWNLOAD_SERVER_ROUND_ROBIN = 0; //round robin
    public static final int FDFS_DOWNLOAD_SERVER_SOURCE_FIRST = 1;//the source server

    //which path to upload file
    public static final int FDFS_STORE_PATH_ROUND_ROBIN = 0; //round robin
    public static final int FDFS_STORE_PATH_LOAD_BALANCE = 2;//load balance

    //the mode of the files distributed to the data path
    public static final int FDFS_FILE_DIST_PATH_ROUND_ROBIN = 0; //round robin
    public static final int FDFS_FILE_DIST_PATH_RANDOM = 1;//random

    //http check alive type
    public static final int FDFS_HTTP_CHECK_ALIVE_TYPE_TCP = 0; //tcp
    public static final int FDFS_HTTP_CHECK_ALIVE_TYPE_HTTP = 1;  //http

    public static final int FDFS_DOWNLOAD_TYPE_TCP = 0;//tcp
    public static final int FDFS_DOWNLOAD_TYPE_HTTP = 1;//http

    public static final int FDFS_FILE_DIST_DEFAULT_ROTATE_COUNT = 100;

    public static final int FDFS_DOMAIN_NAME_MAX_SIZE = 128;

    public static final int FDFS_STORAGE_STORE_PATH_PREFIX_CHAR = 'M';
    public static final String FDFS_STORAGE_DATA_DIR_FORMAT = "%02X";
    public static final String FDFS_STORAGE_META_FILE_EXT = "-m";
    public static final int INFINITE_FILE_SIZE = 0;
    public static final int FDFS_APPENDER_FILE_SIZE =
            INFINITE_FILE_SIZE;


    public static final long FDFS_TRUNK_FILE_MARK_SIZE = (512 * 1024L * 1024 * 1024 * 1024 * 1024L);


    public static final int FDFS_CHANGE_FLAG_TRACKER_LEADER = 1;  //tracker leader changed
    public static final int FDFS_CHANGE_FLAG_TRUNK_SERVER = 2;  //trunk server changed
    public static final int FDFS_CHANGE_FLAG_GROUP_SERVER = 4; //group server changed
    public static final int FDFS_STORAGE_ID_MAX_SIZE = 16;
    public static final int TRACKER_STORAGE_RESERVED_SPACE_FLAG_MB = 0;
    public static final int TRACKER_STORAGE_RESERVED_SPACE_FLAG_RATIO = 1;

    public static final boolean IS_APPENDER_FILE(int file_size) {
        return ((file_size & FDFS_APPENDER_FILE_SIZE) != 0);
    }

    public static final boolean IS_TRUNK_FILE(int file_size) {
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
