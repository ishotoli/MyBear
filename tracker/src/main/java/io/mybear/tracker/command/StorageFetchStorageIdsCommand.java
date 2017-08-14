package io.mybear.tracker.command;

import io.mybear.common.constants.ErrorNo;
import io.mybear.common.constants.SizeOfConstant;
import io.mybear.common.tracker.FDFSStorageIdInfo;
import io.mybear.tracker.trackerNio.TrackerByteBufferArray;
import io.mybear.tracker.trackerNio.TrackerConnection;
import io.mybear.tracker.trackerNio.TrackerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static io.mybear.common.constants.TrackerProto.TRACKER_PROTO_CMD_RESP;
import static io.mybear.common.constants.TrackerProto.TRACKER_PROTO_CMD_STORAGE_GET_GROUP_NAME;
import static io.mybear.common.constants.config.TrackerGlobal.g_min_buff_size;
import static io.mybear.common.constants.config.TrackerGlobal.g_use_storage_id;
import static io.mybear.common.tracker.FdfsSharedFunc.g_storage_id_count;
import static io.mybear.common.tracker.FdfsSharedFunc.g_storage_ids_by_ip;

/**
 * Created by jamie on 2017/7/25.
 * TRACKER_PROTO_CMD_STORAGE_FETCH_STORAGE_IDS
 * tracker_deal_fetch_storage_ids
 * 完成了
 */
public class StorageFetchStorageIdsCommand extends TrackerCommand {
    private static final Logger logger = LoggerFactory.getLogger(StorageFetchStorageIdsCommand.class);

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
        if (nPkgLen != SizeOfConstant.SIZE_OF_INT) {
            logger.error("cmd=%d, client ip addr: %s,package size %d is not correct",
                    TRACKER_PROTO_CMD_STORAGE_GET_GROUP_NAME, conn.getHost(), SizeOfConstant.SIZE_OF_TRACKER_HEADER);
            state = ErrorNo.EINVAL;
        }
        int start_index = message.readInt();
        if (start_index < 0 || start_index >= g_storage_id_count) {
            logger.error("client ip addr: %s, invalid offset: %d", conn.getHost(), start_index);
            state = ErrorNo.EINVAL;
            return;
        }
        TrackerByteBufferArray array = conn.writeBufferArray;
        byte[] empty10 = new byte[10];
        array.write(empty10);
        array.write(g_storage_id_count);
        int size = g_storage_ids_by_ip.size();
        int i = start_index;
        for (; i < size; i++) {
            int len = (int) array.getCurPacageLength();
            if (len >= (g_min_buff_size - 64)) {
                break;
            }
            FDFSStorageIdInfo pIdInfo = g_storage_ids_by_ip.get(i);
            String szPortPart;
            if (pIdInfo.port > 0) {
                szPortPart = String.format(":%d", pIdInfo.port);
            } else {
                szPortPart = "";
            }
            array.write(String.format("%s %s %s%s\n", pIdInfo.id, pIdInfo.group_name, pIdInfo.ipAddr, szPortPart).getBytes(StandardCharsets.US_ASCII));
        }
        array.write(i - start_index);
        nPkgLen = (int) array.getTotalBytesLength();
        ByteBuffer byteBuffer = array.getFirstByteBuffer();
        byteBuffer.putLong(0, nPkgLen);
        byteBuffer.put(8, TRACKER_PROTO_CMD_RESP);
        byteBuffer.putLong(9, 0);
        conn.doWriteQueue();
    }
}
