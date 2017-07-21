package io.mybear.storage;

import com.alibaba.fastjson.JSON;
import io.mybear.common.FdfsStorePathInfo;
import io.mybear.common.IniFileReader;
import io.mybear.common.constants.CommonConstant;
import io.mybear.storage.trunkMgr.TrunkMem;
import io.mybear.storage.trunkMgr.TrunkShared;
import io.mybear.tracker.SharedFunc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.mybear.common.FdfsGlobal.*;
import static io.mybear.storage.FdfsStoraged.g_current_time;
import static io.mybear.storage.StorageDiskRecovery.storage_disk_recovery_start;
import static io.mybear.storage.StorageGlobal.g_last_http_port;
import static io.mybear.storage.StorageGlobal.g_last_server_port;
import static io.mybear.storage.StorageGlobal.g_last_storage_ip;
import static io.mybear.storage.StorageGlobal.g_storage_join_time;
import static io.mybear.storage.StorageGlobal.*;
import static io.mybear.storage.StorageGlobal.g_sync_old_done;
import static io.mybear.storage.StorageGlobal.g_sync_src_id;
import static io.mybear.storage.StorageGlobal.g_sync_until_timestamp;
import static io.mybear.tracker.SharedFunc.isDir;
import static io.mybear.tracker.TrackerTypes.FDFS_STORAGE_DATA_DIR_FORMAT;

/**
 * Created by jamie on 2017/6/21.
 */
public class StorageFunc {

    public static final Logger log = LoggerFactory.getLogger(StorageFunc.class);

    public static final String DATA_DIR_INITED_FILENAME = ".data_init_flag";
    public static final String STORAGE_STAT_FILENAME = "storage_stat.dat";

    public static final String INIT_ITEM_STORAGE_JOIN_TIME = "storage_join_time";
    public static final String INIT_ITEM_SYNC_OLD_DONE = "sync_old_done";
    public static final String INIT_ITEM_SYNC_SRC_SERVER = "sync_src_server";
    public static final String INIT_ITEM_SYNC_UNTIL_TIMESTAMP = "sync_until_timestamp";
    public static final String INIT_ITEM_LAST_IP_ADDRESS = "last_ip_addr";
    public static final String INIT_ITEM_LAST_SERVER_PORT = "last_server_port";
    public static final String INIT_ITEM_LAST_HTTP_PORT = "last_http_port";
    public static final String INIT_ITEM_CURRENT_TRUNK_FILE_ID = "current_trunk_file_id";
    public static final String INIT_ITEM_TRUNK_LAST_COMPRESS_TIME = "trunk_last_compress_time";

