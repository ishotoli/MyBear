package io.mybear.storage.trackerClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * Created by jamie on 2017/8/7.
 */
public class TrackerClientConnection {
    public static final List<TrackerClientConnection> connections = new java.util.concurrent.CopyOnWriteArrayList<>();
    public ByteBuffer buffer;
    public int srcStorageStatus = 0; //returned by tracker server
    public int myReportStatus = 0;  //returned by tracker server
    public boolean needRejoinTracker = false;
    SocketChannel socketChannel;


    public TrackerClientConnection(ByteBuffer buffer, String ip, int port) throws IOException {
        this.buffer = buffer;
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(true);
        InetSocketAddress address = new InetSocketAddress(ip, port);
        socketChannel.connect(address);
        Channels.newChannel(socketChannel.socket().getInputStream());
        connections.add(this);
    }

    public static void main(String[] args) throws IOException {
        TrackerClientConnection connection = new TrackerClientConnection(ByteBuffer.allocateDirect(128), "127.0.0.1", 222);
    }

    public void sendData() throws IOException {
        if (-1 == socketChannel.write(buffer)) {
            connections.remove(this);
        }
    }

    public ByteBuffer getReadBuffer() throws IOException {
        return this.buffer;
    }

    public ByteBuffer getWriteBuffer() throws IOException {
        this.buffer.clear();
        return this.buffer;
    }

    public void recData() throws IOException {
        if (-1 == socketChannel.read(buffer)) {
            connections.remove(this);
        }
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public int getSrcStorageStatus() {
        return srcStorageStatus;
    }

    public void setSrcStorageStatus(int srcStorageStatus) {
        this.srcStorageStatus = srcStorageStatus;
    }

    public int getMyReportStatus() {
        return myReportStatus;
    }

    public void setMyReportStatus(int myReportStatus) {
        this.myReportStatus = myReportStatus;
    }

    public boolean isNeedRejoinTracker() {
        return needRejoinTracker;
    }

    public void setNeedRejoinTracker(boolean needRejoinTracker) {
        this.needRejoinTracker = needRejoinTracker;
    }

    public boolean isClosed() {
        return !socketChannel.isOpen();
    }
}
