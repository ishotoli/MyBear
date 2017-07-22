package io.mybear.tracker.command;

import io.mybear.common.utils.ProtocolUtil;
import io.mybear.net2.tracker.TrackerByteBufferArray;
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
public class ServiceQueryFetchOneCommand extends TrackerCommand {
    public static final int LENGTH = 0;
    private static final Logger logger = LoggerFactory.getLogger(ServiceQueryFetchOneCommand.class);

    @Override
    public void handle(TrackerConnection conn, TrackerMessage message) {
        logger.debug("deal with " + "TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ONE");
        byte state = 0;
        TrackerByteBufferArray array = message.getData();
        array.getLastByteBuffer().flip();
        array.getLastByteBuffer().position(10);
        String groupName = ProtocolUtil.getGroupName(array.getLastByteBuffer());
        logger.debug("groupName:" + groupName);
        message.setPosition(16);
        byte[] bytes = message.getRestByteArray();
        String fileName = new String(bytes);
        logger.debug("fileName:" + fileName);
        ByteBuffer res = buildHeader(conn.getWriteBufferArray().getWritedBlockLst().get(0), 0, TRACKER_PROTO_CMD_RESP, 0);
        res.position(26);
        ProtocolUtil.setIP(res, "127.0.0.1");
        ProtocolUtil.setPort(res, 23000);
        ProtocolUtil.setIP(res, "127.0.0.2");
        res.putLong(0, 39);
        res.position(39 + 10);
        conn.doWriteQueue();
        message.getData().getLastByteBuffer().clear();//连接复用,清空数组
    }
}
