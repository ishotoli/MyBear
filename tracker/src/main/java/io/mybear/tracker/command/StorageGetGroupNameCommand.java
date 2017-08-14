package io.mybear.tracker.command;

import io.mybear.common.constants.CommonConstant;
import io.mybear.common.constants.ErrorNo;
import io.mybear.common.constants.SizeOfConstant;
import io.mybear.common.constants.TrackerProto;
import io.mybear.common.tracker.FDFSStorageIdInfo;
import io.mybear.common.tracker.FdfsSharedFunc;
import io.mybear.common.utils.ProtocolUtil;
import io.mybear.tracker.trackerNio.TrackerConnection;
import io.mybear.tracker.trackerNio.TrackerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static io.mybear.common.constants.CommonConstant.FDFS_GROUP_NAME_MAX_LEN;
import static io.mybear.common.constants.TrackerProto.TRACKER_PROTO_CMD_STORAGE_GET_GROUP_NAME;
import static io.mybear.common.constants.config.TrackerGlobal.g_use_storage_id;

/**
 * Created by jamie on 2017/7/25.
 * 已完成
 * tracker_deal_get_storage_group_name
 * TRACKER_PROTO_CMD_STORAGE_GET_GROUP_NAME
 */
public class StorageGetGroupNameCommand extends TrackerCommand {
    private static final Logger logger = LoggerFactory.getLogger(StorageGetGroupNameCommand.class);

    /**
     * 主要参数
     * ip
     * port
     *
     * @param conn
     * @param message
     */
    @Override
    public void handle(TrackerConnection conn, TrackerMessage message) {
        String ip_addr;
        int port;
        byte state = 0;
        logger.debug("deal with StorageGetGroupNameCommand");
        if (!g_use_storage_id) {
            logger.error("use_storage_id is disabled, can't get group name from storage ip and port!");
            state = ErrorNo.EINVAL;
            return;
        }
        int nPkgLen = (int) message.getPkgLen();
        if (nPkgLen < 4) {
            logger.error("cmd=%d, client ip addr: %s,package size %d is not correct",
                    TRACKER_PROTO_CMD_STORAGE_GET_GROUP_NAME, conn.getHost(), SizeOfConstant.SIZE_OF_TRACKER_HEADER);
            state = ErrorNo.EINVAL;
            return;
        }
        if (nPkgLen == 4) {
            ip_addr = conn.getHost();
            port = message.readPort();
        } else {
            int ip_len = nPkgLen - 4;
            if ((ip_len) >= CommonConstant.IP_ADDRESS_SIZE) {
                logger.error("ip address is too long, length: %d", ip_len);
                state = ErrorNo.ENAMETOOLONG;
                return;
            }
            ip_addr = message.readIP();
            port = message.readPort();
        }
        FDFSStorageIdInfo pFDFSStorageIdInfo = FdfsSharedFunc.fdfs_get_storage_id_by_ip_port(ip_addr, port);
        if (pFDFSStorageIdInfo == null) {
            logger.error("client ip: %s, can't get group name for storage %s:%d", conn.getHost(), ip_addr, port);
            state = ErrorNo.ENOENT;
            return;
        }
        ByteBuffer res = conn.getAvailableWriteByteBuffer();
        ProtocolUtil.buildHeader(res, FDFS_GROUP_NAME_MAX_LEN, TrackerProto.TRACKER_PROTO_CMD_RESP, state);
        ProtocolUtil.setGroupName(res, pFDFSStorageIdInfo.group_name);
        conn.doWriteQueue();
    }
}
