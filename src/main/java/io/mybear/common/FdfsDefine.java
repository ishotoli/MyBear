package io.mybear.common;

/**
 * Created by jamie on 2017/6/21.
 */
public class FdfsDefine {
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
