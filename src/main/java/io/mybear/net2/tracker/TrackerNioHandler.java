package io.mybear.net2.tracker;

import io.mybear.net2.NIOHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackerNioHandler implements NIOHandler<TrackerConnection> {
    private static final Logger logger = LoggerFactory.getLogger(TrackerNioHandler.class);

    @Override
    public void onConnected(TrackerConnection conn) throws IOException {
        logger.debug("onConnected: {}", conn);
    }

    @Override
    public void onConnectFailed(TrackerConnection conn, Throwable e) {
        logger.debug("onConnectFailed: {}", conn);

    }

    @Override
    public void onClosed(TrackerConnection conn, String reason) {
        logger.debug("onClosed: {}", conn);
        logger.debug("reason: {}", reason);
    }

    @Override
    public void handle(TrackerConnection con, ByteBuffer nioData) {
        // nothing to do
    }

    public void handle(TrackerConnection conn, TrackerByteBufferArray nioData) {
        long pkgLen = nioData.getCurPacageLength();

        logger.debug("handle: {}", nioData);
    }

    @Override
    public void handleEnd(TrackerConnection con, ByteBuffer nioData) {
        // wtf...
    }

    @Override
    public void handleMetaData(TrackerConnection con, ByteBuffer nioData) {
        // wtf...
    }
}
