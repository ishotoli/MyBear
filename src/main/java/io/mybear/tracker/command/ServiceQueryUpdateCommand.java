package io.mybear.tracker.command;

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
public class ServiceQueryUpdateCommand extends TrackerCommand {
    public static final int LENGTH = 0;
    private static final Logger logger = LoggerFactory.getLogger(ServiceQueryStoreWithoutGroupOneCommand.class);

    @Override
    public void handle(TrackerConnection conn, TrackerMessage message) {
        logger.debug("deal with " + "TRACKER_PROTO_CMD_SERVICE_QUERY_UPDATE");
        byte state = 0;
        String groupName = ProtocolUtil.getGroupName(message.getData().getLastByteBuffer());
        logger.debug("groupName:" + groupName);
        byte[] bytes = message.getRestByteArray();
        String fileName = new String(bytes);
        logger.debug("fileName:" + fileName);
        ByteBuffer res = buildHeader(conn.getWriteBufferArray().getLastByteBuffer(), 0, TRACKER_PROTO_CMD_RESP, 0);
        res.position(26);
        ProtocolUtil.setIP(res, "127.0.0.1");
        ProtocolUtil.setPort(res, 23000);
        ProtocolUtil.setIP(res, "127.0.0.2");
        res.putLong(0, 54);
        conn.doWriteQueue();
    }
}
