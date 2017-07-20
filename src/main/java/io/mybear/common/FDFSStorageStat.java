package io.mybear.common;

/**
 * Created by jamie on 2017/7/8.
 */
public class FDFSStorageStat {
    /* following count stat by source server,
       not including synced count
*/
    public long total_upload_count;
    public long success_upload_count;
    public long total_append_count;
    public long success_append_count;
    public long total_modify_count;
    public long success_modify_count;
    public long total_truncate_count;
    public long success_truncate_count;
    public long total_set_meta_count;
    public long success_set_meta_count;
    public long total_delete_count;
    public long success_delete_count;
    public long total_download_count;
    public long success_download_count;
    public long total_get_meta_count;
    public long success_get_meta_count;
    public long total_create_link_count;
    public long success_create_link_count;
    public long total_delete_link_count;
    public long success_delete_link_count;
    public long total_upload_bytes;
    public long success_upload_bytes;
    public long total_append_bytes;
    public long success_append_bytes;
    public long total_modify_bytes;
    public long success_modify_bytes;
    public long total_download_bytes;
    public long success_download_bytes;
    public long total_sync_in_bytes;
    public long success_sync_in_bytes;
    public long total_sync_out_bytes;
    public long success_sync_out_bytes;
    public long total_file_open_count;
    public long success_file_open_count;
    public long total_file_read_count;
    public long success_file_read_count;
    public long total_file_write_count;
    public long success_file_write_count;

    /* last update timestamp as source server, 
           current server' timestamp
    */
    public long last_source_update;

    /* last update timestamp as dest server, 
           current server' timestamp
    */
    public long last_sync_update;

    /* last syned timestamp, 
       source server's timestamp
    */
    public long last_synced_timestamp;

    /* last heart beat time */
    public long last_heart_beat_time;


    public int alloc_count;
    public volatile int current_count;
    public int max_count;

}
