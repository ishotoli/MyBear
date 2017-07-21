package io.mybear.storage;

import io.mybear.common.FDFSStorageServer;
import io.mybear.common.FDFSStorageStat;
import io.mybear.common.FdfsStorePathInfo;
import io.mybear.common.IniFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;

/**
 * Created by jamie on 2017/6/21.
 */
public class StorageGlobal {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageGlobal.class);
    public static Path exeName;
    public static String BASE_PATH;
    public static int STORAGE_BEAT_DEF_INTERVAL = 30;
    public static int STORAGE_REPORT_DEF_INTERVAL = 300;
    public static int STORAGE_DEF_SYNC_WAIT_MSEC = 100;
    public static int DEFAULT_DISK_READER_THREADS = 1;
    public static int DEFAULT_DISK_WRITER_THREADS = 1;
    public static int DEFAULT_SYNC_STAT_FILE_INTERVAL = 300;
    public static int DEFAULT_DATA_DIR_COUNT_PER_PATH = 256;
    public static int DEFAULT_UPLOAD_PRIORITY = 10;
    public static int FDFS_DEFAULT_SYNC_MARK_FILE_FREQ = 500;
    public static int STORAGE_DEFAULT_BUFF_SIZE = (64 * 1024);
    public static byte STORAGE_FILE_SIGNATURE_METHOD_HASH = 1;
    public static int STORAGE_FILE_SIGNATURE_METHOD_MD5 = 2;
    /* subdirs under store path, g_subdir_count * g_subdir_count 2 level subdirs */
    public static int g_subdir_count_per_path;
    public static int G_SERVER_PORT = 23000;
    public static int G_HTTP_PORT;  //http server port
    public static int g_last_server_port;
    public static int g_last_http_port;  //last http server port
    public static String g_http_domain;  //http server domain name
    public static int g_max_connections;
    public static int g_accept_threads;
    public static int g_work_threads;
    public static long g_buff_size;
    public static boolean g_disk_rw_direct;     //if file read / write directly
    public static boolean g_disk_rw_separated;  //if disk read / write separated
    public static int g_disk_reader_threads; //disk reader thread count per store base path
    public static int g_disk_writer_threads; //disk writer thread count per store base path
    public static int g_extra_open_file_flags; //extra open file flags
    public static int g_file_distribute_path_mode;
    public static int g_file_distribute_rotate_count;
    public static long g_fsync_after_written_bytes;
    public static int g_dist_path_index_high; //current write to high path
    public static int g_dist_path_index_low;  //current write to low path
    public static int g_dist_write_file_count; //current write file count
    public static int g_storage_count;  //stoage server count in my group
    public static FDFSStorageServer[] g_storage_servers;
    public static int g_tracker_reporter_count;
    public static int g_heart_beat_interval;
    public static int g_stat_report_interval;
    public static int g_sync_wait_usec;
    public static int g_sync_interval; //unit: milliseconds
    public static LocalTime g_sync_start_time;
    public static LocalTime g_sync_end_time;
    public static boolean g_sync_part_time; //true for part time, false for all time of a day
    public static int g_sync_log_buff_interval; //sync log buff to disk every interval seconds
    public static int g_sync_binlog_buff_interval; //sync binlog buff to disk every interval seconds
    public static int g_write_mark_file_freq;      //write to mark file after sync N files
    public static int g_sync_stat_file_interval;   //sync storage stat info to disk interval
    public static FDFSStorageStat g_storage_stat = new FDFSStorageStat();
    public static int g_stat_change_count;
    public static int g_sync_change_count; //sync src timestamp change counter
    public static long g_storage_join_time;  //my join timestamp
    public static int g_sync_until_timestamp;
    public static boolean g_sync_old_done;     //if old files synced to me done
    public static String g_sync_src_id; //the source storage server id
    public static String g_group_name;
    public static String g_my_server_id_str; //my server id string
    public static String g_tracker_client_ip; //storage ip as tracker client
    public static String g_last_storage_ip;    //the last storage ip address
    public static String g_server_id_in_filename;

    // public static LogContext g_access_log_context;
    public static boolean g_store_slave_file_use_link; //if store slave file use symbol link
    public static boolean g_use_storage_id;  //identify storage by ID instead of IP address
    public static byte g_id_type_in_filename; //id type of the storage server in the filename
    public static boolean g_use_access_log;  //if log to access log
    public static boolean g_rotate_access_log;  //if rotate the access log every day
    public static boolean g_rotate_error_log;  //if rotate the error log every day
    public static LocalTime g_access_log_rotate_time; //rotate access log time base
    public static LocalTime g_error_log_rotate_time;  //rotate error log time base
    public static boolean g_check_file_duplicate;  //if check file content duplicate
    public static byte g_file_signature_method = STORAGE_FILE_SIGNATURE_METHOD_HASH; //file signature method
    public static String g_key_namespace;
    public static int g_namespace_len;
    public static int g_allow_ip_count;  /* -1 means match any ip address */
    public static String[] g_allow_ip_addrs;  /* sorted array, asc order */
    public static String g_run_by_gid;
    public static String g_run_by_uid;
    public static char[] g_run_by_group;
    public static char[] g_run_by_user;
    public static String g_bind_addr;
    public static boolean g_client_bind_addr;
    public static boolean g_storage_ip_changed_auto_adjust;
    public static boolean g_thread_kill_done;
    public static boolean g_file_sync_skip_invalid_record;
    public static int g_thread_stack_size;
    public static int g_upload_priority;
    public static long g_up_time;
    public static FdfsStorePathInfo[] g_path_space_list;
    static IniFileReader iniReader;

    public static void init(String conf_filename) throws IOException {
        iniReader = new IniFileReader(conf_filename);
        BASE_PATH = iniReader.getStrValue("base_path");
    }
