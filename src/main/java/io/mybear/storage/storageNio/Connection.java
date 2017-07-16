package io.mybear.storage.storageNio;


import io.mybear.storage.parserHandler.ParserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static io.mybear.storage.storageNio.Connection.PacketState.*;
import static java.nio.channels.SelectionKey.OP_READ;


/**
 * @author wuzh
 */
public abstract class Connection implements ClosableConnection {
    private static final int OP_NOT_READ = ~OP_READ;
    private static final int OP_NOT_WRITE = ~SelectionKey.OP_WRITE;
    public static Logger LOGGER = LoggerFactory.getLogger(Connection.class);
    protected final SocketChannel channel;
    protected final long startupTime;
    // private final WriteQueue writeQueue = new WriteQueue(1024 * 1024 * 16);
    //private final ReentrantLock writeQueueLock = new ReentrantLock();
    public long length; //data length
    public long offset = 0;
    public int metaDataLength;
    public byte cmd;
    public ParserHandler parserHandler;
    public PacketState packetState;
    public ByteBuffer readBuffer;
    public volatile Object writeBuffer;
    protected String host;
    protected int port;
    protected int localPort;
    protected long id;
    protected boolean isClosed;
    protected boolean isSocketClosed;
    protected long lastReadTime;
    protected long lastWriteTime;
    protected int netInBytes;
    protected int netOutBytes;
    protected int pkgTotalSize;
    protected int pkgTotalCount;
    @SuppressWarnings("rawtypes")
    protected NIOHandler handler;
    private State state = State.connecting;
    private Direction direction = Direction.in;
    private SelectionKey processKey;
    private int readBufferOffset;
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
        this.packetState = header;
        this.offset = 0;
        // this.readBuffer.limit(10);
    }

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

    public void setLastReadTime(long lastReadTime) {
        this.lastReadTime = lastReadTime;
    }

    public long getLastWriteTime() {
        return lastWriteTime;
    }

    public void setLastWriteTime(long lastWriteTime) {
        this.lastWriteTime = lastWriteTime;
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
        if (readBuffer != null)
            myBufferPool.recycle(readBuffer);
        if (writeBuffer != null) {
            if (writeBuffer instanceof ByteBuffer) {
                myBufferPool.recycle((ByteBuffer) writeBuffer);
            } else {
                ((ByteBufferArray) writeBuffer).recycle();
            }
            this.writeBuffer = null;
        }
    }


    @SuppressWarnings("unchecked")
    public void register(Selector selector, ReactorBufferPool myBufferPool) throws IOException {
        this.myBufferPool = myBufferPool;
        processKey = channel.register(selector, OP_READ, this);
        NetSystem.getInstance().addConnection(this);
        //this.readBufferArray = myBufferPool.allocateByteBuffer()
        readBuffer = this.myBufferPool.allocateByteBuffer();
        offset = 0;
        readBuffer.limit(10).position(0);
    }

    public void doWriteQueue() throws IOException {
        try {
            boolean noMoreData = write();
            if (noMoreData) {
                LOGGER.debug("写入完成");
                if ((processKey.isValid() && (processKey.interestOps() & SelectionKey.OP_WRITE) != 0)) {
                    disableWrite();
                }
            } else {
                if ((processKey.isValid() && (processKey.interestOps() & SelectionKey.OP_WRITE) == 0)) {
                    enableWrite(false);
                }
            }
        } catch (IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("caught err:", e);
            }
            close("err:" + e);
        }
    }


    public void write(ByteBufferArray bufferArray) {
        for (ByteBuffer it : bufferArray.getWritedBlockLst()) {
            it.flip();
        }
        this.writeBuffer = bufferArray;
        this.disableRead();
        this.enableWrite(true);
    }

    public void write(ByteBuffer buffer) {
        buffer.flip();
        this.writeBuffer = buffer;
        this.disableRead();
        this.enableWrite(true);
    }

    private boolean write() throws IOException {
        Object data = this.writeBuffer;//被dio线程设置writeBuffer后被唤醒
        if (data != null) {
            if (data instanceof ByteBuffer) {
                ByteBuffer writeBuffer = (ByteBuffer) data;
                if (writeBuffer.hasRemaining()) {
                    channel.write(writeBuffer);
                    lastWriteTime = TimeUtil.currentTimeMillis();
                }
                if (!writeBuffer.hasRemaining()) {
                    this.disableWrite();
                    this.enableRead();
                    this.myBufferPool.recycle(writeBuffer);
                    toHead();
                    return true;
                }
                return false;
            } else {
                int written = 0;
                io.mybear.net2.ByteBufferArray arry = (io.mybear.net2.ByteBufferArray) data;
                for (ByteBuffer buffer : arry.getWritedBlockLst()) {
                    if (buffer != null) {
                        while (buffer.hasRemaining()) {
                            written = channel.write(buffer);
                            if (written > 0) {
                                lastWriteTime = TimeUtil.currentTimeMillis();
                                netOutBytes += written;
                                io.mybear.net2.NetSystem.getInstance().addNetOutBytes(written);
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
                    arry.recycle();
                    return true;
                }
            }
        }
        throw new IOException("写入数据是空的");
    }

    private void disableWrite() {
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() & OP_NOT_WRITE);
        } catch (Exception e) {
            LOGGER.warn("can't disable write " + e + " con " + this);
        }

    }

    public void enableWrite(boolean wakeup) {
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
            key.interestOps(key.interestOps() | OP_READ);
            needWakeup = true;
        } catch (Exception e) {
            LOGGER.warn("enable read fail " + e);
        }
        if (needWakeup) {
            processKey.selector().wakeup();
        }
    }

    void toHead() {
        length = 0;
        metaDataLength = 0;
        offset = 0;
        if (readBuffer == null) {
            readBuffer = myBufferPool.allocateByteBuffer();
        }
        readBuffer.limit(10).position(0);
        this.packetState = header;
    }

    void toPacket() {
        ByteBuffer byteBuffer = readBuffer;
        byteBuffer.position(0);
        byteBuffer.limit(byteBuffer.capacity());
        this.packetState = packet;
    }

    void toMetaData(int metaDatalength) {
        ByteBuffer byteBuffer = readBuffer;
        byteBuffer.position(0);
        try {
            byteBuffer.limit(metaDatalength);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.packetState = metaData;
    }

    void toFix() {
        ByteBuffer byteBuffer = readBuffer;
        long rest = (this.length - this.offset) % byteBuffer.capacity();
        byteBuffer.position(0);
        byteBuffer.limit((int) rest);
        this.packetState = fix;
    }


    /**
     * 异步读取数据,only nio thread call
     *
     * @throws IOException
     */
    protected void asynRead() throws IOException {
        /**
         * 避免重复进入状态机多次操作
         */
        if (this.isClosed || readBuffer == null) {
            return;
        }
        int got = 0;
        boolean needReadChannel = false;//避免多次读取channel却是返回0
        got = channel.read(readBuffer);
        if (got == 0) return;
        LOGGER.debug("解析报文开始->读取通道");
        if (got == -1) {
            closeSocket();
            return;
        }
        lastReadTime = TimeUtil.currentTimeMillis();
        offset += got;
        do {
            if (needReadChannel) {
                LOGGER.debug("读取通道");
                got = channel.read(readBuffer);
                if (got == -1) closeSocket();
                offset += got;
            }
            if (packetState == header) {
                if (readBuffer.position() < 10) {
                    return;
                } else {
                    long len = readBuffer.getLong(0);
                    this.length = len + 10;
                    this.cmd = readBuffer.get(8);
                    int state = readBuffer.get(9);
                    if (len == 0) {
                        LOGGER.debug("解析报文结束->解析得到仅仅有head的请求");
                        parseEndFunction();
                        return;
                    }
                    this.parserHandler = ParserHandler.getParserHandler(cmd);
                    //int mataLen = (int) ParserHandler.getMetaDataLength(cmd, length);
                    int mataLen = parserHandler.getSize();
                    if (mataLen == 0) {
                        LOGGER.debug("遇上特殊报文,没有进一步描述数据包的长度");
                        toFix();
                        needReadChannel = true;
                        continue;
                    }
                    needReadChannel = true;
                    toMetaData(mataLen);
                    continue;
                }
            } else if (packetState == metaData) {
                if (readBuffer.hasRemaining()) {
                    return;
                } else {
                    this.length = this.length - parserHandler.handleMetaData(this, readBuffer);
                    if (offset == length) {
                        LOGGER.debug("解析报文结束->解析得到不含有变长字段的报文");
                        parseEndFunction();
                        return;
                    } else {
                        int cap = readBuffer.capacity();
                        if (length > cap) {
                            needReadChannel = true;
                            toFix();
                            continue;
                        } else {
                            readBuffer.position(0);
                            readBuffer.limit((int) (length - offset));
                            packetState = PacketState.packet;
                            needReadChannel = true;
                            continue;
                        }
                    }
                }
            } else if (packetState == PacketState.packet) {
                if (offset == length) {
                    LOGGER.debug("解析报文结束->解析得到含有变长字段的报文");
                    parseEndFunction();
                    return;
                } else if (readBuffer.hasRemaining()) {
                    return;
                } else {
                    parserHandler.handle(this, readBuffer);
                    readBuffer.position(0);
                    continue;
                }
            } else if (packetState == fix) {
                if (readBuffer.hasRemaining()) {
                    return;
                } else {
                    parserHandler.handle(this, readBuffer);
                    needReadChannel = true;
                    toPacket();
                    continue;
                }
            }
        } while (true);
    }

    void parseEndFunction() throws IOException {
        parserHandler.handleEnd(this, readBuffer);
        if (readBuffer != null) {
            myBufferPool.recycle(readBuffer);
        }
        readBuffer = null;
        disableRead();
        /**
         * 不再监控异步读的超时
         */
        NetSystem.getInstance().removeConnection(this);
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

    public enum PacketState {
        header, fix, packet, metaData, write
    }

    // 连接的方向，in表示是客户端连接过来的，out表示自己作为客户端去连接对端Sever
    public enum Direction {
        in, out
    }

}
