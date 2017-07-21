package io.mybear.common;

import io.mybear.tracker.TrackerTypes;

import java.io.File;
import java.net.InetSocketAddress;

import static io.mybear.common.FdfsDefine.DEFAULT_MAX_CONNECTONS;
import static io.mybear.common.FdfsDefine.FDFS_STORAGE_SERVER_DEF_PORT;
import static io.mybear.storage.StorageGlobal.*;
import static io.mybear.tracker.TrackerTypes.*;
import static org.csource.fastdfs.ClientGlobal.DEFAULT_CONNECT_TIMEOUT;
import static org.csource.fastdfs.ClientGlobal.DEFAULT_NETWORK_TIMEOUT;

/**
 * Created by jamie on 2017/6/21.
 */
public class FdfsGlobal {

    public static final int FDFS_FILE_EXT_NAME_MAX_LEN = 6;
    public static int g_fdfs_connect_timeout = DEFAULT_CONNECT_TIMEOUT;
    public static int g_fdfs_network_timeout = DEFAULT_NETWORK_TIMEOUT;


    public static int g_server_port = FDFS_STORAGE_SERVER_DEF_PORT;
    public static String g_http_domain = "";
    public static int g_http_port = 80;
    public static int g_last_server_port = 0;
    public static int g_last_http_port = 0;
    public static int g_max_connections = DEFAULT_MAX_CONNECTONS;
    public static int g_accept_threads = 1;
    public static int g_work_threads = 4;
    public static int g_buff_size = STORAGE_DEFAULT_BUFF_SIZE;

    public static boolean g_disk_rw_direct = false;
    public static boolean g_disk_rw_separated = true;
    public static int g_disk_reader_threads = DEFAULT_DISK_READER_THREADS;
    public static int g_disk_writer_threads = DEFAULT_DISK_WRITER_THREADS;
    public static int g_extra_open_file_flags = 0;

    public static int g_file_distribute_path_mode = FDFS_FILE_DIST_PATH_ROUND_ROBIN;
    public static int g_file_distribute_rotate_count = FDFS_FILE_DIST_DEFAULT_ROTATE_COUNT;
    public static int g_fsync_after_written_bytes = -1;

    public static int g_dist_path_index_high = 0; //current write to high path
    public static int g_dist_path_index_low = 0;  //current write to low path
    public static int g_dist_write_file_count = 0;  //current write file count

    public static int g_storage_count = 0;
    public static FDFSStorageServer[] g_storage_servers = new FDFSStorageServer[FDFS_MAX_SERVERS_EACH_GROUP];
    public static FDFSStorageServer[] g_sorted_storages = new FDFSStorageServer[FDFS_MAX_SERVERS_EACH_GROUP];

    public static int g_tracker_reporter_count = 0;
    public static int g_heart_beat_interval = STORAGE_BEAT_DEF_INTERVAL;
    public static int g_stat_report_interval = STORAGE_REPORT_DEF_INTERVAL;

    public static int g_sync_wait_usec = STORAGE_DEF_SYNC_WAIT_MSEC;
    public static int g_sync_interval = 0; //unit: milliseconds
    public static long[] g_sync_start_time = {0, 0};
    public static long[] g_sync_end_time = {23, 59};
    public static boolean g_sync_part_time = false;
    public static int g_sync_log_buff_interval = 10;
    public static int g_sync_binlog_buff_interval = 60;
    public static int g_write_mark_file_freq = FDFS_DEFAULT_SYNC_MARK_FILE_FREQ;
    public static int g_sync_stat_file_interval = DEFAULT_SYNC_STAT_FILE_INTERVAL;


    public static int g_stat_change_count = 1;
    public static int g_sync_change_count = 0;

    public static int g_storage_join_time = 0;
    public static int g_sync_until_timestamp = 0;
    public static boolean g_sync_old_done = false;
    public static int[] g_sync_src_id = {0};

    public static String g_group_name = "";
    public static String g_my_server_id_str = "";
    ; //my server id string
    public static String g_tracker_client_ip = "";
    ; //storage ip as tracker client
    public static String g_last_storage_ip = "";//the last storage ip address


    public static InetSocketAddress g_server_id_in_filename = null;
    public static boolean g_use_access_log = false;    //if log to access log
    public static boolean g_rotate_access_log = false; //if rotate the access log every day
    public static boolean g_rotate_error_log = false;  //if rotate the error log every day
    public static boolean g_use_storage_id = false;    //identify storage by ID instead of IP address
    public static byte g_id_type_in_filename = TrackerTypes.FDFS_ID_TYPE_IP_ADDRESS; //id type of the storage server in the filename
    public static boolean g_store_slave_file_use_link = false; //if store slave file use symbol link

    public static boolean g_check_file_duplicate = false;
    public static int g_file_signature_method = STORAGE_FILE_SIGNATURE_METHOD_HASH;
    public static String g_key_namespace = "";


    public static int g_namespace_len = 0;
    public static int g_allow_ip_count = 0;
    public static InetSocketAddress[] g_allow_ip_addrs = null;

    public static long[] g_access_log_rotate_time = {0, 0}; //rotate access log time base
    public static long[] g_error_log_rotate_time = {0, 0}; //rotate error log time base

    public static long g_run_by_gid;
    public static long g_run_by_uid;

    public static String g_run_by_group = "";
    public static String g_run_by_user = "";
    public static String g_bind_addr = "";
    public static boolean g_client_bind_addr = true;
    public static boolean g_storage_ip_changed_auto_adjust = false;
    public static boolean g_thread_kill_done = false;
    public static boolean g_file_sync_skip_invalid_record = false;
    public static int g_thread_stack_size = 512 * 1024;
    public static int g_upload_priority = DEFAULT_UPLOAD_PRIORITY;
    public static long g_up_time = 0;
    public static int g_log_file_keep_days = 0;
    public static String g_fdfs_base_path = File.separatorChar+"tmp";
}
