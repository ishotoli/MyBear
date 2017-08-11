package io.mybear.common.tracker;

import java.util.List;

public class FdfsGroupInfo implements Comparable<FdfsGroupInfo> {
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
    public List<FdfsStorageDetail> pStoreServer;  //for upload priority mode
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

    //   public static int tracker_mem_realloc_store_servers(FdfsGroupInfo pGroup, int inc_count, boolean bNeedSleep) throws Exception{
//        int result;
//        FdfsStorageDetail[] old_servers;
//        FdfsStorageDetail[] old_sorted_servers;
//        FdfsStorageDetail[] old_active_servers;
//        int[][] old_last_sync_timestamps;
//        List<FdfsStorageDetail> new_servers;
//        FdfsStorageDetail[] new_sorted_servers;
//        FdfsStorageDetail[] new_active_servers;
//        FdfsStorageDetail[] ppServer;
//        FdfsStorageDetail[] ppServerEnd;
//
//        int[][] new_last_sync_timestamps;
//        int old_size;
//        int new_size;
//        int err_no;
//        int i;
//
//        new_size = pGroup.alloc_size + inc_count + TRACKER_MEM_ALLOC_ONCE;
//        new_servers = new ArrayList<>(new_size);
//        for (int j = 0; j < new_size; j++) {
//            new_servers.add(j, new FdfsStorageDetail());
//        }
//
//
//        memcpy(new_servers, pGroup -> all_servers, \
//                sizeof(FDFSStorageDetail *) * pGroup -> count);
//
//        new_sorted_servers = (FDFSStorageDetail * *) \
//        malloc(sizeof(FDFSStorageDetail *) * new_size);
//
//        new_active_servers = (FDFSStorageDetail * *) \
//        malloc(sizeof(FDFSStorageDetail *) * new_size);
//
//
//        memset(new_sorted_servers, 0, sizeof(FDFSStorageDetail *) * new_size);
//        memset(new_active_servers, 0, sizeof(FDFSStorageDetail *) * new_size);
//        if (pGroup -> store_path_count > 0) {
//            for (i = pGroup -> count; i < new_size; i++) {
//                result = tracker_malloc_storage_path_mbs( * (new_servers + i), \
//                pGroup -> store_path_count);
//            }
//        }
//
//        memcpy(new_sorted_servers, pGroup -> sorted_servers, \
//                sizeof(FDFSStorageDetail *) * pGroup -> count);
//
//        memcpy(new_active_servers, pGroup -> active_servers, \
//                sizeof(FDFSStorageDetail *) * pGroup -> count);
//
//        new_last_sync_timestamps = tracker_malloc_last_sync_timestamps( \
//                new_size, & err_no);
//        if (new_last_sync_timestamps == NULL) {
//            free(new_servers);
//            free(new_sorted_servers);
//            free(new_active_servers);
//
//            return err_no;
//        }
//        for (i = 0; i < pGroup -> alloc_size; i++) {
//            memcpy(new_last_sync_timestamps[i],  \
//                    pGroup -> last_sync_timestamps[i], \
//            (int) sizeof(int) *pGroup -> alloc_size);
//        }
//
//        old_size = pGroup -> alloc_size;
//        old_servers = pGroup -> all_servers;
//        old_sorted_servers = pGroup -> sorted_servers;
//        old_active_servers = pGroup -> active_servers;
//        old_last_sync_timestamps = pGroup -> last_sync_timestamps;
//
//        pGroup.alloc_size = new_size;
//        pGroup.all_servers = new_servers;
//        pGroup.sorted_servers = new_sorted_servers;
//        pGroup.active_servers = new_active_servers;
//        pGroup.last_sync_timestamps = new_last_sync_timestamps;
//
//        tracker_mem_find_store_server(pGroup);
//        if (g_if_leader_self && g_if_use_trunk_file) {
//            tracker_mem_find_trunk_server(pGroup, true);
//        }
//
//
//        if (bNeedSleep) {
//            sleep(1);
//        }
//        return 0;
    //  }
    static int tracker_malloc_storage_path_mbs(FdfsStorageDetail pStorage, final int store_path_count) {
        if (store_path_count <= 0) {
            return 0;
        }
        pStorage.path_total_mbs = new long[store_path_count];
        pStorage.path_free_mbs = new long[store_path_count];
        return 0;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public int compareTo(FdfsGroupInfo o) {
        return this.groupName.compareTo(o.groupName);
    }

    /**
     * tracker_get_group_file_count
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
     */
    public FdfsStorageDetail getGroupSyncSrcServer(FdfsGroupInfo pDestServer) {
        for (FdfsStorageDetail it : this.active_servers) {
            if (it.id.equals(pDestServer.getGroupName())) {
                return it;
            }
        }
        return null;
    }
}
