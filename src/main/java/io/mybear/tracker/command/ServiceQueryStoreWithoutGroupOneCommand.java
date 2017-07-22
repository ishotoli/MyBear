package io.mybear.tracker.command;

import io.mybear.common.ErrorNo;
import io.mybear.common.utils.ProtocolUtil;
import io.mybear.net2.tracker.TrackerConnection;
import io.mybear.net2.tracker.TrackerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static io.mybear.common.utils.ProtocolUtil.buildHeader;
import static org.csource.fastdfs.ProtoCommon.TRACKER_PROTO_CMD_RESP;

/**
 * Created by jamie on 2017/7/22.
 */
public class ServiceQueryStoreWithoutGroupOneCommand extends TrackerCommand {
    public static final int LENGTH = 0;
    private static final Logger logger = LoggerFactory.getLogger(ServiceQueryStoreWithoutGroupOneCommand.class);

    @Override
    public void handle(TrackerConnection conn, TrackerMessage message) {
        logger.debug("deal with " + "TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE");

        byte state = 0;
        if (message.getPkgLen() < LENGTH) {
            logger.error("cmd={}, client ip: {}, package size {} is not correct,  expect length >= {}"
                    , message.getCmd(), conn.getHost(), message.getPkgLen(), LENGTH);
            state = ErrorNo.EINVAL;
        }
        String resIp = "127.0.0.1";
        String resGroupName = "Hello";
        int resPort = 23000;
        int resIndex = 0;
        ByteBuffer res = buildHeader(conn.getWriteBufferArray().getWritedBlockLst().get(0), 30, TRACKER_PROTO_CMD_RESP, 0);
        ProtocolUtil.setGroupName(res, resGroupName);
        ProtocolUtil.setIP(res, resIp);
        ProtocolUtil.setPort(res, resPort);
        ProtocolUtil.setStorePathIndex(res, resIndex);
        conn.doWriteQueue();
        message.getData().getLastByteBuffer().clear();//连接复用,清空数组
    }
}
