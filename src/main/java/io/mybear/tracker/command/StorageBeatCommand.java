package io.mybear.tracker.command;

import io.mybear.common.ErrorNo;
import io.mybear.net2.tracker.TrackerConnection;
import io.mybear.net2.tracker.TrackerMessage;
import io.mybear.tracker.types.FdfsStorageDetail;
import io.mybear.tracker.types.FdfsStorageStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageBeatCommand extends TrackerCommand {
    private static final Logger logger = LoggerFactory.getLogger(StorageBeatCommand.class);

    public static final int STORAGE_BEAT_PACKET_LENGTH = 348;

    @Override
    public void handle(TrackerConnection conn, TrackerMessage message) {
        logger.debug("deal with storage beat.");

        FdfsStorageDetail storageServer = conn.getClientInfo().getStorage();
        FdfsStorageStat storageStat = storageServer.getState();
        byte state = 0;

        if(message.getPkgLen() == STORAGE_BEAT_PACKET_LENGTH){
            storageStat.getConnection().setAllocCount(message.readInt());
            storageStat.getConnection().setCurrentCount(message.readInt());
            storageStat.getConnection().setMaxCount(message.readInt());
            storageStat.setTotalUploadCount(message.readLong());
            storageStat.setSuccessUploadCount(message.readLong());
            storageStat.setTotalAppendCount(message.readLong());
            storageStat.setSuccessAppendCount(message.readLong());
            storageStat.setTotalModifyCount(message.readLong());
            storageStat.setSuccessModifyCount(message.readLong());
            storageStat.setTotalTruncateCount(message.readLong());
            storageStat.setSuccessTruncateCount(message.readLong());
            storageStat.setTotalDownloadCount(message.readLong());
            storageStat.setSuccessDownloadCount(message.readLong());
            storageStat.setTotalSetMetaCount(message.readLong());
            storageStat.setSuccessSetMetaCount(message.readLong());
            storageStat.setTotalDeleteCount(message.readLong());
            storageStat.setSuccessDeleteCount(message.readLong());
            storageStat.setTotalGetMetaCount(message.readLong());
            storageStat.setSuccessGetMetaCount(message.readLong());
            storageStat.getLastSourceUpdate().setTime(message.readLong());
            storageStat.getLastSyncUpdate().setTime(message.readLong());
            storageStat.setTotalCreateLinkCount(message.readLong());
            storageStat.setSuccessCreateLinkCount(message.readLong());
            storageStat.setTotalDeleteLinkCount(message.readLong());
            storageStat.setSuccessDeleteLinkCount(message.readLong());
            storageStat.setTotalUploadBytes(message.readLong());
            storageStat.setSuccessUploadBytes(message.readLong());
            storageStat.setTotalAppendBytes(message.readLong());
            storageStat.setSuccessAppendBytes(message.readLong());
            storageStat.setTotalModifyBytes(message.readLong());
            storageStat.setSuccessModifyBytes(message.readLong());
            storageStat.setTotalDownloadBytes(message.readLong());
            storageStat.setSuccessDownloadBytes(message.readLong());
            storageStat.setTotalSyncInBytes(message.readLong());
            storageStat.setSuccessSyncInBytes(message.readLong());
            storageStat.setTotalSyncOutBytes(message.readLong());
            storageStat.setSuccessSyncOutBytes(message.readLong());
            storageStat.setTotalFileOpenCount(message.readLong());
            storageStat.setSuccessFileOpenCount(message.readLong());
            storageStat.setTotalFileReadCount(message.readLong());
            storageStat.setSuccessFileReadCount(message.readLong());
            storageStat.setTotalFileWriteCount(message.readLong());
            storageStat.setSuccessFileWriteCount(message.readLong());

            // TODO: 写入文件
        }else if(message.getPkgLen() > STORAGE_BEAT_PACKET_LENGTH){
            logger.error("cmd={}, client ip: {}, package size {} is not correct,  expect length: 0 or {}"
                    , message.getCmd(), conn.getHost(), message.getPkgLen(), STORAGE_BEAT_PACKET_LENGTH);
            state = ErrorNo.EINVAL;
        }

        if(state == 0){
            // TODO: tracker_mem_active_store_server(conn.getClientInfo().getGroup(), storageServer)

            storageStat.getLastHeartBeatTime().setTime(System.currentTimeMillis());
        }

        // TODO: tracker_check_and_sync(conn, state)
        message.setStatus(state);
    }
}
