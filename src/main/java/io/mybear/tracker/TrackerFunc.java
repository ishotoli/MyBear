package io.mybear.tracker;

import com.alibaba.fastjson.JSON;
import io.mybear.common.CommonDefine;
import io.mybear.common.FdfsDefine;
import io.mybear.common.FdfsGlobal;
import io.mybear.common.IniFileReader;
import io.mybear.common.constants.CommonConstant;
import io.mybear.common.utils.Utils;
import io.mybear.tracker.types.Contants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.mybear.tracker.TrackerGlobal.*;
import static org.csource.fastdfs.ClientGlobal.DEFAULT_CONNECT_TIMEOUT;
import static org.csource.fastdfs.ClientGlobal.DEFAULT_NETWORK_TIMEOUT;

/**
 * @author yangll
 * Created by jamie on 2017/6/21.
 */
public class TrackerFunc {

    public static final Logger log = LoggerFactory.getLogger(TrackerFunc.class);

    public static int trackerLoadFromConfFile(String fileName, String bindAddr) {
        IniFileReader iniReader;
        try {
            iniReader = new IniFileReader(fileName);
        } catch (IOException e) {
            log.error("read config file faile,error:" + e.getMessage(), e);
            return -1;
        }

        boolean disabled = iniReader.getBoolValue("disabled", false);
        if (!disabled) {
            log.error(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader),
                    String.format("config file : %s disabled=true,exit", fileName));
            return -1;
        }

        char[] pPath = iniReader.getStrValue("base_path") == null ? null : iniReader.getStrValue("base_path")
                .toCharArray();
        if (pPath == null) {
            log.error(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), "base_path值为null");
            return -1;
        }

        pPath = SharedFunc.chopPath(pPath);
        FdfsGlobal.g_fdfs_base_path = new String(pPath);
        if (!SharedFunc.fileExists(FdfsGlobal.g_fdfs_base_path)) {
            log.error(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), FdfsGlobal.g_fdfs_base_path + "路径不存在");
            return -1;
        }

        if (!SharedFunc.isDir(FdfsGlobal.g_fdfs_base_path)) {
            log.error(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), FdfsGlobal.g_fdfs_base_path + " is not a directory!");
            return -1;
        }

        FdfsGlobal.g_fdfs_connect_timeout = iniReader.getIntValue("connect_time", DEFAULT_CONNECT_TIMEOUT);
        if (FdfsGlobal.g_fdfs_connect_timeout <= 0) {
            FdfsGlobal.g_fdfs_connect_timeout = DEFAULT_CONNECT_TIMEOUT;
        }

        FdfsGlobal.g_fdfs_network_timeout = iniReader.getIntValue("network_timeout", DEFAULT_NETWORK_TIMEOUT);
        if (FdfsGlobal.g_fdfs_network_timeout <= 0) {
            FdfsGlobal.g_fdfs_network_timeout = DEFAULT_NETWORK_TIMEOUT;
        }

        FdfsGlobal.g_server_port = iniReader.getIntValue("port", FdfsDefine.FDFS_TRACKER_SERVER_DEF_PORT);
        if (FdfsGlobal.g_server_port <= 0) {
            FdfsGlobal.g_server_port = FdfsDefine.FDFS_TRACKER_SERVER_DEF_PORT;
        }

        String pBindAddr = iniReader.getStrValue("bind_addr");
        bindAddr = "";

        if (trackerLoadStoreLookup(fileName, iniReader) != 0) {
            return 0;
        }

        TrackerGlobal.ggroups.setStoreServer((byte) iniReader.getIntValue("store_server", Contants.FDFS_STORE_SERVER_ROUND_ROBIN));
        if (!(TrackerGlobal.ggroups.getStoreServer() == Contants.FDFS_STORE_SERVER_FIRST_BY_IP
                || TrackerGlobal.ggroups.getStoreServer() == Contants.FDFS_STORE_SERVER_FIRST_BY_PRI
                || TrackerGlobal.ggroups.getStoreServer() == Contants.FDFS_STORE_SERVER_ROUND_ROBIN)) {
            log.warn(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), "store_server 's value %d is invalid, set to %d (round robin)", TrackerGlobal.ggroups.getStoreServer(), Contants.FDFS_STORE_SERVER_ROUND_ROBIN);
            TrackerGlobal.ggroups.setStoreServer(Contants.FDFS_STORE_SERVER_ROUND_ROBIN);
        }

        TrackerGlobal.ggroups.setDownloadServer((byte) iniReader.getIntValue("download_server", Contants.FDFS_STORE_SERVER_ROUND_ROBIN));
        if (!(TrackerGlobal.ggroups.getDownloadServer() == Contants.FDFS_DOWNLOAD_SERVER_ROUND_ROBIN
                || TrackerGlobal.ggroups.getDownloadServer() == Contants.FDFS_DOWNLOAD_SERVER_SOURCE_FIRST)) {
            log.warn(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), "download_server 's value %d is invalid, set to %d (round robin)", TrackerGlobal.ggroups.getDownloadServer(), Contants.FDFS_DOWNLOAD_SERVER_ROUND_ROBIN);
            TrackerGlobal.ggroups.setDownloadServer(Contants.FDFS_DOWNLOAD_SERVER_ROUND_ROBIN);
        }

        TrackerGlobal.ggroups.setDownloadServer((byte) iniReader.getIntValue("download_server", Contants.FDFS_STORE_SERVER_ROUND_ROBIN));
        if (!(TrackerGlobal.ggroups.getDownloadServer() == Contants.FDFS_DOWNLOAD_SERVER_ROUND_ROBIN
                || TrackerGlobal.ggroups.getDownloadServer() == Contants.FDFS_DOWNLOAD_SERVER_SOURCE_FIRST)) {
            log.warn(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), "download_server 's value %d is invalid, set to %d (round robin)", TrackerGlobal.ggroups.getDownloadServer(), Contants.FDFS_DOWNLOAD_SERVER_ROUND_ROBIN);
            TrackerGlobal.ggroups.setDownloadServer(Contants.FDFS_DOWNLOAD_SERVER_ROUND_ROBIN);
        }

        TrackerGlobal.ggroups.setStorePath((byte) iniReader.getIntValue("store_path", Contants.FDFS_STORE_PATH_ROUND_ROBIN));
        if (!(TrackerGlobal.ggroups.getStorePath() == Contants.FDFS_STORE_PATH_ROUND_ROBIN
                || TrackerGlobal.ggroups.getStorePath() == Contants.FDFS_STORE_PATH_LOAD_BALANC)) {
            log.warn(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), "store_path 's value %d is invalid, set to %d (round robin)", TrackerGlobal.ggroups.getStorePath(), Contants.FDFS_STORE_PATH_ROUND_ROBIN);
            TrackerGlobal.ggroups.setDownloadServer(Contants.FDFS_DOWNLOAD_SERVER_ROUND_ROBIN);
        }

        //TODO
