package io.mybear.common.tracker;

import io.mybear.common.FDFSStorageStat;

public class FdfsStorageDetail implements Comparable<FdfsStorageDetail> {
    public FdfsStorageStat state;

    public char status;
    public char padding;  //just for padding
    public String id;//char id[FDFS_STORAGE_ID_MAX_SIZE];
    public String ip_addr;//    char ip_addr[IP_ADDRESS_SIZE];
    public String version;//    char version[FDFS_VERSION_SIZE];
    public String domain_name;//    char domain_name[FDFS_DOMAIN_NAME_MAX_SIZE];

    public FdfsStorageDetail psync_src_server;
    public long[] path_total_mbs;//    int64_t *path_total_mbs; //total disk storage in MB
    public long[] path_free_mbs;//    int64_t *path_free_mbs;  //free disk storage in MB
    //
    public long total_mb;//    int64_t total_mb;  //total disk storage in MB
    public long free_mb;//    int64_t free_mb;  //free disk storage in MB
    public long changelog_offset; //    int64_t changelog_offset;  //changelog file offset
    //
    public long sync_until_timestamp;//    time_t sync_until_timestamp;
    public long join_time;//    time_t join_time;  //storage join timestamp (create timestamp)
    public long up_time;//    time_t up_time;    //startup timestamp

    public int store_path_count;  //store base path count of each storage server
    public int subdir_count_per_path;
    public int upload_priority; //storage upload priority

    public int storage_port;   //storage server port
    public int storage_http_port; //storage http server port

    public int current_write_path; //current write path index

    public int chg_count;    //current server changed counter
    public int trunk_chg_count;   //trunk server changed count
    public FDFSStorageStat stat;

//#ifdef WITH_HTTPD
//    int http_check_last_errno;
//    int http_check_last_status;
//    int http_check_fail_count;
//    char http_check_error_info[256];
//#endif

    /**
     * tracker_mem_cmp_by_storage_id
     * 写完了
     *
     * @return
     */
    public static int cmpByStorageId(FdfsStorageDetail p1, FdfsStorageDetail p2) {
        return p1.id.compareTo(p2.id);
    }

    @Override
    public int compareTo(FdfsStorageDetail o) {
        return cmpByStorageId(this, o);
    }

}
