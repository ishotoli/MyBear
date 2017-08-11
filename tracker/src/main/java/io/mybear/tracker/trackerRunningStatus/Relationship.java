package io.mybear.tracker.trackerRunningStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jamie on 2017/6/21.
 */
public class Relationship {
    private static final Logger LOGGER = LoggerFactory.getLogger(Relationship.class);
    public static boolean g_if_leader_self = false;  //if I am leader

//    static int fdfs_ping_leader(RelationshipCon pTrackerServer) {
//        //发送ping
//        ByteBuffer byteBuffer = ProtocolUtil.buildHeader(0, TrackerProto.TRACKER_PROTO_CMD_TRACKER_PING_LEADER, 0);
//        pTrackerServer.sendData(byteBuffer);
//        //接收回复
//        ByteBuffer pInBuff = ByteBuffer.allocate((FDFS_GROUP_NAME_MAX_LEN + FDFS_STORAGE_ID_MAX_SIZE) * FDFS_MAX_GROUPS);
//        pTrackerServer.recData(pInBuff);
//        int in_bytes = pInBuff.position();
//        if (in_bytes == 0) {
//            return 0;
//        } else if (in_bytes % (FDFS_GROUP_NAME_MAX_LEN + FDFS_STORAGE_ID_MAX_SIZE) != 0) {
//            //todo
//            LOGGER.error("tracker server ip: %s, invalid body length: ");
//            return EINVAL;
//        }
//      int  success_count = 0;
//        for (int p=0; p<in_bytes; p += (FDFS_GROUP_NAME_MAX_LEN + FDFS_STORAGE_ID_MAX_SIZE))
//        {
//        memset(group_name, 0, sizeof(group_name));
//        memset(trunk_server_id, 0, sizeof(trunk_server_id));
//
//        pEnd = in_buff + in_bytes;
//        memset( & header, 0, sizeof(header));
//        header.cmd =;
//        result = tcpsenddata_nb(pTrackerServer -> sock, & header, \
//        sizeof(header), g_fdfs_network_timeout);
//        if (result != 0) {
//            logError("file: "__FILE__", line: %d, " \
//                    "tracker server ip: %s, send data fail, " \
//                    "errno: %d, error info: %s", \
//                    __LINE__, pTrackerServer -> ip_addr, \
//                    result, STRERROR(result));
//            return result;
//        }
//
//        pInBuff = in_buff;
//        if ((result = fdfs_recv_response(pTrackerServer, & pInBuff, \
//        sizeof(in_buff), &in_bytes)) !=0)
//        {
//            logError("file: "__FILE__", line: %d, "
//                    "fdfs_recv_response fail, result: %d",
//                    __LINE__, result);
//            return result;
//        }
//
//        if (in_bytes == 0) {
//            return 0;
//        } else if (in_bytes % (FDFS_GROUP_NAME_MAX_LEN + \
//        FDFS_STORAGE_ID_MAX_SIZE) !=0)
//        {
//            logError("file: "__FILE__", line: %d, " \
//                    "tracker server ip: %s, invalid body length: " \
//                    "%"PRId64, __LINE__, \
//                    pTrackerServer -> ip_addr, in_bytes);
//            return EINVAL;
//        }
//
//        success_count = 0;
//        memset(group_name, 0, sizeof(group_name));
//        memset(trunk_server_id, 0, sizeof(trunk_server_id));
//
//        pEnd = in_buff + in_bytes;
//        for (p = in_buff; p < pEnd; p += FDFS_GROUP_NAME_MAX_LEN + \
//        FDFS_STORAGE_ID_MAX_SIZE)
//        {
//            memcpy(group_name, p, FDFS_GROUP_NAME_MAX_LEN);
//            memcpy(trunk_server_id, p + FDFS_GROUP_NAME_MAX_LEN, \
//                    FDFS_STORAGE_ID_MAX_SIZE - 1);
//
//            pGroup = tracker_mem_get_group(group_name);
//            if (pGroup == NULL) {
//                logWarning("file: "__FILE__", line: %d, " \
//                        "tracker server ip: %s, group: %s not exists", \
//                        __LINE__, pTrackerServer -> ip_addr, group_name);
//                continue;
//            }
//
//            if (*trunk_server_id == '\0')
//            {
//            *(pGroup -> last_trunk_server_id) = '\0';
//                pGroup -> pTrunkServer = NULL;
//                success_count++;
//                continue;
//            }
//
//            pGroup -> pTrunkServer = tracker_mem_get_storage(pGroup, \
//                    trunk_server_id);
//            if (pGroup -> pTrunkServer == NULL) {
//                logWarning("file: "__FILE__", line: %d, " \
//                        "tracker server ip: %s, group: %s, " \
//                        "trunk server: %s not exists", \
//                        __LINE__, pTrackerServer -> ip_addr, \
//                        group_name, trunk_server_id);
//            }
//            snprintf(pGroup -> last_trunk_server_id, sizeof( \
//                    pGroup -> last_trunk_server_id), "%s", trunk_server_id);
//            success_count++;
//        }
//
//        if (success_count > 0) {
//            tracker_save_groups();
//        }
//
//        return 0;
//    }

    public static void main(String[] args) {

    }

}