    public static final String STAT_ITEM_TOTAL_UPLOAD = "total_upload_count";
    public static final String STAT_ITEM_SUCCESS_UPLOAD = "success_upload_count";
    public static final String STAT_ITEM_TOTAL_APPEND = "total_append_count";
    public static final String STAT_ITEM_SUCCESS_APPEND = "success_append_count";
    public static final String STAT_ITEM_TOTAL_MODIFY = "total_modify_count";
    public static final String STAT_ITEM_SUCCESS_MODIFY = "success_modify_count";
    public static final String STAT_ITEM_TOTAL_TRUNCATE = "total_truncate_count";
    public static final String STAT_ITEM_SUCCESS_TRUNCATE = "success_truncate_count";
    public static final String STAT_ITEM_TOTAL_DOWNLOAD = "total_download_count";
    public static final String STAT_ITEM_SUCCESS_DOWNLOAD = "success_download_count";
    public static final String STAT_ITEM_LAST_SOURCE_UPD = "last_source_update";
    public static final String STAT_ITEM_LAST_SYNC_UPD = "last_sync_update";
    public static final String STAT_ITEM_TOTAL_SET_META = "total_set_meta_count";
    public static final String STAT_ITEM_SUCCESS_SET_META = "success_set_meta_count";
    public static final String STAT_ITEM_TOTAL_DELETE = "total_delete_count";
    public static final String STAT_ITEM_SUCCESS_DELETE = "success_delete_count";
    public static final String STAT_ITEM_TOTAL_GET_META = "total_get_meta_count";
    public static final String STAT_ITEM_SUCCESS_GET_META = "success_get_meta_count";
    public static final String STAT_ITEM_TOTAL_CREATE_LINK = "total_create_link_count";
    public static final String STAT_ITEM_SUCCESS_CREATE_LINK = "success_create_link_count";
    public static final String STAT_ITEM_TOTAL_DELETE_LINK = "total_delete_link_count";
    public static final String STAT_ITEM_SUCCESS_DELETE_LINK = "success_delete_link_count";
    public static final String STAT_ITEM_TOTAL_UPLOAD_BYTES = "total_upload_bytes";
    public static final String STAT_ITEM_SUCCESS_UPLOAD_BYTES = "success_upload_bytes";
    public static final String STAT_ITEM_TOTAL_APPEND_BYTES = "total_append_bytes";
    public static final String STAT_ITEM_SUCCESS_APPEND_BYTES = "success_append_bytes";
    public static final String STAT_ITEM_TOTAL_MODIFY_BYTES = "total_modify_bytes";
    public static final String STAT_ITEM_SUCCESS_MODIFY_BYTES = "success_modify_bytes";
    public static final String STAT_ITEM_TOTAL_DOWNLOAD_BYTES = "total_download_bytes";
    public static final String STAT_ITEM_SUCCESS_DOWNLOAD_BYTES = "success_download_bytes";
    public static final String STAT_ITEM_TOTAL_SYNC_IN_BYTES = "total_sync_in_bytes";
    public static final String STAT_ITEM_SUCCESS_SYNC_IN_BYTES = "success_sync_in_bytes";
    public static final String STAT_ITEM_TOTAL_SYNC_OUT_BYTES = "total_sync_out_bytes";
    public static final String STAT_ITEM_SUCCESS_SYNC_OUT_BYTES = "success_sync_out_bytes";
    public static final String STAT_ITEM_TOTAL_FILE_OPEN_COUNT = "total_file_open_count";
    public static final String STAT_ITEM_SUCCESS_FILE_OPEN_COUNT = "success_file_open_count";
    public static final String STAT_ITEM_TOTAL_FILE_READ_COUNT = "total_file_read_count";
    public static final String STAT_ITEM_SUCCESS_FILE_READ_COUNT = "success_file_read_count";
    public static final String STAT_ITEM_TOTAL_FILE_WRITE_COUNT = "total_file_write_count";
    public static final String STAT_ITEM_SUCCESS_FILE_WRITE_COUNT = "success_file_write_count";
    public static final String STAT_ITEM_DIST_PATH_INDEX_HIGH = "dist_path_index_high";
    public static final String STAT_ITEM_DIST_PATH_INDEX_LOW = "dist_path_index_low";
    public static final String STAT_ITEM_DIST_WRITE_FILE_COUNT = "dist_write_file_count";

    public static int storage_stat_fd = -1;

    public static int storageLoadPaths(IniFileReader pItemContext) {
        int result;
        result = storageLoadPathsFromConfFile(pItemContext);
        if (result != 0) {
            return result;
        }
        StorageGlobal.g_path_space_list = new FdfsStorePathInfo[TrunkShared.fdfsStorePaths.getCount()];
        if (StorageGlobal.g_path_space_list.length == 0) {
            log.error(CommonConstant.LOG_FORMAT, "storageLoadPaths",
                    JSON.toJSON(pItemContext) + " count " + TrunkShared.fdfsStorePaths.getCount(), "count值为0");
            return -1;
        }
        return 0;
    }

