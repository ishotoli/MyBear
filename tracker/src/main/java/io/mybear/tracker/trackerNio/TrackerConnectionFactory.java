package io.mybear.tracker.trackerNio;


import java.io.IOException;
import java.nio.channels.SocketChannel;

public class TrackerConnectionFactory extends ConnectionFactory {
    private static final NIOHandler handler = new TrackerNioHandler();

    @Override
    protected Connection makeConnection(SocketChannel channel) throws IOException {
        return new TrackerConnection(channel);
    }

    @Override
    protected NIOHandler getNIOHandler() {
        return handler;
    }
}
