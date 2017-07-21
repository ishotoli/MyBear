package io.mybear.storage;

import com.sun.management.UnixOperatingSystemMXBean;
import io.mybear.common.*;
import io.mybear.storage.fdhtClient.FdhtClient;
import io.mybear.storage.storageNio.*;
import io.mybear.storage.storageNio.StorageClientInfo;
import io.mybear.storage.trunkMgr.TrunkShared;
import io.mybear.tracker.types.TrackerServerGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

import static io.mybear.common.FdfsDefine.*;
import static io.mybear.common.FdfsGlobal.*;
import static io.mybear.storage.StorageGlobal.*;
import static io.mybear.storage.StorageGlobal.DEFAULT_SYNC_STAT_FILE_INTERVAL;
import static io.mybear.storage.StorageGlobal.g_accept_threads;
import static io.mybear.storage.StorageGlobal.g_buff_size;
import static io.mybear.storage.StorageGlobal.g_check_file_duplicate;
import static io.mybear.storage.StorageGlobal.g_disk_reader_threads;
import static io.mybear.storage.StorageGlobal.g_disk_rw_separated;
import static io.mybear.storage.StorageGlobal.g_disk_writer_threads;
import static io.mybear.storage.StorageGlobal.g_file_distribute_rotate_count;
import static io.mybear.storage.StorageGlobal.g_group_name;
import static io.mybear.storage.StorageGlobal.g_heart_beat_interval;
import static io.mybear.storage.StorageGlobal.g_http_domain;
import static io.mybear.storage.StorageGlobal.g_max_connections;
import static io.mybear.storage.StorageGlobal.g_stat_report_interval;
import static io.mybear.storage.StorageGlobal.g_sync_binlog_buff_interval;
import static io.mybear.storage.StorageGlobal.g_sync_end_time;
import static io.mybear.storage.StorageGlobal.g_sync_interval;
import static io.mybear.storage.StorageGlobal.g_sync_log_buff_interval;
import static io.mybear.storage.StorageGlobal.g_sync_start_time;
import static io.mybear.storage.StorageGlobal.g_sync_stat_file_interval;
import static io.mybear.storage.StorageGlobal.g_sync_wait_usec;
import static io.mybear.storage.StorageGlobal.g_use_access_log;
import static io.mybear.storage.StorageGlobal.g_work_threads;
import static io.mybear.storage.StorageGlobal.g_write_mark_file_freq;
import static io.mybear.storage.trunkMgr.TrunkSync.TRUNK_BINLOG_BUFFER_SIZE;
import static io.mybear.tracker.TrackerTypes.*;
import static org.csource.fastdfs.ClientGlobal.DEFAULT_NETWORK_TIMEOUT;
import static sun.net.NetworkClient.DEFAULT_CONNECT_TIMEOUT;

/**
 * Created by jamie on 2017/6/21.
 */
public class FdfsStoraged {
    public static final int SCHEDULE_ENTRIES_MAX_COUNT = 9;
    public static final int MAX_PATH_SIZE = 256;
    public static final long g_current_time = System.currentTimeMillis();
    public static final boolean DEBUG_FLAG = true;
    private static final Logger LOGGER = LoggerFactory.getLogger(FdfsStoraged.class);
    public static long g_up_time = g_current_time;
    static String TRACKER_ERROR_LOG_FILENAME = "trackerd";
    static String STORAGE_ERROR_LOG_FILENAME = "storaged";

    public static void main(String[] args) throws Exception {
        main0(new String[]{"storage.conf", "start"});
    }

    public static void main0(String[] args) throws Exception {
        String confFilename;
        int result;
        int sock;
        int waitCount;
        Thread scheduleTid;
        ScheduleEntry[] scheduleEntry = new ScheduleEntry[SCHEDULE_ENTRIES_MAX_COUNT];
        ScheduleArray scheduleArray;
        boolean stop = false;
        if (args.length < 2) {
            usage(args[0]);
            return;
        }
        confFilename = args[0];
        String conf = ProcessAction.getBasePathFromConfFile(confFilename);
        Path pidFilename = Paths.get(g_fdfs_base_path, "/data/fdfs_storaged.pid");
        if (!ProcessAction.processAction(pidFilename, args[1])) {
            return;
        }
        StorageGlobal.exeName = Paths.get(args[0]).toAbsolutePath();
        if (DEBUG_FLAG && StorageGlobal.exeName == null) {
            LOGGER.error("exit abnormally!\n");
            return;
        }
        String bindAddr = storageFuncInit(conf);
        StorageDio.init();
        TrunkShared.trunkSharedInit();
        socketServer(bindAddr, StorageGlobal.G_SERVER_PORT);
    }

