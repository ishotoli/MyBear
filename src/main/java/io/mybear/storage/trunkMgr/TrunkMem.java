package io.mybear.storage.trunkMgr;

import io.mybear.common.FdfsDefine;
import io.mybear.tracker.types.FDFSStorageReservedSpace;

import static io.mybear.tracker.TrackerTypes.FDFS_STORE_PATH_ROUND_ROBIN;

/**
 * Created by jamie on 2017/6/21.
 */
public class TrunkMem {

    public final static String STORAGE_TRUNK_DATA_FILENAME = "storage_trunk.dat";

    public final static int STORAGE_TRUNK_INIT_FLAG_NONE = 0;
    public final static int STORAGE_TRUNK_INIT_FLAG_DESTROYING = 1;
    public final static int STORAGE_TRUNK_INIT_FLAG_DONE = 2;

    public static int g_slot_min_size;
    public static int g_trunk_file_size;
    public static int g_slot_max_size;
    public static int g_store_path_mode = FDFS_STORE_PATH_ROUND_ROBIN;
    public static FDFSStorageReservedSpace g_storage_reserved_space = new FDFSStorageReservedSpace();
    public static int g_avg_storage_reserved_mb = FdfsDefine.FDFS_DEF_STORAGE_RESERVED_MB;
    public static int g_store_path_index = 0;
    public static int g_current_trunk_file_id = 0;
    public static long[] g_trunk_create_file_time_base = {0, 0};
    public static int g_trunk_create_file_interval = 86400;
    public static int g_trunk_compress_binlog_min_interval = 0;
    public static boolean g_if_use_trunk_file = false;
    public static boolean g_if_trunker_self = false;
    public static boolean g_trunk_create_file_advance = false;
    public static boolean g_trunk_init_check_occupying = false;
    public static boolean g_trunk_init_reload_from_binlog = false;
    public static byte trunk_init_flag = STORAGE_TRUNK_INIT_FLAG_NONE;
    public static long g_trunk_total_free_space = 0;
    public static long g_trunk_create_file_space_threshold = 0;
    public static long g_trunk_last_compress_time = 0;
    int[] g_trunk_server = {-1, 0};
}
