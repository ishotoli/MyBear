package io.mybear.storage.trunkMgr;

import io.mybear.common.FdfsDefine;
import io.mybear.common.FdfsTrunkFullInfo;
import io.mybear.tracker.types.FDFSStorageReservedSpace;
import org.omg.CORBA.Object;

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
    public static int[] g_trunk_server = {-1, 0};

    public static int storageTrunkInit() {
        return 0;
    }

    public static int storage_trunk_destroy_ex(boolean bNeedSleep) {
        return 0;
    }


    public static int storageTrunkDestroy() {
        return storage_trunk_destroy_ex(false);
    }


    public static int trunkAllocSpace(int size, FdfsTrunkFullInfo pResult) {
        return 0;
    }

    public static int trunkAllocConfirm(FdfsTrunkFullInfo pTrunkInfo, int status) {
        return 0;
    }

    public static int trunkFreeSpace(FdfsTrunkFullInfo TrunkInfo, boolean bWriteBinLog) {
        return 0;
    }

    public static boolean trunkCheckSize(long file_size) {
        return true;
    }


    public static int trunkInitFile(String filename) {


        return trunkInitFileEx(filename, g_trunk_file_size);

    }

    public static int trunkCheckAndInitFile(String filename) {

        return trunkCheckAndInitFileEx(filename, g_trunk_file_size);

    }

    public static int trunkInitFileEx(String filename, final long file_size) {
        return 0;
    }

    public static int trunkCheckAndInitFileEx(String filename, long file_size) {
        return 0;
    }

    public static int trunkFileDelete(String trunk_filename, FdfsTrunkFullInfo pTrunkInfo) {
        return 0;
    }

    public static int trunkCreateTrunkFileAdvance(Object args) {
        return 0;
    }

    public static int storageDeleteTrunkDataFile() {
        return 0;
    }

    public static String storageTrunkGetDataFilename(String full_filename) {
        return "";
    }



}
