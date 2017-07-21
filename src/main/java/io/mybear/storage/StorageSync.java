package io.mybear.storage;

/**
 * Created by jamie on 2017/6/21.
 */
public class StorageSync {
    public static byte STORAGE_OP_TYPE_SOURCE_CREATE_FILE = 'C'; //upload file
    public static byte STORAGE_OP_TYPE_SOURCE_APPEND_FILE = 'A'; //append file
    public static byte STORAGE_OP_TYPE_SOURCE_DELETE_FILE = 'D'; //delete file
    public static byte STORAGE_OP_TYPE_SOURCE_UPDATE_FILE = 'U'; //for whole file update such as metadata file
    public static byte STORAGE_OP_TYPE_SOURCE_MODIFY_FILE = 'M';//for part modify
    public static byte STORAGE_OP_TYPE_SOURCE_TRUNCATE_FILE = 'T'; //truncate file
    public static byte STORAGE_OP_TYPE_SOURCE_CREATE_LINK = 'L';//create symbol link
    public static byte STORAGE_OP_TYPE_REPLICA_CREATE_FILE = 'c';
    public static byte STORAGE_OP_TYPE_REPLICA_APPEND_FILE = 'a';
    public static byte STORAGE_OP_TYPE_REPLICA_DELETE_FILE = 'd';
    public static byte STORAGE_OP_TYPE_REPLICA_UPDATE_FILE = 'u';
    public static byte STORAGE_OP_TYPE_REPLICA_MODIFY_FILE = 'm';
    public static byte STORAGE_OP_TYPE_REPLICA_TRUNCATE_FILE = 't';
    public static byte STORAGE_OP_TYPE_REPLICA_CREATE_LINK = 'l';

    public static int STORAGE_BINLOG_BUFFER_SIZE = 64 * 1024;
    public static int STORAGE_BINLOG_LINE_SIZE = 256;
}