//public static void debug(){
//    LOGGER.debug("MyBear v%d.%02d, base_path=%s, store_path_count=%d, " +
//            "subdir_count_per_path=%d, group_name=%s, " +
//            "run_by_group=%s, run_by_user=%s, " +
//            "connect_timeout=%ds, network_timeout=%ds, "+
//            "port=%d, bind_addr=%s, client_bind=%d, " +
//            "max_connections=%d, accept_threads=%d, " +
//            "work_threads=%d, "    +
//            "disk_rw_separated=%d, disk_reader_threads=%d, " +
//            "disk_writer_threads=%d, " +
//            "buff_size=%dKB, heart_beat_interval=%ds, " +
//            "stat_report_interval=%ds, tracker_server_count=%d, " +
//            "sync_wait_msec=%dms, sync_interval=%dms, " +
//            "sync_start_time=%02d:%02d, sync_end_time=%02d:%02d, "+
//            "write_mark_file_freq=%d, " +
//            "allow_ip_count=%d, " +
//            "file_distribute_path_mode=%d, " +
//            "file_distribute_rotate_count=%d, " +
//            "fsync_after_written_bytes=%d, " +
//            "sync_log_buff_interval=%ds, " +
//            "sync_binlog_buff_interval=%ds, " +
//            "sync_stat_file_interval=%ds, " +
//            "thread_stack_size=%d KB, upload_priority=%d, " +
//            "if_alias_prefix=%s, " +
//            "check_file_duplicate=%d, file_signature_method=%s, " +
//            "FDHT group count=%d, FDHT server count=%d, " +
//            "FDHT key_namespace=%s, FDHT keep_alive=%d, " +
//            "HTTP server port=%d, domain name=%s, " +
//            "use_access_log=%d, rotate_access_log=%d, " +
//            "access_log_rotate_time=%02d:%02d, " +
//            "rotate_error_log=%d, " +
//            "error_log_rotate_time=%02d:%02d, " +
//            "rotate_access_log_size=%PRId64"+
//            "rotate_error_log_size=%PRId64"+
//            "log_file_keep_days=%d, " +
//            "file_sync_skip_invalid_record=%d, " +
//            "use_connection_pool=%d, " +
//            "g_connection_pool_max_idle_time=%ds",
//                   0, 0,
//            g_fdfs_base_path, g_fdfs_store_paths.length,
//            g_subdir_count_per_path,
//            g_group_name, g_run_by_group, g_run_by_user,
//            g_fdfs_connect_timeout, g_fdfs_network_timeout,
//            g_server_port, g_bind_addr,
//            g_client_bind_addr, g_max_connections,
//            g_accept_threads, g_work_threads, g_disk_rw_separated,
//            g_disk_reader_threads, g_disk_writer_threads,
//            g_buff_size / 1024,
//            g_heart_beat_interval, g_stat_report_interval,
//            g_tracker_group.tracker_servers.length, g_sync_wait_usec / 1000,
//            g_sync_interval / 1000,
//            g_sync_start_time.getHour(), g_sync_start_time.getMinute(),
//            g_sync_end_time.getHour(), g_sync_end_time.getMinute(),
//            g_write_mark_file_freq,
//            g_allow_ip_count, g_file_distribute_path_mode,
//            g_file_distribute_rotate_count,
//            g_fsync_after_written_bytes, g_sync_log_buff_interval,
//            g_sync_binlog_buff_interval, g_sync_stat_file_interval,
//            g_thread_stack_size/1024, g_upload_priority,
////            g_if_alias_prefix,
//            g_check_file_duplicate,
//            g_file_signature_method == STORAGE_FILE_SIGNATURE_METHOD_HASH
//                    ? "hash" : "md5",
////            g_group_array.group_count, g_group_array.server_count,
//            0,0,
//            g_key_namespace, g_keep_alive,
//            g_http_port, g_http_domain, g_use_access_log,
//            g_rotate_access_log, g_access_log_rotate_time,
//            g_access_log_rotate_time.getMinute(),
//            g_rotate_error_log, g_error_log_rotate_time.getHour(),
//            g_error_log_rotate_time.getMinute(),
////            g_access_log_context.rotate_size,
////            g_log_context.rotate_size,
//            0,0,
//            g_log_file_keep_days,
//            g_file_sync_skip_invalid_record,
//          //  g_use_connection_pool, g_connection_pool_max_idle_time);
//            null, null);
//}
}