//        if ((result=fdfs_parse_storage_reserved_space(&iniContext, \
//        &g_storage_reserved_space)) != 0)
//        {
//            break;
//        }

        TrackerGlobal.g_max_connections = iniReader.getIntValue("max_connections", FdfsDefine.DEFAULT_MAX_CONNECTONS);
        if (TrackerGlobal.g_max_connections <= 0) {
            TrackerGlobal.g_max_connections = CommonDefine.DEFAULT_MAX_CONNECTONS;
        }

        TrackerGlobal.g_accept_threads = iniReader.getIntValue("accept_threads", 1);
        if (TrackerGlobal.g_accept_threads <= 0) {
            log.error(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), "item \"accept_threads\" is invalid, value: %d <=0!", TrackerGlobal.g_accept_threads);
            return -1;
        }

        TrackerGlobal.g_work_threads = iniReader.getIntValue("work_threads", CommonDefine.DEFAULT_WORK_THREADS);
        if (TrackerGlobal.g_work_threads <= 0) {
            log.error(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), "item \"work_threads\" is invalid, value: %d <=0!", TrackerGlobal.g_work_threads);
            return -1;
        }

        //TODO
//        if ((result=set_rlimit(RLIMIT_NOFILE, g_max_connections)) != 0)
//        {
//            break;
//        }

        String pRunByGroup = iniReader.getStrValue("run_by_group");
        String pRunByUser = iniReader.getStrValue("run_by_user");
        if (pRunByGroup == null || pRunByGroup.equals("")) {
            TrackerGlobal.g_run_by_group = "";
        }

        if (TrackerGlobal.g_run_by_group.equals("")) {
            TrackerGlobal.g_run_by_gid = getegid();
        } else {//TODO
//            struct group *pGroup;
//
//            pGroup = getgrnam(g_run_by_group);
//            if (pGroup == NULL)
//            {
//                result = errno != 0 ? errno : ENOENT;
//                logError("file: "__FILE__", line: %d, " \
//                        "getgrnam fail, errno: %d, " \
//                        "error info: %s", __LINE__, \
//                        result, STRERROR(result));
//                return result;
//            }
//
//            g_run_by_gid = pGroup->gr_gid;
        }

        if (pRunByUser == null || pRunByUser.equals("")) {
            TrackerGlobal.g_run_by_user = "";
        }

        if (TrackerGlobal.g_run_by_user.equals("")) {
            TrackerGlobal.g_run_by_uid = geteuid();
        } else {//TODO
//            struct passwd *pUser;
//
//            pUser = getpwnam(g_run_by_user);
//            if (pUser == NULL)
//            {
//                result = errno != 0 ? errno : ENOENT;
//                logError("file: "__FILE__", line: %d, " \
//                        "getpwnam fail, errno: %d, " \
//                        "error info: %s", __LINE__, \
//                        result, STRERROR(result));
//                return result;
//            }
//
//            g_run_by_uid = pUser->pw_uid;
        }

        //TODO