    static boolean storageLoadPaths(IniFileReader iniFileReader) {
        return true;
    }

    static void loadLogLevel(IniFileReader iniFileReader) {
        return;
    }

    static boolean logSetPrefix(String path, String filename) {
        return true;
    }

    static boolean fdfsLoadTrackerGroupEx(TrackerServerGroup trackerGroup, Path filename) {
        return true;
    }

    static int storageGetGroupNameFromTracker() {
        return 0;
    }

    static int fdfsValidateGroupName(String groupName) {
        return 0;
    }

    public static long parseBytes(String pStr, int default_unit_bytes) {
        if (pStr == null) return 0;
        pStr = pStr.trim();
        if ("".equals(pStr)) return 0;
        if (Character.isDigit(pStr.charAt(pStr.length() - 1))) return Long.parseLong(pStr);
        long res = Long.parseLong(pStr.substring(0, pStr.length() - 2));
        char c = pStr.charAt(pStr.length() - 2);
        switch (c) {
            case 'G':
            case 'g':
                return res * 1024 * 1024 * 1024;
            case 'M':
            case 'm':
                return res * 1024 * 1024;
            case 'K':
            case 'k':
                return res * 1024;
            default:
                return default_unit_bytes * res;
        }
    }

    static String getegid() {
        return "";
    }

