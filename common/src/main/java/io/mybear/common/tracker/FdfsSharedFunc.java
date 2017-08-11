package io.mybear.common.tracker;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static io.mybear.common.constants.ErrorNo.EINVAL;
import static io.mybear.common.constants.ErrorNo.ENOENT;

/**
 * Created by jamie on 2017/8/10.
 */
public class FdfsSharedFunc {
    final static List<FDFSStorageIdInfo> g_storage_ids_by_ip = null;  //sorted by group name and storage IP
    final static List<FDFSStorageIdInfo> g_storage_ids_by_id = null; //sorted by storage ID
    final static List<FDFSStorageIdInfo> g_storage_ids_by_ip_port = null;  //sorted by storage ip and port

    int g_storage_id_count = 0;

    /**
     * @param tid
     * @return
     */
    public static FDFSStorageIdInfo fdfs_get_storage_by_id(String tid) {
        FDFSStorageIdInfo target = new FDFSStorageIdInfo();
        target.id = tid;
        FDFSStorageIdInfo ppFound;
        int index = Collections.binarySearch(g_storage_ids_by_id, target, Comparator.comparing((x) -> x.group_name));
        ppFound = g_storage_ids_by_id.get(index);
        if (ppFound == null) {
            return ppFound;
        } else {
            return ppFound;
        }
    }

    /**
     * fdfs_check_storage_id
     *
     * @return
     */
    int fdfs_check_storage_id(String group_name, String id) {
        FDFSStorageIdInfo pFound;
        pFound = fdfs_get_storage_by_id(id);
        if (pFound == null) {
            return ENOENT;
        }
        if (pFound.group_name == null) {
            return ENOENT;
        }
        return pFound.group_name.equals(group_name) ? 0 : EINVAL;
    }

    /**
     *
     * @param leaderPort
     * @return
     */
//  public  static   int fdfs_get_tracker_leader_index_ex(TrackerServerGroup pServerGroup, InetSocketAddress leaderIp)
//    {
//       Arrays.binarySearch(pServerGroup.tracker_servers,leaderIp,(x,y)->x.getHostName().equals(y.)));
//        ConnectionInfo *pServer;
//        ConnectionInfo *pEnd;
//
//        if (pServerGroup->server_count == 0)
//        {
//            return -1;
//        }
//
//        pEnd = pServerGroup->servers + pServerGroup->server_count;
//        for (pServer=pServerGroup->servers; pServer<pEnd; pServer++)
//        {
//            if (strcmp(pServer->ip_addr, leaderIp) == 0 &&
//            pServer->port == leaderPort)
//            {
//                return pServer - pServerGroup->servers;
//            }
//        }
//
//        return -1;
//    }
}
