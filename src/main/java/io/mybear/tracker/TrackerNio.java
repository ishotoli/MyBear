package io.mybear.tracker;

import io.mybear.net2.Connection;
import io.mybear.net2.NIOHandler;
import io.mybear.tracker.command.CommandFactory;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jamie on 2017/6/21.
 */
public class TrackerNio implements NIOHandler<Connection>{

    @Override
    public void onConnected(Connection con) throws IOException {
        // nothing to do
    }

    @Override
    public void onConnectFailed(Connection con, Throwable e) {
        // nothing to do
    }

    @Override
    public void onClosed(Connection con, String reason) {
        // nothing to do
    }

    @Override
    public void handle(Connection con, ByteBuffer nioData) {
        byte cmd = 0;
        CommandFactory.getHandler(cmd).handle(con);
    }

    @Override
    public void handleEnd(Connection con, ByteBuffer nioData) {
        // nothing to do
    }

    @Override
    public void handleMetaData(Connection con, ByteBuffer nioData) {
        // nothing to do
    }
}
