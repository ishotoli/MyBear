package io.mybear.storage.storageNio;


import io.mybear.storage.parserHandler.ParserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import static io.mybear.storage.storageNio.Connection.PacketState.*;
import static io.mybear.storage.storageNio.Connection.TaskType.*;
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
    private final WriteQueue writeQueue = new WriteQueue(1024 * 1024 * 16);
    private final ReentrantLock writeQueueLock = new ReentrantLock();
    public long length; //data length
    public long offset = 0;
    public int metaDataLength;
    public byte cmd;
    public long uploadFileSize;
    public boolean needInit = false;
    public ParserHandler parserHandler;
    public FileChannel uploadChannel;
    public long downloadPosition;
    public FileChannel downloadChannel;
    public TaskType taskType = TaskType.Query;
    public PacketState packetState;
    public ByteBuffer readBuffer;
    public ByteBuffer writeBuffer;
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
        processKey = channel.register(selector, OP_READ, this);
        NetSystem.getInstance().addConnection(this);
        //this.readBufferArray = myBufferPool.allocateByteBuffer()
        readBuffer = this.myBufferPool.allocateByteBuffer();
        offset = 0;
        readBuffer.limit(10).position(0);
    }

    public void doWriteQueue0() throws IOException {
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

    public void doWriteQueue() {
        if (readBuffer != null) {
            try {
                if (readBuffer.hasRemaining()) {
                    channel.write(writeBuffer);
                }
                if (!readBuffer.hasRemaining()) {
                    needInit = true;
                    this.disableWrite();
                    this.enableRead();
                    toHead(readBuffer);
                }
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if (taskType != TaskType.Download) {
                doWriteQueue0();
            } else {
                if (downloadPosition == 0) {
                    while (!write0()) ;
                }
                lastWriteTime = TimeUtil.currentTimeMillis();
                long size = downloadChannel.size();
                downloadPosition += this.downloadChannel.transferTo(downloadPosition, downloadChannel.size() - downloadPosition, channel);
                if (downloadPosition == size) {
                    this.handler.handleEnd(this, readBuffer);
                    toHead(readBuffer);
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

    void toHead(ByteBuffer byteBuffer) {
        length = 0;
        metaDataLength = 0;
        offset = 0;
        try {
            byteBuffer.limit(10).position(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.packetState = header;
    }

    void toDownloadWithHead(ByteBuffer byteBuffer) throws IOException {
        byteBuffer.limit(10).position(0);
        this.packetState = header;
    }

//    void toFix(ByteBuffer byteBuffer, int length) {
//        byteBuffer.clear();
//        byteBuffer.limit(length);
//        this.packetState = PacketState.fix;
//    }

    void toPacket(ByteBuffer byteBuffer) {
        byteBuffer.position(0);
        byteBuffer.limit(byteBuffer.capacity());
        this.packetState = packet;
    }

    void toMetaData(ByteBuffer byteBuffer, int metaDatalength) {
        byteBuffer.position(0);
        try {
            byteBuffer.limit(metaDatalength);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.packetState = metaData;
    }

    void toFix(ByteBuffer byteBuffer) {
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
        if (this.isClosed) {
            return;
        }
        if (Connection.PacketState.write == this.packetState) {
            if (!this.needInit) {
                return;
            } else {
                this.needInit = false;
                this.toHead(this.readBuffer);
            }
        }
        int got = 0;
        boolean needReadChannel = false;//避免多次读取channel却是返回0
        got = channel.read(readBuffer);
        LOGGER.debug("解析报文开始->读取通道");
        if (got == -1) {
            closeSocket();
            return;
        }
        if (got == 0) return;
        lastReadTime = TimeUtil.currentTimeMillis();
        offset += got;
        do {
            if (needReadChannel) {
                LOGGER.info("读取通道");
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
                        toFix(readBuffer);
                        needReadChannel = true;
                        continue;
                    }
                    needReadChannel = true;
                    toMetaData(readBuffer, mataLen);
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
                        needReadChannel = true;
                        toFix(readBuffer);
                        continue;
                    }
                }
            } else if (packetState == fix) {
                if (readBuffer.hasRemaining()) {
                    return;
                } else {
                    parserHandler.handle(this, readBuffer);
                    needReadChannel = true;
                    toPacket(readBuffer);
                    continue;
                }
            } else if (offset == length) {
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
        } while (true);
    }

    void parseEndFunction() {
        if (readBuffer.position() == 0 || (readBuffer.limit() == readBuffer.position())) {
            LOGGER.debug("解析报文结束->精准取出报文信息");
        } else {
            LOGGER.debug("解析报文结束->解析报文失败,下一次解析会出错");
        }
        packetState = write;
        disableRead();
        if (!parserHandler.handleEnd(this, readBuffer)) {
            /**
             * 如果返回false,则马上向通道写入数据,写入不了等下次响应通知
             */
            try {
                SocketChannel socketChannel = this.getChannel();
                int g = socketChannel.write(readBuffer);
                if (g == 10) {
                    toHead(readBuffer);
                    return;
                } else if (g == -1) {
                    closeSocket();
                    return;
                }
                this.enableWrite(true);
            } catch (IOException e) {
                e.printStackTrace();
                closeSocket();
            }
        }
        /**
         * 不再监控异步读的超时
         */
        NetSystem.getInstance().removeConnection(this);
    }


    public void setTaskType(TaskType type) throws Exception {
        TaskType taskType = this.taskType;
        if (taskType == Download) {
            switch (type) {
                case Download:
                    this.taskType = DownloadWithDownload;
                    throw new Exception("暂不支持");
                case Upload:
                    this.taskType = DownloadWithUpload;
                    return;
                case Query:
                    this.taskType = DownloadWithQuery;
                    return;
                default:
                    break;
            }
        } else {
            this.taskType = type;
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

    public enum PacketState {
        header, fix, packet, metaData, write
    }

    public enum TaskType {
        Query, Upload, Download, DownloadWithQuery, DownloadWithUpload, DownloadWithDownload
    }

    // 连接的方向，in表示是客户端连接过来的，out表示自己作为客户端去连接对端Sever
    public enum Direction {
        in, out
    }

}
