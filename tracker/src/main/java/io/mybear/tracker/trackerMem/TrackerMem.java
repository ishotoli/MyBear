package io.mybear.tracker.trackerMem;

/**
 * Created by jamie on 2017/8/10.
 */
public class TrackerMem {
//    static boolean _tracker_mem_add_storage(FdfsGroupInfo pGroup,
//                                            FdfsStorageDetail ppStorageServer, String id,
//                                            String ip_addr, boolean bNeedSleep,
//                                            boolean bNeedLock) throws Exception {
//        //返回值:bool *bInserted
//        int result;
//        if (!"".equals(ip_addr))throw new Exception("ip address is empty!");
//
//
//        if (id != null) {
//            if (g_use_storage_id) {
//                result = fdfs_check_storage_id( \
//                        pGroup -> group_name, id);
//                if (result != 0) {
//                    logError("file: "__FILE__", line: %d, " \
//                            "check storage id fail, " \
//                            "group_name: %s, id: %s, " \
//                            "storage ip: %s, errno: %d, " \
//                            "error info: %s", __LINE__, \
//                            pGroup -> group_name, id, ip_addr, \
//                            result, STRERROR(result));
//                    return result;
//                }
//            }
//
//            storage_id = id;
//        } else if (g_use_storage_id) {
//            FDFSStorageIdInfo * pStorageIdInfo;
//            pStorageIdInfo = fdfs_get_storage_id_by_ip( \
//                    pGroup -> group_name, ip_addr);
//            if (pStorageIdInfo == NULL) {
//                logError("file: "__FILE__", line: %d, " \
//                        "get storage id info fail, " \
//                        "group_name: %s, storage ip: %s", \
//                        __LINE__, pGroup -> group_name, ip_addr);
//                return ENOENT;
//            }
//            storage_id = pStorageIdInfo -> id;
//        } else {
//            storage_id = ip_addr;
//        }
//
//        if (bNeedLock && (result = pthread_mutex_lock( & mem_thread_lock)) !=0)
//        {
//            logError("file: "__FILE__", line: %d, " \
//                    "call pthread_mutex_lock fail, " \
//                    "errno: %d, error info: %s", \
//                    __LINE__, result, STRERROR(result));
//            return result;
//        }
//
//        do {
//            result = 0;
//		*bInserted = false;
//		*ppStorageServer = tracker_mem_get_storage(pGroup, storage_id);
//            if (*ppStorageServer != NULL)
//            {
//                if (g_use_storage_id) {
//                    memcpy(( * ppStorageServer)->ip_addr, ip_addr, \
//                    IP_ADDRESS_SIZE);
//                }
//
//                if (( * ppStorageServer)->status == FDFS_STORAGE_STATUS_DELETED \
//			 ||( * ppStorageServer)->status == FDFS_STORAGE_STATUS_IP_CHANGED)
//                {
//                    ( * ppStorageServer)->status = FDFS_STORAGE_STATUS_INIT;
//                }
//
//                break;
//            }
//
//            if (pGroup -> count >= pGroup -> alloc_size) {
//                result = tracker_mem_realloc_store_servers( \
//                        pGroup, 1, bNeedSleep);
//                if (result != 0) {
//                    break;
//                }
//            }
//
//		*ppStorageServer = *(pGroup -> all_servers + pGroup -> count);
//            snprintf(( * ppStorageServer)->id, FDFS_STORAGE_ID_MAX_SIZE, \
//            "%s", storage_id);
//            memcpy(( * ppStorageServer)->ip_addr, ip_addr, IP_ADDRESS_SIZE);
//
//            tracker_mem_insert_into_sorted_servers( * ppStorageServer, \
//            pGroup -> sorted_servers, pGroup -> count);
//            pGroup -> count++;
//            pGroup -> chg_count++;
//
//		*bInserted = true;
//        } while (0);
//
//        if (bNeedLock && pthread_mutex_unlock( & mem_thread_lock) !=0)
//        {
//            logError("file: "__FILE__", line: %d, "   \
//                    "call pthread_mutex_unlock fail", \
//                    __LINE__);
//        }
//
//        return result;
//    }
}