    /**
     * @param filename
     * @return bind_addr
     */
    static String storageFuncInit(final String filename) {

        // boolean g_client_bind_addr;
        String pGroupName;
        String pRunByGroup;
        String pRunByUser;
        String pFsyncAfterWrittenBytes;
        String pThreadStackSize;
        String pBuffSize;
        String pIfAliasPrefix;
        String pHttpDomain;
        String pRotateAccessLogSize;
        String pRotateErrorLogSize;

        long fsync_after_written_bytes;

        long buff_size;
        long rotate_access_log_size;
        long rotate_error_log_size;

        String pBindAddr = "";

        IniFileReader iniContext = null;
        try {
            iniContext = StorageGlobal.iniReader;
        } catch (Exception e) {
            LOGGER.error("load conf file \"%s\" fail, ret code: %d", filename);
        }
        int result = 0;
        do {
            if (iniContext.getBoolValue("disabled", false)) {
                LOGGER.info("conf file \\\"%s\\\" disabled=true, exit", filename);
                //  所有配置项失效
                result = 1;
            }
            g_subdir_count_per_path = iniContext.getIntValue("subdir_count_per_path", StorageGlobal.DEFAULT_DATA_DIR_COUNT_PER_PATH);
            if (g_subdir_count_per_path < 0 || g_subdir_count_per_path > 256) {
                LOGGER.info("conf file \\\"%s\\\", invalid subdir_count: %d", filename, g_subdir_count_per_path);
                result = -1;
            }
            //初始化路径信息
            if (StorageFunc.storageLoadPaths(iniContext) != 0) {
                break;
            }
            loadLogLevel(iniContext);
            if (!logSetPrefix(g_fdfs_base_path, STORAGE_ERROR_LOG_FILENAME)) {
                break;
            }
            FdfsGlobal.g_fdfs_connect_timeout = iniContext.getIntValue("connect_timeout", DEFAULT_CONNECT_TIMEOUT);

            if (FdfsGlobal.g_fdfs_connect_timeout <= 0) {
                FdfsGlobal.g_fdfs_connect_timeout = DEFAULT_CONNECT_TIMEOUT;
            }
            g_fdfs_network_timeout = iniContext.getIntValue("network_timeout", DEFAULT_NETWORK_TIMEOUT);
            if (g_fdfs_network_timeout <= 0) {
                g_fdfs_network_timeout = DEFAULT_NETWORK_TIMEOUT;
            }
            g_server_port = iniContext.getIntValue("port",
                    FDFS_STORAGE_SERVER_DEF_PORT);
            if (g_server_port <= 0) {
                g_server_port = FDFS_STORAGE_SERVER_DEF_PORT;
            }
            g_heart_beat_interval = iniContext.getIntValue(
                    "heart_beat_interval",
                    STORAGE_BEAT_DEF_INTERVAL);
            if (g_heart_beat_interval <= 0) {
                g_heart_beat_interval = STORAGE_BEAT_DEF_INTERVAL;
            }
            g_stat_report_interval = iniContext.getIntValue(
                    "stat_report_interval",
                    STORAGE_REPORT_DEF_INTERVAL);
            if (g_stat_report_interval <= 0) {
                g_stat_report_interval = STORAGE_REPORT_DEF_INTERVAL;
            }
            pBindAddr = iniContext.getStrValue("bind_addr");
            if (pBindAddr == null) {
                pBindAddr = "";
            } else {
                LOGGER.info(pBindAddr);
            }
            StorageGlobal.g_client_bind_addr = iniContext.getBoolValue("client_bind", true);
//            if (!fdfsLoadTrackerGroupEx(g_tracker_group, filename)) {
//                break;
//            }
//            int len = g_tracker_group.tracker_servers.length;
//            for (InetSocketAddress it : g_tracker_group.tracker_servers) {
//                if ("127.0.0.1".equals(it.getHostString())) {
//                    LOGGER.error("conf file \"%s\", " + "tracker: \"%s:%d\" is invalid, tracker server ip can't be 127.0.0.1", filename, it.getHostString(), it.getPort());
//                    result = -1;
//                    break;
//                }
//            }
            if (result != 0) {
                break;
            }
            pGroupName = iniContext.getStrValue("group_name");
            if (pGroupName == null) {
                result = storageGetGroupNameFromTracker();
                if (result == 0) {
                    LOGGER.info("get group name from tracker server, group_name: %s", g_group_name);
                } else {
                    LOGGER.error("conf file \"%s\" must have item \"group_name\"!", filename);
                    result = -1;
                    break;
                }
            } else if ("".equals(pGroupName.trim())) {
                LOGGER.error("conf file \"%s\", group_name is empty!", filename);
                result = -1;
                break;
            } else {
                g_group_name = pGroupName;
                LOGGER.info(g_group_name);
            }
            if ((result = fdfsValidateGroupName(g_group_name)) != 0) {
                LOGGER.error("conf file \"%s\"the group name \"%s\" is invalid!", filename, g_group_name);
                result = -1;
                break;
            }
            g_sync_wait_usec = iniContext.getIntValue("sync_wait_msec", STORAGE_DEF_SYNC_WAIT_MSEC);
            if (g_sync_wait_usec <= 0) {
                g_sync_wait_usec = STORAGE_DEF_SYNC_WAIT_MSEC;
            }
            g_sync_interval = iniContext.getIntValue("sync_interval", 0);
            if (g_sync_interval < 0) {
                g_sync_interval = 0;
            }
            g_sync_interval *= 1000;
            try {
                g_sync_start_time = LocalTime.parse(iniContext.getStrValue(
                        "sync_start_time"));
            } catch (Exception e) {
                e.printStackTrace();
                g_sync_start_time = LocalTime.parse("0:0");
                result = -1;
                break;
            }
            try {
                g_sync_end_time = LocalTime.parse(iniContext.getStrValue("sync_end_time"));
            } catch (Exception e) {
                e.printStackTrace();
                result = -1;
                g_sync_start_time = LocalTime.parse("23:59");
                break;
            }
            StorageGlobal.g_sync_part_time = !((g_sync_start_time.getHour() == 0 &&
                    g_sync_start_time.getMinute() == 0) &&
                    (g_sync_end_time.getHour() == 23 &&
                            g_sync_end_time.getMinute() == 59));

            g_max_connections = iniContext.getIntValue("max_connections", DEFAULT_MAX_CONNECTONS);
            if (g_max_connections <= 0) {
                g_max_connections = DEFAULT_MAX_CONNECTONS;
            }
            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            if (os instanceof UnixOperatingSystemMXBean) {
                long count = ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount();
                LOGGER.info("Number of open fd: %d", count);
                if (count < g_max_connections) {
                    break;
                }
            }
            g_accept_threads = iniContext.getIntValue("accept_threads", 1);
            if (g_accept_threads <= 0) {
                LOGGER.error("item \"accept_threads\" is invalid, " + "value: %d <= 0!", g_accept_threads);
                result = -1;
                break;
            }
            g_work_threads = iniContext.getIntValue("work_threads", 4);
            if (g_work_threads <= 0) {
                LOGGER.error("item \"work_threads\" is invalid,value: %d <= 0!", g_work_threads);
                result = -1;
                break;
            }
            pBuffSize = iniContext.getStrValue("buff_size");
            if (pBuffSize == null) {
                buff_size = STORAGE_DEFAULT_BUFF_SIZE;
            } else {
                try {
                    buff_size = parseBytes(pBuffSize, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            g_buff_size = buff_size;
            if (g_buff_size < 4 * 1024 || g_buff_size < (10 + TRUNK_BINLOG_BUFFER_SIZE)) {
                LOGGER.error("item \"buff_size\" is too small, value: %d < %d or < %d!", g_buff_size, 4 * 1024, 10 + TRUNK_BINLOG_BUFFER_SIZE);
                result = -1;
                break;
            }
            g_disk_rw_separated = iniContext.getBoolValue("disk_rw_separated", true);
            g_disk_reader_threads = iniContext.getIntValue("disk_reader_threads", DEFAULT_DISK_READER_THREADS);
            if (g_disk_reader_threads < 0) {
                LOGGER.error("item \"disk_reader_threads\" is invalid, value: %d < 0!", g_disk_reader_threads);
                result = -1;
                break;
            }
            g_disk_writer_threads = iniContext.getIntValue("disk_writer_threads", DEFAULT_DISK_WRITER_THREADS);
            if (g_disk_writer_threads < 0) {
                LOGGER.error("item \"disk_writer_threads\" is invalid, value: %d < 0!", g_disk_writer_threads);
                result = -1;
                break;
            }
            if (g_disk_rw_separated) {
                if (g_disk_reader_threads == 0) {
                    LOGGER.error("item \"disk_reader_threads\" is invalid, value = 0!");
                    result = -1;
                    break;
                }

                if (g_disk_writer_threads == 0) {
                    LOGGER.error("item \"disk_writer_threads\" is invalid, value = 0!");
                    result = -1;
                    break;
                }
            } else if (g_disk_reader_threads + g_disk_writer_threads == 0) {
                LOGGER.error("item \"disk_reader_threads\" and \"disk_writer_threads\" are invalid, both value = 0!");
                result = -1;
                break;
            }

//            pRunByGroup = iniContext.getStrValue("run_by_group");
//            pRunByUser = iniContext.getStrValue("run_by_user");
//            if (pRunByGroup == null) {
//                g_run_by_group = new char[]{};
//            } else {
//                LOGGER.info(pRunByGroup);
//            }
//            if (g_run_by_group.length == 0) {
//                StorageGlobal.g_run_by_gid = getegid();
//            } else {
//                struct group *pGroup;
//                pGroup = getgrnam(g_run_by_group);
//                if (pGroup == null) {
//                    result = errno != 0 ? errno : ENOENT;
//                    LOGGER.error(
//                            "getgrnam fail, errno: %d, " \
//                            "error info: %s", , \
//                            result, STRERROR(result));
//                    return result;
//                }
            //  StorageGlobal.g_run_by_gid = pGroup -> gr_gid;
//            }

//            if (pRunByUser == null) {
//                StorageGlobal.g_run_by_user = new char[]{};
//            } else {
//                LOGGER.info(g_run_by_user, sizeof(g_run_by_user),
//                        "%s", pRunByUser);
//            }
//            if (g_run_by_user == '\0') {
//                StorageGlobal.g_run_by_uid = geteuid();
//            } else {
//                struct passwd *pUser;
//
//                pUser = getpwnam(g_run_by_user);
//                if (pUser == null) {
//                    result = errno != 0 ? errno : ENOENT;
//                    LOGGER.error("getpwnam fail, errno: %d,error info: %s", result, STRERROR(result));
//                    return result;
//                }
//
//                g_run_by_uid = pUser -> pw_uid;
//            }
//            if ((result = load_allow_hosts( & g_allow_ip_addrs, &g_allow_ip_count)) !=0)
//            {
//                return result;
//            }

            StorageGlobal.g_file_distribute_path_mode = iniContext.getIntValue("file_distribute_path_mode", FDFS_FILE_DIST_PATH_ROUND_ROBIN);
            g_file_distribute_rotate_count = iniContext.getIntValue("file_distribute_rotate_count", FDFS_FILE_DIST_DEFAULT_ROTATE_COUNT);
            if (g_file_distribute_rotate_count <= 0) {
                g_file_distribute_rotate_count = FDFS_FILE_DIST_DEFAULT_ROTATE_COUNT;
            }

            pFsyncAfterWrittenBytes = iniContext.getStrValue("fsync_after_written_bytes");
            if (pFsyncAfterWrittenBytes == null) {
                fsync_after_written_bytes = 0;
            } else {
                try {
                    fsync_after_written_bytes = parseBytes(pFsyncAfterWrittenBytes, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            StorageGlobal.g_fsync_after_written_bytes = fsync_after_written_bytes;

            final int SYNC_LOG_BUFF_DEF_INTERVAL = 10;
            g_sync_log_buff_interval = iniContext.getIntValue("sync_log_buff_interval", SYNC_LOG_BUFF_DEF_INTERVAL);
            if (g_sync_log_buff_interval <= 0) {
                g_sync_log_buff_interval = SYNC_LOG_BUFF_DEF_INTERVAL;
            }

            g_sync_binlog_buff_interval = iniContext.getIntValue("sync_binlog_buff_interval", SYNC_BINLOG_BUFF_DEF_INTERVAL);
            if (g_sync_binlog_buff_interval <= 0) {
                g_sync_binlog_buff_interval = SYNC_BINLOG_BUFF_DEF_INTERVAL;
            }

            g_write_mark_file_freq = iniContext.getIntValue(
                    "write_mark_file_freq",
                    FDFS_DEFAULT_SYNC_MARK_FILE_FREQ);
            if (g_write_mark_file_freq <= 0) {
                g_write_mark_file_freq = FDFS_DEFAULT_SYNC_MARK_FILE_FREQ;
            }


            g_sync_stat_file_interval = iniContext.getIntValue("sync_stat_file_interval", DEFAULT_SYNC_STAT_FILE_INTERVAL);
            if (g_sync_stat_file_interval <= 0) {
                g_sync_stat_file_interval = DEFAULT_SYNC_STAT_FILE_INTERVAL;
            }

//            pThreadStackSize = iniContext.getStrValue("thread_stack_size");
//            if (pThreadStackSize == null) {
//                thread_stack_size = 512 * 1024;
//            } else if ((result = parseBytes(pThreadStackSize, 1, \ & thread_stack_size)) !=0){
//                break;
//            }
//            g_thread_stack_size = (int) thread_stack_size;
//            if (g_thread_stack_size < FAST_WRITE_BUFF_SIZE + 64 * 1024) {
//                LOGGER.error("item \"thread_stack_size\" %d is invalid, which < %d", , g_thread_stack_size, FAST_WRITE_BUFF_SIZE + 64 * 1024);
//                result = -1;
//                break;
//            }
            StorageGlobal.g_upload_priority = iniContext.getIntValue("upload_priority", DEFAULT_UPLOAD_PRIORITY);
            pIfAliasPrefix = iniContext.getStrValue("if_alias_prefix");
//            if (pIfAliasPrefix == null) {
//                StorageGlobal.g_if_alias_prefix = '\0';
//            } else {
//                LOGGER.info(g_if_alias_prefix, sizeof(g_if_alias_prefix), "%s", pIfAliasPrefix);
//            }
            g_check_file_duplicate = iniContext.getBoolValue("check_file_duplicate", false);
            if (g_check_file_duplicate) {
//            char *pKeyNamespace;
//            char *pFileSignatureMethod;
//                pFileSignatureMethod = iniContext.getStrValue("file_signature_method");
//                if (pFileSignatureMethod != null && strcasecmp( \
//                        pFileSignatureMethod, "md5") == 0) {
//                    g_file_signature_method = \
//                    STORAGE_FILE_SIGNATURE_METHOD_MD5;
//                } else {
//                    g_file_signature_method = \
//                    STORAGE_FILE_SIGNATURE_METHOD_HASH;
//                }
//                strcpy(g_fdht_base_path, g_fdfs_base_path);
//                g_fdht_connect_timeout = g_fdfs_connect_timeout;
//                g_fdht_network_timeout = g_fdfs_network_timeout;
//                StorageGlobal.pKeyNamespace = iniContext.getStrValue("key_namespace")
//                if (pKeyNamespace == null || *pKeyNamespace == '\0'){
//                    LOGGER.error("item \"key_namespace\" does not exist or is empty");
//                    result = -1;
//                    break;
//                }
//                g_namespace_len = strlen(pKeyNamespace);
//                if (g_namespace_len >= sizeof(g_key_namespace)) {
//                    g_namespace_len = sizeof(g_key_namespace) - 1;
//                }
//                memcpy(g_key_namespace, pKeyNamespace, g_namespace_len);
//            *(g_key_namespace + g_namespace_len) = '\0';
//                if ((result = fdht_load_groups( & g_group_array)) !=0){
//                    break;
//                }
                FdhtClient.g_keep_alive = iniContext.getBoolValue("keep_alive", false);
            }
            g_http_port = iniContext.getIntValue("http.server_port", 80);
            if (g_http_port <= 0) {
                LOGGER.error("invalid param \"http.server_port\": %d", g_http_port);
                result = -1;
                break;
            }
            pHttpDomain = iniContext.getStrValue("http.domain_name");
            if (pHttpDomain == null) {
                g_http_domain = "";
            } else {
                g_http_domain = pHttpDomain;
                LOGGER.info(g_http_domain);
            }
            g_use_access_log = iniContext.getBoolValue("use_access_log", false);
//            if (g_use_access_log) {
//                result = log_init_ex( & g_access_log_context);
//                if (result != 0) {
//                    break;
//                }
//                log_set_time_precision( & g_access_log_context, \
//                LOG_TIME_PRECISION_MSECOND);
//                log_set_cache_ex( & g_access_log_context, true);
//                result = log_set_prefix_ex( & g_access_log_context, \
//                g_fdfs_base_path, "storage_access");
//                if (result != 0) {
//                    break;
//                }
//                log_set_header_callback( & g_access_log_context, storage_set_access_log_header);
//            }
//            g_rotate_access_log = iniContext.getBoolValue("rotate_access_log", false);
//            if ((result = get_time_item_from_conf("access_log_rotate_time", & g_access_log_rotate_time,0, 0)) !=0){
//                break;
//            }
//            g_rotate_error_log = iniContext.getBoolValue("rotate_error_log", false);
//            if ((result = get_time_item_from_conf("error_log_rotate_time", & g_error_log_rotate_time,0, 0)) !=0){
//                break;
//            }
            pRotateAccessLogSize = iniContext.getStrValue("rotate_access_log_size");
            if (pRotateAccessLogSize == null) {
                rotate_access_log_size = 0;
            } else {
                try {
                    rotate_access_log_size = parseBytes(pRotateAccessLogSize, 1);
                } catch (Exception e) {
                    break;
                }
            }
            if (rotate_access_log_size > 0 && rotate_access_log_size < FDFS_ONE_MB) {
                LOGGER.info("item \"rotate_access_log_size\": % PRId64 is too small, change to 1 MB", rotate_access_log_size);
                rotate_access_log_size = FDFS_ONE_MB;
            }
//            fdfs_set_log_rotate_size( & g_access_log_context, rotate_access_log_size);
            pRotateErrorLogSize = iniContext.getStrValue("rotate_error_log_size");
            if (pRotateErrorLogSize == null) {
                rotate_error_log_size = 0;
            } else {
                try {
                    rotate_error_log_size = parseBytes(pRotateErrorLogSize, 1);
                } catch (Exception e) {
                    break;
                }
            }
            if (rotate_error_log_size > 0 && rotate_error_log_size < FDFS_ONE_MB) {
                LOGGER.info("item \"rotate_error_log_size\": " + "% PRId64 is too small, " + "change to 1 MB", rotate_error_log_size);
                rotate_error_log_size = FDFS_ONE_MB;
            }
//            fdfs_set_log_rotate_size( & g_log_context, rotate_error_log_size);
            g_log_file_keep_days = iniContext.getIntValue("log_file_keep_days", 0);
            StorageGlobal.g_file_sync_skip_invalid_record = iniContext.getBoolValue("file_sync_skip_invalid_record", false);
//            if ((result = fdfs_connection_pool_init(filename, & iniContext)) !=0){
//                break;
//            }
        } while (false);


//            String g_client_bind_addr = iniContext.getStrValue("client_bind");
//            String pGroupName = iniContext.getStrValue("group_name");
//            String pRunByGroup = iniContext.getStrValue("run_by_group");
//            String pRunByUser = iniContext.getStrValue("run_by_user");
//            String pFsyncAfterWrittenBytes = iniContext.getStrValue("fsync_after_written_bytes");
//            String pThreadStackSize = iniContext.getStrValue("thread_stack_size");
//            String pBuffSize = iniContext.getStrValue("buff_size");
//            String pIfAliasPrefix = iniContext.getStrValue("if_alias_prefix");
//            String pHttpDomain = iniContext.getStrValue("http.domain_name");
//            String pRotateAccessLogSize = iniContext.getStrValue("rotate_access_log_size");
//            String pRotateErrorLogSize = iniContext.getStrValue("rotate_error_log_size");
//
//
//      pBindAddr = iniContext.getStrValue("bind_addr");
//
//            if (pBindAddr == null) pBindAddr = "";
//            else LOGGER.info("");
        if (!StorageFunc.storageCheckAndMakeDataDirs()) {
            LOGGER.info("storage_check_and_make_data_dirs fail,program exit!");
            return "";
        }

        return pBindAddr;
    }


    static void socketServer(String g_bind_addr, int g_server_port) throws IOException {
        // Business Executor ，用来执行那些耗时的任务
        NameableExecutor businessExecutor = ExecutorUtil.create("BusinessExecutor", 1);
        // 定时器Executor，用来执行定时任务
        NamebleScheduledExecutor timerExecutor = ExecutorUtil.createSheduledExecute("Timer", 1);
        timerExecutor.scheduleAtFixedRate(TimeUtil::update, 0, 1, TimeUnit.SECONDS);
        /// timerExecutor.scheduleAtFixedRate(TimeUtil::update,0,1, TimeUnit.SECONDS);
        SharedBufferPool sharedPool = new SharedBufferPool(1024 * 1024 * 100, 1024);
        new NetSystem(sharedPool, businessExecutor, timerExecutor);
        // timerExecutor.scheduleAtFixedRate(()->NetSystem.getInstance().firstReadIdleCheck(),30,30,TimeUnit.SECONDS);
        // Reactor pool
        NIOReactorPool reactorPool = new NIOReactorPool("Reactor Pool", 2, sharedPool);
        NIOConnector connector = new NIOConnector("NIOConnector", reactorPool);
        connector.start();
        NetSystem.getInstance().setConnector(connector);
        NetSystem.getInstance().setNetConfig(new SystemConfig());


        ConnectionFactory frontFactory = new ConnectionFactory() {
            @Override
            protected Connection makeConnection(SocketChannel channel) throws IOException {
                Connection c = new StorageClientInfo(channel);
                c.setIdleTimeout(30 * 1000L);//30s
                return c;
            }

            @Override
            protected NIOHandler getNIOHandler() {
                return null;
            }
        };
        NIOAcceptor server = new NIOAcceptor("Server", g_bind_addr, g_server_port, frontFactory, reactorPool);
        server.start();
    }

    static void usage(String program) {
        LOGGER.error("Usage: %s <config_file> [start | stop | restart]\n", program);
    }
}
