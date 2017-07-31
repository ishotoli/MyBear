package io.mybear.tracker.command;

import io.mybear.tracker.trackerNio.TrackerConnection;
import io.mybear.tracker.trackerNio.TrackerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jamie on 2017/7/25.
 * FDFS_PROTO_CMD_QUIT
 */
public class QuitCommand extends TrackerCommand {
    private static final Logger logger = LoggerFactory.getLogger(QuitCommand.class);

    @Override
    public void handle(TrackerConnection conn, TrackerMessage message) {
        logger.debug("deal with " + "FDFS_PROTO_CMD_QUIT");
        conn.close("FDFS_PROTO_CMD_QUIT");
    }
}