//        if ((result=load_allow_hosts(&iniContext, \
//        &g_allow_ip_addrs, &g_allow_ip_count)) != 0)
//        {
//            return result;
//        }

        TrackerGlobal.g_sync_log_buff_interval =
                iniReader.getIntValue("sync_log_buffer_interval", CommonDefine.SYNC_LOG_BUFF_DEF_INTERVAL);
        if (TrackerGlobal.g_sync_log_buff_interval <= 0) {
            TrackerGlobal.g_sync_log_buff_interval = CommonDefine.SYNC_LOG_BUFF_DEF_INTERVAL;
        }

        TrackerGlobal.g_check_active_interval =
                iniReader.getIntValue("check_active_interval", FdfsDefine.CHECK_ACTIVE_DEF_INTERVAL);
        if (TrackerGlobal.g_check_active_interval <= 0) {
            TrackerGlobal.g_check_active_interval = FdfsDefine.CHECK_ACTIVE_DEF_INTERVAL;
        }

        String pThreadStackSize = iniReader.getStrValue("thread_stack_size");
        long thread_stack_size = 0;
        if (pThreadStackSize == null || pThreadStackSize.equals("")) {
            thread_stack_size = 64 * 1024;
        } else {
            thread_stack_size = Utils.parseBytes(pThreadStackSize, 1);
        }
        TrackerGlobal.g_thread_stack_size = (int) thread_stack_size;

        TrackerGlobal.g_storage_ip_changed_auto_adjust = iniReader.getBoolValue("storage_ip_changed_auto_adjust", true);

        TrackerGlobal.g_storage_sync_file_max_delay = iniReader.
                getIntValue("storage_sync_file_max_delay", FdfsDefine.DEFAULT_STORAGE_SYNC_FILE_MAX_DELAY);
        if (TrackerGlobal.g_storage_sync_file_max_delay <= 0) {
            TrackerGlobal.g_storage_sync_file_max_delay = FdfsDefine.DEFAULT_STORAGE_SYNC_FILE_MAX_DELAY;
        }

        TrackerGlobal.g_storage_sync_file_max_time = iniReader.getIntValue("storage_sync_file_max_time", FdfsDefine.DEFAULT_STORAGE_SYNC_FILE_MAX_TIME);
        if (TrackerGlobal.g_storage_sync_file_max_time <= 0) {
            TrackerGlobal.g_storage_sync_file_max_time = FdfsDefine.DEFAULT_STORAGE_SYNC_FILE_MAX_TIME;
        }

        TrackerGlobal.g_if_use_trunk_file = iniReader.getBoolValue("use_trunk_file", false);

        String pSlotMinSize = iniReader.getStrValue("slot_min_size");
        long slot_min_size = 0;
        if (pSlotMinSize == null) {
            slot_min_size = 256;
        } else {
            slot_min_size = Utils.parseBytes(pSlotMinSize, 1);
        }
        TrackerGlobal.g_slot_min_size = (int) slot_min_size;
        if (TrackerGlobal.g_slot_min_size <= 0) {
            log.error(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), "item \"slot_min_size\" %d is invalid, which<=0!", TrackerGlobal.g_slot_min_size);
            return -1;
        }
        if (TrackerGlobal.g_slot_min_size > 64 * 1024) {
            log.warn(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), "item \"slot_min_size\" %d is too large, change to 64KB", TrackerGlobal.g_slot_min_size);
            TrackerGlobal.g_slot_min_size = 64 * 1024;
        }

        String pTrunkFileSize = iniReader.getStrValue("trunk_file_size");
        long trunk_file_size = 0;
        if (pTrunkFileSize == null) {
            trunk_file_size = 64 * 1024 * 1024;
        } else {
            trunk_file_size = Utils.parseBytes(pTrunkFileSize, 1);
        }
        TrackerGlobal.g_trunk_file_size = (int) trunk_file_size;
        if (TrackerGlobal.g_trunk_file_size < 4 * 1024 * 1024) {
            log.warn(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), "item \"trunk_file_size\" %d is too small, change to 4MB", TrackerGlobal.g_trunk_file_size);
            TrackerGlobal.g_trunk_file_size = 4 * 1024 * 1024;
        }

        String pSlotMaxSize = iniReader.getStrValue("slot_max_size");
        long slot_max_size = 0;
        if (pSlotMaxSize == null) {
            slot_max_size = TrackerGlobal.g_trunk_file_size / 2;
        } else {
            slot_max_size = Utils.parseBytes(pSlotMaxSize, 1);
        }
        TrackerGlobal.g_slot_max_size = (int) slot_max_size;
        if (TrackerGlobal.g_slot_max_size <= TrackerGlobal.g_slot_min_size) {
            log.error(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), "item \"slot_max_size\" %d is invalid, which <= slot_min_size: %d", TrackerGlobal.g_slot_max_size, TrackerGlobal.g_slot_min_size);
            return -1;
        }
        if (TrackerGlobal.g_slot_max_size > TrackerGlobal.g_trunk_file_size / 2) {
            log.error(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), "item \"slot_max_size\" %d is too large, change to %d", TrackerGlobal.g_slot_max_size, TrackerGlobal.g_trunk_file_size / 2);
            TrackerGlobal.g_slot_max_size = TrackerGlobal.g_trunk_file_size / 2;
        }

        TrackerGlobal.g_trunk_create_file_advance = iniReader.getBoolValue("trunk_create_file_advance", false);
