package io.mybear.common.tracker;

import io.mybear.common.constants.config.TrackerGlobal;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static io.mybear.common.constants.CommonConstant.FDFS_STORE_SERVER_FIRST_BY_PRI;
import static io.mybear.common.constants.CommonConstant.FDFS_STORE_SERVER_ROUND_ROBIN;

public class FdfsGroupInfo {
    public static final int TRACKER_MEM_ALLOC_ONCE = 2;
    public long total_mb;  //total disk storage in MB
    public long free_mb;  //free disk storage in MB
    public long trunk_free_mb;  //trunk free disk storage in MB
    public int alloc_size;  //alloc storage count
    public int count;    //total server count
    public int active_count; //active server count
    public int storage_port;  //storage server port
    public int storage_http_port; //storage http server port
    public int current_trunk_file_id;  //current trunk file id report by storage
    public List<FdfsStorageDetail> all_servers;   //all storage servers
    public List<FdfsStorageDetail> sorted_servers;  //storages order by ip addr
    public List<FdfsStorageDetail> active_servers;  //storages order by ip addr
    public FdfsStorageDetail pStoreServer;  //for upload priority mode
    public FdfsStorageDetail pTrunkServer;  //point to the trunk server
    public String last_trunk_server_id;
    int current_read_server;   //current read storage server index
    int current_write_server;  //current write storage server index
    int store_path_count;  //store base path count of each storage server
    /* subdir_count * subdir_count directories will be auto created
       under each store_path (disk) of the storage servers
    */
    int subdir_count_per_path;
    int[][] last_sync_timestamps;//row for src storage, col for dest storage
    int chg_count;   //current group changed count
    int trunk_chg_count;   //trunk server changed count

    //#ifdef WITH_HTTPD
//    FDFSStorageDetail **http_servers;  //storages order by ip addr
//    int http_server_count; //http server count
//    int current_http_server; //current http server index
//#endif
    long last_source_update;  //last source update timestamp
    long last_sync_update;    //last synced update timestamp
    private String groupName;

    static int tracker_malloc_storage_path_mbs(FdfsStorageDetail pStorage, final int store_path_count) {
        if (store_path_count <= 0) {
            return 0;
        }
        pStorage.path_total_mbs = new long[store_path_count];
        pStorage.path_free_mbs = new long[store_path_count];
        return 0;
    }

    /**
     * tracker_mem_cmp_by_group_name
     * 写完了
     *
     * @return
     */
    public static int cmpByGroupName(FdfsGroupInfo p1, FdfsGroupInfo p2) {
        return p1.groupName.compareTo(p2.groupName);
    }

    /**
     * tracker_mem_find_store_server
     */
    public void findStoreServer() {
        if (this.active_count == 0) {
            this.pStoreServer = null;
            return;
        }
        if (TrackerGlobal.ggroups.getStoreServer() == FDFS_STORE_SERVER_FIRST_BY_PRI) {
            this.pStoreServer = Collections.min(this.active_servers, Comparator.comparingInt(i -> i.upload_priority));
        } else {
            this.pStoreServer = this.active_servers.get(0);
        }
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * tracker_get_group_success_upload_count
     * 写完了
     *
     * @return
     */
    public long getGroupSuccessUploadCount() {
        int count = 0;
        for (FdfsStorageDetail it : all_servers) {
            count += it.stat.success_upload_count.sum();
        }
        return count;
    }

    /**
     * tracker_get_group_file_count
     * 写完了
     *
     * @return
     */
    public int getGroupFileCount() {
        int count = 0;
        for (FdfsStorageDetail it : all_servers) {
            FdfsStorageStat stat = it.state;
            count += stat.getSuccessUploadCount() - stat.getSuccessDeleteCount();
        }
        return count;
    }

    /**
     * tracker_get_group_sync_src_server
     * 写完了
     */
    public FdfsStorageDetail getGroupSyncSrcServer(String groupName) {
        for (FdfsStorageDetail it : this.active_servers) {
            if (it.id.equals(groupName)) {
                return it;
            }
        }
        return null;
    }

    /**
     * 写完了
     * tracker_get_writable_storage
     *
     * @return
     */
    public FdfsStorageDetail getWritableStorage() {
        int write_server_index;
        if (TrackerGlobal.ggroups.getStoreServer() == FDFS_STORE_SERVER_ROUND_ROBIN) {
            write_server_index = this.current_write_server++;
            if (this.current_write_server >= this.active_count) {
                this.current_write_server = 0;
            }
            if (write_server_index >= this.active_count) {
                write_server_index = 0;
            }
            return this.active_servers.get(write_server_index);
        } else //use the first server
        {
            return this.pStoreServer;
        }
    }

    /**
     * tracker_mem_get_active_storage_by_id
     * 写完了
     *
     * @return
     */
    public FdfsStorageDetail getActiveStorageById(String id) {
        if (id == null) {
            return null;
        }
        if ("".equals(id)) return null;
        FdfsStorageDetail target = new FdfsStorageDetail();
        int index = Collections.binarySearch(this.active_servers, target);
        if (index == -1) return null;
        return this.active_servers.get(index);
    }

    /**
     * tracker_mem_get_active_storage_by_ip
     *
     * @param ip_addr
     * @return
     */
    public FdfsStorageDetail getActiveStorageByIp(String ip_addr) {
//        FDFSStorageIdInfo *pStorageId;
//
//        if (!g_use_storage_id)
//        {
//            return getActiveStorageById( ip_addr);
//        }
//
//        pStorageId = fdfs_get_storage_id_by_ip(pGroup->group_name, ip_addr);
//        if (pStorageId == NULL)
//        {
//            return NULL;
//        }
//        return tracker_mem_get_active_storage_by_id(pGroup, pStorageId->id);
        return null;
    }


}
