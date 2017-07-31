package io.mybear.storage.storageSync;

/**
 * Created by jamie on 2017/7/22.
 */
public class StorageBinLogRecord {
    public long timestamp;
    public char op_type;
    public String filename;//filename[128];  //filename with path index prefix which should be trimed
    public String true_filename;//[128]; //pure filename
    public String src_filename;//[128];  //src filename with path index prefix
    public int true_filename_len;
    public int store_path_index;
}
