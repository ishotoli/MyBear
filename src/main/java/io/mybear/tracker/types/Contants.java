package io.mybear.tracker.types;

/**
 * @author qd
 * @create 2017-07-20 17:42
 */

public final class Contants {

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

    public static final int FDFS_ONE_MB = (1024 * 1024);

    //which group to upload file
    public static final int FDFS_STORE_LOOKUP_ROUND_ROBIN = 0;  //round robin
    public static final int FDFS_STORE_LOOKUP_SPEC_GROUP = 1;  //specify group
    public static final int FDFS_STORE_LOOKUP_LOAD_BALANCE = 2;  //load balance
}
