package io.mybear.tracker.command;

import io.mybear.net2.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageBeatCommand extends TrackerCommand {
    private static final Logger logger = LoggerFactory.getLogger(StorageBeatCommand.class);

    @Override
    public void handle(Connection conn) {
        logger.debug("deal with storage beat.");
    }
}
