package io.mybear.storage.trunkMgr;


import io.mybear.common.tracker.FDFSStorageReservedSpace;
import io.mybear.common.trunk.FdfsTrunkFileInfo;
import io.mybear.common.trunk.FdfsTrunkFullInfo;
import io.mybear.common.trunk.FdfsTrunkPathInfo;
import org.omg.CORBA.Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static io.mybear.common.constants.CommonConstant.FDFS_DEF_STORAGE_RESERVED_MB;
import static io.mybear.common.constants.CommonConstant.FDFS_STORE_PATH_ROUND_ROBIN;
import static io.mybear.common.constants.ErrorNo.EEXIST;
import static io.mybear.common.trunk.TrunkShared.FDFS_TRUNK_STATUS_FREE;
import static io.mybear.common.trunk.TrunkShared.FDFS_TRUNK_STATUS_HOLD;

/**
 * Created by jamie on 2017/6/21.
 * Trunk原型
 */
public class TrunkMem {
    public final static String STORAGE_TRUNK_DATA_FILENAME = "storage_trunk.dat";
    public final static int STORAGE_TRUNK_INIT_FLAG_NONE = 0;
    public final static int STORAGE_TRUNK_INIT_FLAG_DESTROYING = 1;
    public final static int STORAGE_TRUNK_INIT_FLAG_DONE = 2;
    static final NavigableMap<Integer, List<FdfsTrunkFullInfo>> TREE_INFO_BY_SIZES = new TreeMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(TrunkMem.class);
    public static int g_slot_min_size;
    public static int g_trunk_file_size;
    public static int g_slot_max_size;
    public static int g_store_path_mode = FDFS_STORE_PATH_ROUND_ROBIN;
    public static FDFSStorageReservedSpace g_storage_reserved_space = new FDFSStorageReservedSpace();
    public static int g_avg_storage_reserved_mb = FDFS_DEF_STORAGE_RESERVED_MB;
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


    public static FdfsTrunkFullInfo allocSpace(int size) {
        FdfsTrunkFullInfo node = null;
        List<FdfsTrunkFullInfo> queue = null;
        Map.Entry<Integer, List<FdfsTrunkFullInfo>> listEntry = TREE_INFO_BY_SIZES.floorEntry(size);
        if (listEntry != null) {
            queue = listEntry.getValue();
            Iterator<FdfsTrunkFullInfo> iterator = queue.iterator();
            while (iterator.hasNext()) {
                FdfsTrunkFullInfo it = iterator.next();
                if (it.getStatus() == FDFS_TRUNK_STATUS_FREE) {
                    iterator.remove();
                    if (queue.size() == 0) {
                        TREE_INFO_BY_SIZES.remove(listEntry.getKey());//如果这个队列空了,就删了吧
                    }
                    node = it;//找到合适的了
                    break;
                }
            }
        }
        if (node == null) {
            node = createNewTrunk();
        }
        //split
        FdfsTrunkFullInfo result = node.split(size);
        FdfsTrunkFullInfo old = node;
        insertTreeInfoBySizes(result);
        insertTreeInfoBySizes(old);
        node.setStatus(FDFS_TRUNK_STATUS_HOLD);
        return node;
    }

    private static void insertTreeInfoBySizes(FdfsTrunkFullInfo trunkFullInfo) {
        Integer size = trunkFullInfo.getFile().getSize();
        List<FdfsTrunkFullInfo> que = TREE_INFO_BY_SIZES.get(size);
        if (que != null) {
            que.add(trunkFullInfo);
        } else {
            que = new LinkedList<>();
            que.add(trunkFullInfo);
        }
        TREE_INFO_BY_SIZES.put(size, que);
        FreeBlockCheacker.freeBlockInsert(trunkFullInfo);
    }

    private static FdfsTrunkFullInfo createNewTrunk() {
        //新建文件
        FdfsTrunkFullInfo trunkFullInfo = new FdfsTrunkFullInfo();
        FdfsTrunkPathInfo path = new FdfsTrunkPathInfo();
        FdfsTrunkFileInfo file = new FdfsTrunkFileInfo();
        trunkFullInfo.setPath(path);
        trunkFullInfo.setFile(file);
        trunkFullInfo.setStatus(FDFS_TRUNK_STATUS_FREE);
        trunkFullInfo.getFile().setSize(64 * 1024);
        trunkFullInfo.getFile().setOffset(0);
        //文件名
        trunkFullInfo.getPath().setStorePathIndex(0);
        trunkFullInfo.getPath().setSubPathHigh(0);
        trunkFullInfo.getPath().setSubPathLow(0);
        return trunkFullInfo;
    }

    public static int allocConfirm(FdfsTrunkFullInfo pTrunkInfo, int status) {
        List<FdfsTrunkFullInfo> queue;
        if (status == 0) {
            queue = TREE_INFO_BY_SIZES.get(pTrunkInfo.getFile().getSize());
            queue.remove(pTrunkInfo);
            FreeBlockCheacker.freeBlockDelete(pTrunkInfo);
            return 0;
        } else if (status == EEXIST) {
            queue = TREE_INFO_BY_SIZES.get(pTrunkInfo.getFile().getSize());
            queue.remove(pTrunkInfo);
            FreeBlockCheacker.freeBlockDelete(pTrunkInfo);
            return 0;
        }
        pTrunkInfo.setStatus(FDFS_TRUNK_STATUS_FREE);
        return 0;
    }

    public static int freeSpace(FdfsTrunkFullInfo trunkInfo, boolean bWriteBinLog) {
        trunkInfo.setStatus(FDFS_TRUNK_STATUS_FREE);
        insertTreeInfoBySizes(trunkInfo);
        FreeBlockCheacker.freeBlockInsert(trunkInfo);
        return 0;
    }

    public static void main(String[] args) {
        FdfsTrunkFullInfo trunkFullInfo = allocSpace(64);
        allocConfirm(trunkFullInfo, 0);
        freeSpace(trunkFullInfo, true);
        System.out.println(TREE_INFO_BY_SIZES);
        System.out.println(FreeBlockCheacker.TREE_INFO_BY_ID);

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