    public static int storageLoadPathsFromConfFile(IniFileReader pItemContext) {
        char[] pPath = pItemContext.getStrValue("base_path") == null ? null : pItemContext.getStrValue("base_path")
                .toCharArray();
        if (pPath == null) {
            log.error(CommonConstant.LOG_FORMAT, "storageLoadPaths",
                    JSON.toJSON(pItemContext), "base_path值为null");
            return -1;
        }
        pPath = SharedFunc.chopPath(pPath);
        g_fdfs_base_path = new String(pPath);
        if (!SharedFunc.fileExists(g_fdfs_base_path)) {
            log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFile",
                    JSON.toJSON(pItemContext), g_fdfs_base_path + "路径不存在");
            return -1;
        }
        if (!isDir(g_fdfs_base_path)) {
            log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFile",
                    JSON.toJSON(pItemContext), g_fdfs_base_path + " is not a directory!");
            return -1;
        }
        return storageLoadPathsFromConfFileEx(pItemContext, null, true);
    }

    public static int storageLoadPathsFromConfFileEx(IniFileReader pItemContext,
                                                     final char[] szSectionName, final boolean bUseBasePath) {
        String item_name = null;
        String[] store_paths;
        String pPath;
        TrunkShared.fdfsStorePaths.setCount(pItemContext.getIntValue("store_path_count", 1));
        if (TrunkShared.fdfsStorePaths.getCount() <= 0) {
            log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFileEx",
                    JSON.toJSON(pItemContext),
                    String.format("store_path_count: %d is invalid", TrunkShared.fdfsStorePaths.getCount()));
            return -1;
        }
        store_paths = new String[TrunkShared.fdfsStorePaths.getCount()];
        //取store_path0的路径
        pPath = pItemContext.getStrValue("store_path0");
        if (pPath == null || "".equals(pPath.trim())) {
            if (!bUseBasePath) {
                log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFileEx",
                        JSON.toJSON(pItemContext),
                        "conf file must have item store_path0");
                return -1;
            }
            pPath = g_fdfs_base_path;
        }
        if (pPath == null || "".equals(pPath.trim())) {
            log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFileEx",
                    JSON.toJSON(pItemContext),
                    "conf file must have item pPath");
            return -1;
        }
        store_paths[0] = pPath;
        int err_no = 0;
        for (int i = 1; i < TrunkShared.fdfsStorePaths.getCount(); i++) {
            item_name = String.format("store_path%d", i);
            pPath = pItemContext.getStrValue(item_name);
            if (pPath == null || "".equals(pPath.trim())) {
                log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFileEx",
                        JSON.toJSON(pItemContext),
                        String.format("conf file must have item %s", item_name));
                err_no = -1;
                break;
            }
            pPath = new String(SharedFunc.chopPath(pPath.toCharArray()));
            if (!SharedFunc.fileExists(pPath)) {
                log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFileEx",
                        JSON.toJSON(pItemContext),
                        String.format("can't be accessed %s", pPath));
                err_no = -1;
                break;
            }
            if (!isDir(pPath)) {
                log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFileEx",
                        JSON.toJSON(pItemContext),
                        String.format("conf file must have item %s", pPath));
                err_no = -1;
                break;
            }
            store_paths[i] = pPath;
        }
        if (err_no == 0) {
            for (int i = 0; i < TrunkShared.fdfsStorePaths.getCount(); i++) {
                TrunkShared.fdfsStorePaths.getPaths()[i] = store_paths[i];
            }
            return 0;
        }
        return err_no;
    }

    public static boolean storageMakeDataDirs(String pBasePath) throws IOException {
        Path data_path;//256
        Path dir_name;//9
        Path sub_name;//9
        String min_sub_path;//16
        String max_sub_path;//16
        data_path = Paths.get(pBasePath, "data");
        if (!Files.exists(data_path)) {
            try {
                Files.createDirectory(data_path);
            } catch (IOException e) {
                e.printStackTrace();
                log.error(String.format("mkdir \"%s\" fail, error info: %s", data_path, e.getLocalizedMessage()));
                throw e;
            }
        }
        min_sub_path = String.format(FDFS_STORAGE_DATA_DIR_FORMAT + "/" + FDFS_STORAGE_DATA_DIR_FORMAT, 0, 0);
        max_sub_path = String.format(FDFS_STORAGE_DATA_DIR_FORMAT + "/" + FDFS_STORAGE_DATA_DIR_FORMAT, g_subdir_count_per_path - 1, g_subdir_count_per_path - 1);
        if (Files.exists(data_path.resolve(min_sub_path)) && Files.exists(data_path.resolve(max_sub_path))) {
            return true;
        }
        int i, k;
        log.info(String.format("data path: %s, mkdir sub dir...\n", data_path.toString()));
        for (i = 0; i < g_subdir_count_per_path; i++) {
            dir_name = data_path.resolve(String.format(FDFS_STORAGE_DATA_DIR_FORMAT, i));
            log.info(String.format("mkdir data path: %s ...\n", dir_name.toString()));
            try {
                if (!(Files.exists(dir_name) && Files.isDirectory(data_path))) {
                    Files.createDirectory(dir_name);
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.error(String.format("mkdir \"%s/%s\" fail ", data_path.toString(), dir_name.toString()));
                throw e;
            }
            for (k = 0; k < g_subdir_count_per_path; k++) {
                sub_name = dir_name.resolve(String.format(FDFS_STORAGE_DATA_DIR_FORMAT, k));
                try {
                    if (!(Files.exists(sub_name) && Files.isDirectory(sub_name))) {
                        Files.createDirectory(sub_name);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error(String.format("mkdir \"%s/%s\" fail ", data_path.toString(), dir_name.toString()));
                    throw e;
                }
            }
        }
        log.info(String.format("data path: %s, mkdir sub dir done.\n", data_path.toString()));
        return true;
    }

    public static boolean storageCheckAndMakeDataDirs() {
        int result;
        int i;
        Path data_path;
        Path full_filename;
        data_path = Paths.get(g_fdfs_base_path, "data");
        full_filename = data_path.resolve(DATA_DIR_INITED_FILENAME);
        IniFileReader iniContext;
        String pValue;
        if (Files.exists(full_filename)) {
            try {
                iniContext = new IniFileReader(full_filename.toString());
            } catch (IOException e) {
                e.printStackTrace();
                log.error(String.format("load from file \"%s/%s\" fail", data_path, full_filename));
                return false;
            }

            pValue = iniContext.getStrValue(INIT_ITEM_STORAGE_JOIN_TIME);
            if (pValue == null) {
                log.error(String.format("in file \"%s/%s\", item \"%s\" not exists", data_path, full_filename, INIT_ITEM_STORAGE_JOIN_TIME));
                return false;
            }
            StorageGlobal.g_storage_join_time = Integer.parseInt(pValue);

            pValue = iniContext.getStrValue(INIT_ITEM_SYNC_OLD_DONE);
            if (pValue == null) {
                log.error(String.format("in file \"%s/%s\", item \"%s\" not exists", data_path, full_filename, INIT_ITEM_SYNC_OLD_DONE));
                return false;
            }
            g_sync_old_done = Boolean.parseBoolean(pValue);

            pValue = iniContext.getStrValue(INIT_ITEM_SYNC_SRC_SERVER);
            if (pValue == null) {
                log.error(String.format("in file \"%s/%s\", item \"%s\" not exists", data_path, full_filename, INIT_ITEM_SYNC_SRC_SERVER));
                return false;
            }
            g_sync_src_id = pValue;
            g_sync_until_timestamp = iniContext.getIntValue(INIT_ITEM_SYNC_UNTIL_TIMESTAMP, 0);
            pValue = iniContext.getStrValue(INIT_ITEM_LAST_IP_ADDRESS);
            if (pValue != null) {
                g_last_storage_ip = pValue;
            }

            pValue = iniContext.getStrValue(INIT_ITEM_LAST_SERVER_PORT);
            if (pValue != null) {
                g_last_server_port = Integer.parseInt(pValue);
            }

            pValue = iniContext.getStrValue(INIT_ITEM_LAST_HTTP_PORT);
            if (pValue != null) {
                g_last_http_port = Integer.valueOf(pValue);
            }

            TrunkMem.g_current_trunk_file_id = iniContext.getIntValue(INIT_ITEM_CURRENT_TRUNK_FILE_ID, 0);
            TrunkMem.g_trunk_last_compress_time = iniContext.getIntValue(INIT_ITEM_TRUNK_LAST_COMPRESS_TIME, 0);


            if (g_last_server_port == 0 || g_last_http_port == 0) {
                if (g_last_server_port == 0) {
                    g_last_server_port = g_server_port;
                }

                if (g_last_http_port == 0) {
                    g_last_http_port = g_http_port;
                }

                if ((result = storageWriteToSyncIniFile()) != 0) {
                    return false;
                }
            }

		/*
        logInfo("g_sync_old_done = %d, "
			"g_sync_src_id = %s, "
			"g_sync_until_timestamp = %d, "
			"g_last_storage_ip = %s, "
			"g_last_server_port = %d, "
			"g_last_http_port = %d, "
			"g_current_trunk_file_id = %d, "
			"g_trunk_last_compress_time = %d",
			g_sync_old_done, g_sync_src_id, g_sync_until_timestamp,
			g_last_storage_ip, g_last_server_port, g_last_http_port,
			g_current_trunk_file_id, (int)g_trunk_last_compress_time
			);
		*/
        } else {
            if (!Files.exists(data_path)) {
                try {
                    Files.createDirectory(data_path);
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("mkdir \"%s\" fail, error info: %s", data_path, e.getLocalizedMessage());
                    return false;
                }
                // STORAGE_CHOWN(data_path, geteuid(), getegid())
            }
            g_last_server_port = g_server_port;
            g_last_http_port = g_http_port;
            g_storage_join_time = g_current_time;
            if ((result = storageWriteToSyncIniFile()) != 0) {
                return false;
            }
        }

        for (i = 0; i < TrunkShared.getFdfsStorePaths().getCount(); i++) {
            try {
                if (!storageMakeDataDirs(TrunkShared.getFdfsStorePaths().getPaths()[i])) {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            if (g_sync_old_done)  //repair damaged disk
            {
                if ((result = storage_disk_recovery_start(i)) != 0) {
                    return false;
                }
            }

            result = StorageDiskRecovery.storageDiskRecoveryRestore(TrunkShared.getFdfsStorePaths().getPaths()[i]);
            if (result == -1) //need to re-fetch binlog
            {
                if ((result = storage_disk_recovery_start(i)) != 0) {
                    return false;
                }

                result = StorageDiskRecovery.storageDiskRecoveryRestore(TrunkShared.getFdfsStorePaths().getPaths()[i]);
            }

            if (result != 0) {
                return false;
            }
        }

        return true;
    }

    static int storageWriteToSyncIniFile() {
        return 0;
    }

    public static void main(String[] args) throws IOException {
        g_subdir_count_per_path = 16;
        storageMakeDataDirs("D:\\fastdfs\\");
    }
}
