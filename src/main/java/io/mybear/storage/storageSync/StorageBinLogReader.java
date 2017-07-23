package io.mybear.storage.storageSync;

import io.mybear.tracker.types.BinLogBuffer;

/**
 * Created by jamie on 2017/7/22.
 */
public class StorageBinLogReader {
    public String storageId; //char storage_id[FDFS_STORAGE_ID_MAX_SIZE];
    public boolean need_sync_old;
    public boolean sync_old_done;
    public boolean last_file_exist;   //if the last file exist on the dest server
    public BinLogBuffer binlog_buff;
    public long until_timestamp;
    public int mark_fd;
    public int binlog_index;
    public int binlog_fd;
    public long binlog_offset;
    public long scan_row_count;
    public long sync_row_count;

    public long last_scan_rows;  //for write to mark file
    public long last_sync_rows;  //for write to mark file
}
