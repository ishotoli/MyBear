package io.mybear.tracker.trackerNio;

import io.mybear.common.utils.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author wuzh
 */
public abstract class Connection implements ClosableConnection {
    private static final int OP_NOT_READ = ~SelectionKey.OP_READ;
    private static final int OP_NOT_WRITE = ~SelectionKey.OP_WRITE;
    public static Logger LOGGER = LoggerFactory.getLogger(Connection.class);
    protected final SocketChannel channel;
    private final WriteQueue writeQueue = new WriteQueue(1024 * 1024 * 16);
    private final ReentrantLock writeQueueLock = new ReentrantLock();
    protected String host;
    protected int port;
    protected int localPort;
    protected long id;
    protected boolean isClosed;
    protected boolean isSocketClosed;
    protected long startupTime;
    protected long lastReadTime;
    protected long lastWriteTime;
    protected int netInBytes;
    protected int netOutBytes;
    protected int pkgTotalSize;
    protected int pkgTotalCount;
    @SuppressWarnings("rawtypes")
    protected NIOHandler handler;
    protected SelectionKey processKey;
    private State state = State.connecting;
    private Direction direction = Direction.in;
    private ByteBuffer readBuffer;
    private int readBufferOffset;
    private ByteBuffer writeBuffer;
    private long lastLargeMessageTime;
    private long idleTimeout;
    private long lastPerfCollectTime;
    private int maxPacketSize;
    private int packetHeaderSize;
    private ReactorBufferPool myBufferPool;

    public Connection(SocketChannel channel) {
        this.channel = channel;
        this.isClosed = false;
        this.startupTime = TimeUtil.currentTimeMillis();
        this.lastReadTime = startupTime;
        this.lastWriteTime = startupTime;
        this.lastPerfCollectTime = startupTime;
        // this.readBuffer.limit(10);
    }

    public abstract void asynRead() throws IOException;

    public void resetPerfCollectTime() {
        netInBytes = 0;
        netOutBytes = 0;
        pkgTotalCount = 0;
        pkgTotalSize = 0;
        lastPerfCollectTime = TimeUtil.currentTimeMillis();
    }