//        if ((result=get_time_item_from_conf(&iniContext, \
//        "trunk_create_file_time_base", \
//        &g_trunk_create_file_time_base, 2, 0)) != 0)
//        {
//            return result;
//        }

        TrackerGlobal.g_trunk_create_file_interval = iniReader.getIntValue("trunk_create_file_interval", 86400);

        String pSpaceThreshold = iniReader.getStrValue("trunk_create_file_space_threshold");
        if (pSpaceThreshold == null) {
            TrackerGlobal.g_trunk_create_file_space_threshold = 0;
        } else {
            TrackerGlobal.g_trunk_create_file_space_threshold = (int) Utils.parseBytes(pSpaceThreshold, 1);
        }
        TrackerGlobal.g_trunk_compress_binlog_min_interval = iniReader.getIntValue("trunk_compress_binlog_min_interval", 0);

        TrackerGlobal.g_trunk_init_check_occupying = iniReader.getBoolValue("trunk_init_check_occupying", false);

        TrackerGlobal.g_trunk_init_reload_from_binlog = iniReader.getBoolValue("trunk_init_reload_from_binlog", false);

//        if ((result=tracker_load_storage_id_info( \
//                filename, &iniContext)) != 0)
//        {
//            return result;
//        }

        TrackerGlobal.g_rotate_error_log = iniReader.getBoolValue("rotate_error_log", false);
