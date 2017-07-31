package io.mybear.common;

import java.util.concurrent.atomic.LongAdder;

/**
 * Created by jamie on 2017/7/8.
 */
public class FDFSStorageStat {
    /* following count stat by source server,
       not including synced count
*/
    public LongAdder total_upload_count = new LongAdder();
    public LongAdder success_upload_count = new LongAdder();
    public LongAdder total_append_count = new LongAdder();
    public LongAdder success_append_count = new LongAdder();
    public LongAdder total_modify_count = new LongAdder();
    public LongAdder success_modify_count = new LongAdder();
    public LongAdder total_truncate_count = new LongAdder();
    public LongAdder success_truncate_count = new LongAdder();
    public LongAdder total_set_meta_count = new LongAdder();
    public LongAdder success_set_meta_count = new LongAdder();
    public LongAdder total_delete_count = new LongAdder();
    public LongAdder success_delete_count = new LongAdder();
    public LongAdder total_download_count = new LongAdder();
    public LongAdder success_download_count = new LongAdder();
    public LongAdder total_get_meta_count = new LongAdder();
    public LongAdder success_get_meta_count = new LongAdder();
    public LongAdder total_create_link_count = new LongAdder();
    public LongAdder success_create_link_count = new LongAdder();
    public LongAdder total_delete_link_count = new LongAdder();
    public LongAdder success_delete_link_count = new LongAdder();
    public LongAdder total_upload_bytes = new LongAdder();
    public LongAdder success_upload_bytes = new LongAdder();
    public LongAdder total_append_bytes = new LongAdder();
    public LongAdder success_append_bytes = new LongAdder();
    public LongAdder total_modify_bytes = new LongAdder();
    public LongAdder success_modify_bytes = new LongAdder();
    public LongAdder total_download_bytes = new LongAdder();
    public LongAdder success_download_bytes = new LongAdder();
    public LongAdder total_sync_in_bytes = new LongAdder();
    public LongAdder success_sync_in_bytes = new LongAdder();
    public LongAdder total_sync_out_bytes = new LongAdder();
    public LongAdder success_sync_out_bytes = new LongAdder();
    public LongAdder total_file_open_count = new LongAdder();
    public LongAdder success_file_open_count = new LongAdder();
    public LongAdder total_file_read_count = new LongAdder();
    public LongAdder success_file_read_count = new LongAdder();
    public LongAdder total_file_write_count = new LongAdder();
    public LongAdder success_file_write_count = new LongAdder();

    /* last update timestamp as source server, 
           current server' timestamp
    */
    public volatile long last_source_update;
    /* last update timestamp as dest server, 
           current server' timestamp
    */
    public LongAdder last_sync_update = new LongAdder();

    /* last syned timestamp, 
       source server's timestamp
    */
    public LongAdder last_synced_timestamp = new LongAdder();

    /* last heart beat time */
    public LongAdder last_heart_beat_time = new LongAdder();


    public int alloc_count;
    public volatile int current_count;
    public int max_count;

}