    public long getLastPerfCollectTime() {
        return lastPerfCollectTime;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public boolean isIdleTimeout() {
        return TimeUtil.currentTimeMillis() > Math.max(lastWriteTime, lastReadTime) + idleTimeout;

    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public long getStartupTime() {
        return startupTime;
    }

    public long getLastReadTime() {
        return lastReadTime;
    }

    public long getLastWriteTime() {
        return lastWriteTime;
    }

    public long getNetInBytes() {
        return netInBytes;
    }

    public long getNetOutBytes() {
        return netOutBytes;
    }

    private ByteBuffer allocate() {
        return NetSystem.getInstance().getBufferPool().allocate();
    }

    private final void recycle(ByteBuffer buffer) {
        NetSystem.getInstance().getBufferPool().recycle(buffer);
    }

    @SuppressWarnings("rawtypes")
    public NIOHandler getHandler() {
        return this.handler;
    }

    public void setHandler(NIOHandler<? extends Connection> handler) {
        this.handler = handler;

    }

    @SuppressWarnings("unchecked")
    public void handle(final ByteBuffer data, final int start, final int readedLength) {
        // handler.handle(this, data, start, readedLength);
    }

    public boolean isConnected() {
        return (this.state == State.connected);
    }

    private ByteBuffer compactReadBuffer(ByteBuffer buffer, int offset) {
        if (buffer == null)
            return null;
        buffer.limit(buffer.position());
        buffer.position(offset);
        buffer = buffer.compact();
        readBufferOffset = 0;
        return buffer;
    }

    @SuppressWarnings("unchecked")
    public void close(String reason) {
        if (!isClosed) {
            closeSocket();
            this.cleanup();
            isClosed = true;
            NetSystem.getInstance().removeConnection(this);
            LOGGER.info("close connection,reason:" + reason + " ," + this.getClass());
            if (handler != null) {
                handler.onClosed(this, reason);
            }
        }
    }

    /**
     * asyn close (executed later in thread) 该函数使用多线程异步关闭
     * Connection，会存在并发安全问题，暂时注释
     *
     * @param
     */
    // public void asynClose(final String reason) {
    // Runnable runn = new Runnable() {
    // public void run() {
    // Connection.this.close(reason);
    // }
    // };
    // NetSystem.getInstance().getTimer().schedule(runn, 1, TimeUnit.SECONDS);
    //
    // }
    public boolean isClosed() {
        return isClosed;
    }

    public void idleCheck() {
        if (isIdleTimeout()) {
            LOGGER.info(toString() + " idle timeout");
            close(" idle ");
        }
    }

    /**
     * 清理资源
     */

    protected void cleanup() {

        // 清理资源占用
        myBufferPool.recycle(readBuffer);
        this.writeQueue.recycle();
        if (writeBuffer != null) {
            // recycle(writeBuffer);
            this.writeBuffer = null;
        }
    }

    public WriteQueue getWriteQueue() {
        return writeQueue;
    }

    @SuppressWarnings("unchecked")
    public void register(Selector selector, ReactorBufferPool myBufferPool) throws IOException {
        this.myBufferPool = myBufferPool;
        processKey = channel.register(selector, SelectionKey.OP_READ, this);
        NetSystem.getInstance().addConnection(this);
        //this.readBufferArray = myBufferPool.allocateByteBuffer()
        readBuffer = this.myBufferPool.allocateByteBuffer();
        this.handler.onConnected(this);

    }

    public void doWriteQueue() throws IOException {
        boolean noMoreData = write0();
        lastWriteTime = TimeUtil.currentTimeMillis();
        if (noMoreData) {
            if ((processKey.isValid() && (processKey.interestOps() & SelectionKey.OP_WRITE) != 0)) {
                disableWrite();
            }
        } else {
            if ((processKey.isValid() && (processKey.interestOps() & SelectionKey.OP_WRITE) == 0)) {
                enableWrite(false);
            }
        }
    }


    public void write(ByteBufferArray bufferArray) {
        writeQueueLock.lock();
        try {
            writeQueue.add(bufferArray);
        } finally {
            writeQueueLock.unlock();
        }
        this.enableWrite(true);
    }

    private boolean write0() throws IOException {
        for (; ; ) {
            int written = 0;
            ByteBufferArray arry = writeQueue.pull();
            if (arry == null) {
                break;
            }
            for (ByteBuffer buffer : arry.getWritedBlockLst()) {
                buffer.flip();
                if (buffer != null) {
                    while (buffer.hasRemaining()) {
                        written = channel.write(buffer);
                        if (written > 0) {
                            netOutBytes += written;
                            NetSystem.getInstance().addNetOutBytes(written);
                        } else {
                            break;
                        }
                    }

                }
            }
            if (arry.getLastByteBuffer().hasRemaining()) {
                return false;
            } else {
                writeBuffer = null;
                // recycle(buffer);
                arry.recycle();
            }
        }
        return true;
    }

    protected void disableWrite() {
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() & OP_NOT_WRITE);
        } catch (Exception e) {
            LOGGER.warn("can't disable write " + e + " con " + this);
        }

    }

    protected void enableWrite(boolean wakeup) {
        boolean needWakeup = false;
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
            needWakeup = true;
        } catch (Exception e) {
            LOGGER.warn("can't enable write " + e);

        }
        if (needWakeup && wakeup) {
            processKey.selector().wakeup();
        }
    }

    public void disableRead() {
        SelectionKey key = this.processKey;
        key.interestOps(key.interestOps() & OP_NOT_READ);
    }

    public void enableRead() {
        boolean needWakeup = false;
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            needWakeup = true;
        } catch (Exception e) {
            LOGGER.warn("enable read fail " + e);
        }
        if (needWakeup) {
            processKey.selector().wakeup();
        }
    }


    private void closeSocket() {
        if (channel != null) {
            boolean isSocketClosed = true;
            try {
                processKey.cancel();
                channel.close();
            } catch (Throwable e) {
            }
            boolean closed = isSocketClosed && (!channel.isOpen());
            if (!closed) {
                LOGGER.warn("close socket of connnection failed " + this);
            }

        }
    }

    public State getState() {
        return state;
    }

    public void setState(State newState) {
        this.state = newState;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction in) {
        this.direction = in;

    }

    public int getPkgTotalSize() {
        return pkgTotalSize;
    }

    public int getPkgTotalCount() {
        return pkgTotalCount;
    }

    @Override
    public String toString() {
        return "Connection [host=" + host + ",  port=" + port + ", id=" + id + ", state=" + state + ", direction="
                + direction + ", startupTime=" + startupTime + ", lastReadTime=" + lastReadTime + ", lastWriteTime="
                + lastWriteTime + "]";
    }

    public void setMaxPacketSize(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;

    }

    public void setPacketHeaderSize(int packetHeaderSize) {
        this.packetHeaderSize = packetHeaderSize;

    }

    public ReactorBufferPool getMyBufferPool() {
        return myBufferPool;
    }


    public enum State {
        connecting, connected, closing, closed, failed
    }


    // 连接的方向，in表示是客户端连接过来的，out表示自己作为客户端去连接对端Sever
    public enum Direction {
        in, out
    }

}
