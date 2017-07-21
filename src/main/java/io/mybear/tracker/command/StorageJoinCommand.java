package io.mybear.tracker.command;

import io.mybear.common.ErrorNo;
import io.mybear.common.constants.CommonConstant;
import io.mybear.net2.tracker.TrackerConnection;
import io.mybear.net2.tracker.TrackerMessage;
import io.mybear.tracker.types.FdfsStorageJoinBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageJoinCommand extends TrackerCommand {
    private static final Logger logger = LoggerFactory.getLogger(StorageJoinCommand.class);

    // 具体数值待定
    public static final int STORAGE_JOIN_PACKET_LENGTH = 348;

    @Override
    public void handle(TrackerConnection conn, TrackerMessage message) {
        logger.debug("deal with storage join");

        byte state = 0;
        if(message.getPkgLen() < STORAGE_JOIN_PACKET_LENGTH){
            logger.error("cmd={}, client ip: {}, package size {} is not correct,  expect length >= {}"
                , message.getCmd(), conn.getHost(), message.getPkgLen(), STORAGE_JOIN_PACKET_LENGTH);
            state = ErrorNo.EINVAL;
        }

        FdfsStorageJoinBody joinBody = new FdfsStorageJoinBody();
        joinBody.setTrackerCount(message.readLong());
        if(joinBody.getTrackerCount() <= 0 || joinBody.getTrackerCount() > CommonConstant.FDFS_MAX_TRACKERS){
            logger.error("cmd={}, client ip: {}, tracker count {} is invalid, it <= 0 or > {}"
                , message.getCmd(), conn.getHost(), joinBody.getTrackerCount(), CommonConstant.FDFS_MAX_TRACKERS);
            state = ErrorNo.EINVAL;
        }



    }
}
