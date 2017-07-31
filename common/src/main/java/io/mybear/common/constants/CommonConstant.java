package io.mybear.common.constants;

import static io.mybear.common.constants.config.FdfsGlobal.FDFS_FILE_EXT_NAME_MAX_LEN;

/**
 * Created by zkn on 2017/7/13.
 */
public class CommonConstant {
    /**
     * 日志格式
     */
    public static final String LOG_FORMAT = "method={}, params={},result={}";

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
    public static final int FDFS_TRUNK_FILENAME_LENGTH = (FDFS_TRUE_FILE_PATH_LEN + FDFS_FILENAME_BASE64_LENGTH + FDFS_TRUNK_FILE_INFO_LEN + 1 + FDFS_FILE_EXT_NAME_MAX_LEN);
    public static final int FDFS_TRUNK_LOGIC_FILENAME_LENGTH = (FDFS_TRUNK_FILENAME_LENGTH + (FDFS_LOGIC_FILE_PATH_LEN - FDFS_TRUE_FILE_PATH_LEN));

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
    public static final int FDFS_APPENDER_FILE_SIZE = INFINITE_FILE_SIZE;

    public static final long FDFS_TRUNK_FILE_MARK_SIZE = (512 * 1024L * 1024 * 1024 * 1024 * 1024L);

    public static final int FDFS_CHANGE_FLAG_TRACKER_LEADER = 1;  //tracker leader changed
    public static final int FDFS_CHANGE_FLAG_TRUNK_SERVER = 2;  //trunk server changed
    public static final int FDFS_CHANGE_FLAG_GROUP_SERVER = 4; //group server changed
    public static final int FDFS_STORAGE_ID_MAX_SIZE = 16;
    public static final int TRACKER_STORAGE_RESERVED_SPACE_FLAG_MB = 0;
    public static final int TRACKER_STORAGE_RESERVED_SPACE_FLAG_RATIO = 1;
    public static final int IP_ADDRESS_SIZE = 16;
    public static final int MAX_PATH_SIZE = 256;
    public static final int DEFAULT_CONNECT_TIMEOUT = 30;
    public static final int DEFAULT_NETWORK_TIMEOUT = 30;
    public static final int DEFAULT_WORK_THREADS = 4;
    public static final int SYNC_LOG_BUFF_DEF_INTERVAL = 10;
    public static final int TIME_NONE = -1;

    //which server to upload file
    public static final byte FDFS_STORE_SERVER_ROUND_ROBIN = 0;  //round robin
    public static final byte FDFS_STORE_SERVER_FIRST_BY_IP = 1;  //the first server order by ip
    public static final byte FDFS_STORE_SERVER_FIRST_BY_PRI = 2;  //the first server order by priority

    //which server to download file
    public static final byte FDFS_DOWNLOAD_SERVER_ROUND_ROBIN = 0;  //round robin
    public static final byte FDFS_DOWNLOAD_SERVER_SOURCE_FIRST = 1;  //the source server

    //which path to upload file
    public static final byte FDFS_STORE_PATH_ROUND_ROBIN = 0;  //round robin
    public static final byte FDFS_STORE_PATH_LOAD_BALANC = 2;  //load balance


    //which group to upload file
    public static final int FDFS_STORE_LOOKUP_ROUND_ROBIN = 0;  //round robin
    public static final int FDFS_STORE_LOOKUP_SPEC_GROUP = 1;  //specify group
    public static final int FDFS_STORE_LOOKUP_LOAD_BALANCE = 2;  //load balance

    //FdfsDefine
    public static final int DEFAULT_MAX_CONNECTONS = 256;
    public static final int FDFS_TRACKER_SERVER_DEF_PORT = 22000;
    public static final int FDFS_STORAGE_SERVER_DEF_PORT = 23000;
    public static final int FDFS_DEF_STORAGE_RESERVED_MB = 1024;
    public static final String TRACKER_ERROR_LOG_FILENAME = "trackerd";
    public static final String STORAGE_ERROR_LOG_FILENAME = "storaged";
    public static final int FDHT_SERVER_DEFAULT_PORT = 24000;
    public static final int FDHT_DEFAULT_PROXY_PORT = 12200;
    public static final int FDHT_MAX_PKG_SIZE = 64 * 1024;
    public static final int FDHT_MIN_BUFF_SIZE = 64 * 1024;
    public static final int FDHT_DEFAULT_MAX_THREADS = 64;
    public static final int DEFAULT_SYNC_DB_INVERVAL = 86400;
    public static final int DEFAULT_SYNC_WAIT_MSEC = 100;
    public static final int DEFAULT_CLEAR_EXPIRED_INVERVAL = 0;
    public static final int DEFAULT_DB_DEAD_LOCK_DETECT_INVERVAL = 1000;
    public static final int FDHT_MAX_KEY_COUNT_PER_REQ = 128;
    public static final int SYNC_BINLOG_BUFF_DEF_INTERVAL = 60;
    public static final int COMPRESS_BINLOG_DEF_INTERVAL = 86400;
    public static final int DEFAULT_SYNC_STAT_FILE_INTERVAL = 300;
    public static final int FDHT_DEFAULT_SYNC_MARK_FILE_FREQ = 5000;

    public static final int FDHT_STORE_TYPE_BDB = 1;
    public static final int FDHT_STORE_TYPE_MPOOL = 2;

    public static final int FDHT_DEFAULT_MPOOL_INIT_CAPACITY = 10000;
    public static final double FDHT_DEFAULT_MPOOL_LOAD_FACTOR = 0.75;
    public static final int FDHT_DEFAULT_MPOOL_CLEAR_MIN_INTEVAL = 300;

    public static final int CHECK_ACTIVE_DEF_INTERVAL = 100;
    public static final int DEFAULT_STORAGE_SYNC_FILE_MAX_DELAY = 86400;
    public static final int DEFAULT_STORAGE_SYNC_FILE_MAX_TIME = 300;
}