//        if ((result=get_time_item_from_conf(&iniContext, \
//        "error_log_rotate_time", &g_error_log_rotate_time, \
//        0, 0)) != 0)
//        {
//            break;
//        }

        String pRotateErrorLogSize = iniReader.getStrValue("rotate_error_log_size");
        long rotate_error_log_size = 0;
        if (pRotateErrorLogSize == null || pRotateErrorLogSize.equals("")) {
            rotate_error_log_size = 0;
        } else {
            rotate_error_log_size = Utils.parseBytes(pRotateErrorLogSize, 1);
        }
        if (rotate_error_log_size > 0 && rotate_error_log_size < Contants.FDFS_ONE_MB) {
            log.warn(CommonConstant.LOG_FORMAT, "trackerLoadFromConfFile",
                    JSON.toJSON(iniReader), "item \"rotate_error_log_size\" %d is too small, change to 1 MB", rotate_error_log_size);
            rotate_error_log_size = Contants.FDFS_ONE_MB;
        }
//        fdfs_set_log_rotate_size(&g_log_context, rotate_error_log_size);

        TrackerGlobal.g_log_file_keep_days = iniReader.getIntValue("log_file_keep_days", 0);

        TrackerGlobal.g_store_slave_file_use_link = iniReader.getBoolValue("store_slave_file_use_link", false);

//        if ((result=fdfs_connection_pool_init(filename, &iniContext)) != 0)
//        {
//            break;
//        }


        String pMinBuffSize = iniReader.getStrValue("min_buff_size");
        long min_buff_size = 0;
        if (pMinBuffSize == null || pMinBuffSize.equals("")) {
            min_buff_size = TRACKER_MAX_PACKAGE_SIZE;
        } else {
            min_buff_size = Utils.parseBytes(pMinBuffSize, 1);
        }
        g_min_buff_size = (int) min_buff_size;

        String pMaxBuffSize = iniReader.getStrValue("max_buff_size");
        long max_buff_size = 0;
        if (pMaxBuffSize == null || pMaxBuffSize.equals("")) {
            max_buff_size = 16 * TRACKER_MAX_PACKAGE_SIZE;
        } else {
            max_buff_size = Utils.parseBytes(pMaxBuffSize, 1);
        }
        TrackerGlobal.g_max_buff_size = (int) max_buff_size;

        if (g_min_buff_size < TRACKER_MAX_PACKAGE_SIZE) {
            g_min_buff_size = TRACKER_MAX_PACKAGE_SIZE;
        }
        if (TrackerGlobal.g_max_buff_size < TrackerGlobal.g_min_buff_size) {
            TrackerGlobal.g_max_buff_size = TrackerGlobal.g_min_buff_size;
        }

        return 1;
    }

    private static int geteuid() {
        return 0;
    }

    private static int getegid() {
        return 0;
    }

    static int trackerLoadStoreLookup(String fileName, IniFileReader iniReader) {
        String pGroupName = "";
        ggroups.setStoreLookup((byte) iniReader.getIntValue("store_lookup", Contants.FDFS_STORE_LOOKUP_ROUND_ROBIN));
        if (ggroups.getStoreLookup() == Contants.FDFS_STORE_LOOKUP_ROUND_ROBIN) {
            ggroups.setStoreLookup((byte) 0);
            return 0;
        }

        if (ggroups.getStoreLookup() == Contants.FDFS_STORE_LOOKUP_LOAD_BALANCE) {
            ggroups.setStoreLookup((byte) 0);
            return 0;
        }

        if (ggroups.getStoreLookup() != Contants.FDFS_STORE_LOOKUP_SPEC_GROUP) {
            log.error(CommonConstant.LOG_FORMAT, "trackerLoadStoreLookup",
                    JSON.toJSON(iniReader), "conf file  \"%s\", the value of \"store_lookup\" is invalid,value=%d", fileName, ggroups.getStoreLookup());
            return -1;
        }

        pGroupName = iniReader.getStrValue("store_group");
        if (pGroupName == null || pGroupName.equals("")) {
            log.error(CommonConstant.LOG_FORMAT, "trackerLoadStoreLookup",
                    JSON.toJSON(iniReader), "conf file  \"%s\", must have item \"store_lookup\" is invalid,value=%d", fileName);
            return -1;
        }

        //TODO
//        if (fdfs_validate_group_name(g_groups.store_group) != 0) \
//        {
//            logError("file: "__FILE__", line: %d, " \
//                    "conf file \"%s\", " \
//                    "the group name \"%s\" is invalid!", \
//                    __LINE__, filename, g_groups.store_group);
//            return EINVAL;
//        }

        return 0;

    }
}
