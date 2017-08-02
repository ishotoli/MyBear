package io.mybear.storage.storageSync;

import io.mybear.common.FDFSStorageBrief;

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

    public static int g_binlog_fd;
    public static int g_binlog_index;

    public static int g_storage_sync_thread_count;

    public static int storage_sync_init() {
        return 0;
    }

    public static int storage_sync_destroy() {
        return 0;
    }

    ;

    public static int storage_binlog_write(long timestamp, byte op_type, String filename) {
        return storage_binlog_write_ex(timestamp, op_type, filename, null);

    }

    public static int storage_binlog_write_ex(long timestamp, byte op_type, String
            filename, String extra) {
        return 0;
    }

    public static int storage_binlog_read(StorageBinLogReader pReader, StorageBinLogRecord pRecord, int record_length) {
        return 0;
    }

    public static int storage_sync_thread_start(FDFSStorageBrief pStorage) {
        return 0;
    }

    public static int kill_storage_sync_threads() {
        return 0;
    }

    public static int fdfs_binlog_sync_func(Object args) {
        return 0;
    }

    public static String get_mark_filename_by_reader(Object pArg, String full_filename) {
        return "";
    }

    public static int storage_unlink_mark_file(String storage_id) {
        return 0;
    }

    public static int storage_rename_mark_file(String old_ip_addr, int old_port, String new_ip_addr, int new_port) {
        return 0;
    }

    public static int storage_open_readable_binlog(StorageBinLogReader pReader, GetFilenameFunc filename_func, Object pArg) {
        return 0;
    }

    public static int storage_reader_init(FDFSStorageBrief pStorage, StorageBinLogReader pReader) {
        return 0;
    }

    public static void storage_reader_destroy(StorageBinLogReader pReader) {
        return;
    }

    public static int storage_report_storage_status(String storage_id, String ip_addr, char status) {
        return 0;

    }

    @FunctionalInterface
    interface GetFilenameFunc {
        String apply(Object args, String s);
    }
}
