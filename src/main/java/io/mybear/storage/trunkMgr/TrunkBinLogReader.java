package io.mybear.storage.trunkMgr;

/**
 * Created by jamie on 2017/7/8.
 */
public class TrunkBinLogReader {
    public byte[] storage_id;//16
    // public BinLogBuffer binlog_buff;
    public int mark_fd;
    public int binlog_fd;
    public long binlog_offset;
    public long last_binlog_offset;  //for write to mark file
}
