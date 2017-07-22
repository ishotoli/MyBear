package io.mybear.tracker;

import io.mybear.tracker.types.FdfsGroups;

/**
 * Created by jamie on 2017/6/21.
 */
public class TrackerGlobal {
    public static int DEFAULT_SERVER_PORT = 22122;

    public static int TRACKER_SYNC_TO_FILE_FREQ = 1000;
    public static int TRACKER_MAX_PACKAGE_SIZE = 8 * 1024;
    public static int TRACKER_SYNC_STATUS_FILE_INTERVAL = 300; // 5 minute

    public static boolean g_continue_flag = true;
    public static int g_server_port = 22000;
    public static int g_storage_stat_chg_count = 0;
    public static int g_storage_sync_time_chg_count = 0; // sync timestamp
    public static int g_max_connections = 0;
    public static int g_min_buff_size = 0;
    public static int g_max_buff_size = 0;
    public static int g_accept_threads = 1;
    public static int g_work_threads = 0;
    public static int g_sync_log_buff_interval = 0; // sync log buff to disk every interval seconds
    public static int g_check_active_interval = 0; // check storage server alive every interval seconds


    public static FdfsGroups ggroups = new FdfsGroups();

    public static int g_allow_ip_count = 0; // -1 means match any ip address

    public static int g_run_by_gid = 0;
    public static int g_run_by_uid = 0;


    public static String g_run_by_group = null;
    public static String g_run_by_user = null;

    public static boolean g_storage_ip_changed_auto_adjust = true;
    public static boolean g_use_storage_id = false;  //identify storage by ID instead of IP address
    public static byte g_id_type_in_filename = 0; //id type of the storage server in the filename
    public static boolean g_rotate_error_log = false;  //if rotate the error log every day
//    public static  TimeInfo g_error_log_rotate_time;  //rotate error log time base

//    public static  TimeInfo g_trunk_create_file_time_base;

    public static int g_thread_stack_size = 64 * 1024;
    public static int g_storage_sync_file_max_delay = 86400;
    public static int g_storage_sync_file_max_time = 300;

    public static boolean g_store_slave_file_use_link = false;
    public static boolean g_if_use_trunk_file = false;
    public static boolean g_trunk_create_file_advance = false;
    public static boolean g_trunk_init_check_occupying = false;
    public static boolean g_trunk_init_reload_from_binlog = false;
    public static int g_slot_min_size = 256;    //slot min size, such as 256 bytes
    public static int g_slot_max_size = 16 * 1024 * 1024;    //slot max size, such as 16MB
    public static int g_trunk_file_size = 64 * 1024 * 1024;  //the trunk file size, such as 64MB
    //    TimeInfo g_trunk_create_file_time_base = {0, 0};
    public static int g_trunk_create_file_interval = 86400;
    public static int g_trunk_compress_binlog_min_interval = 0;
    public static int g_trunk_create_file_space_threshold = 0;

    public static int g_log_file_keep_days = 0;


//    public static  time_t g_up_time;
//    public static  TrackerStatus g_tracker_last_status;  //the status of last running

    public static int g_http_check_interval = 30;
    public static int g_http_check_type = 0; // 0 tcp; 1 http
    public static char[] g_http_check_uri = {0};
    public static boolean g_http_servers_dirty = false;

    public static char[] g_exe_name = {0};

}
