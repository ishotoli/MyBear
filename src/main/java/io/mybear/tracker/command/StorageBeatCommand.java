package io.mybear.tracker.command;

import io.mybear.net2.tracker.TrackerConnection;
import io.mybear.net2.tracker.TrackerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageBeatCommand extends TrackerCommand {
    private static final Logger logger = LoggerFactory.getLogger(StorageBeatCommand.class);

    @Override
    public void handle(TrackerConnection conn, TrackerMessage message) {
        logger.debug("deal with storage beat.");
        conn.getClientInfo().getStorage().getState().getConnection().setAllocCount(message.readInt());
    }
}
