package io.mybear.tracker;

import java.util.Date;

/**
 * Created by jamie on 2017/6/21.
 */
public class TrackerGlobal {
    public static final int DEFAULT_SERVER_PORT = 22122;


    public static Date gUpTime = new Date(0l);

    public static final int TRACKER_SYNC_TO_FILE_FREQ = 1000;
    public static final int TRACKER_MAX_PACKAGE_SIZE = 8 *1024;
    public static  final int TRACKER_SYNC_STATUS_FILE_INTERVAL = 300; // 5 minute

    public static final boolean g_continue_flag =true;
    public static final int g_server_port = 22000;
    public static final int g_storage_stat_chg_count = 0;
    public static final  int g_storage_sync_time_chg_count = 0; // sync timestamp
    public static final int g_max_connections = 0;
    public static final int g_min_buff_size = 0;
    public static final int g_max_buff_size=0;
    public static final int g_accept_threads = 1;
    public static final int g_work_threads = 0;
    public static final int g_sync_log_buff_interval = 0; // sync log buff to disk every interval seconds
    public static final int g_check_active_interval = 0; // check storage server alive every interval seconds

    public static final int g_allow_ip_count = 0; // -1 means match any ip address

    public static final int g_run_by_gid = 0;
    public static final int g_run_by_uid=0;


    public static final char[] g_run_by_group = {0};
    public static final char[] g_run_by_user = {0};

    public static final boolean g_storage_ip_changed_auto_adjust =true;
    public static final boolean g_use_storage_id = false;  //identify storage by ID instead of IP address
    public static final byte g_id_type_in_filename = 0; //id type of the storage server in the filename
    public static final boolean g_rotate_error_log =false;  //if rotate the error log every day
//    public static final TimeInfo g_error_log_rotate_time;  //rotate error log time base

//    public static final TimeInfo g_trunk_create_file_time_base;

    public static final int g_thread_stack_size = 64 * 1024;
    public static final int g_storage_sync_file_max_delay = 86400;
    public static final int  g_storage_sync_file_max_time = 300;

    public static final boolean g_store_slave_file_use_link = false;
    public static final boolean g_if_use_trunk_file = false;
    public static final boolean g_trunk_create_file_advance =false;
    public static final boolean g_trunk_init_check_occupying = false;
    public static final boolean g_trunk_init_reload_from_binlog = false;
    public static final int g_slot_min_size = 256;    //slot min size, such as 256 bytes
    public static final int g_slot_max_size = 16 * 1024 * 1024;    //slot max size, such as 16MB
    public static final int g_trunk_file_size = 64 * 1024 * 1024;  //the trunk file size, such as 64MB
//    TimeInfo g_trunk_create_file_time_base = {0, 0};
    public static final int g_trunk_create_file_interval = 86400;
    public static final int g_trunk_compress_binlog_min_interval = 0;
    public static final int g_trunk_create_file_space_threshold = 0;



//    public static final time_t g_up_time;
//    public static final TrackerStatus g_tracker_last_status;  //the status of last running

    public static final int g_http_check_interval = 30;
    public static final int g_http_check_type = 0; // 0 tcp; 1 http
    public static final char[] g_http_check_uri = {0};
    public static final boolean g_http_servers_dirty = false;

    public static final char[] g_exe_name = {0};


}
