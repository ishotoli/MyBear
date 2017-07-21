package io.mybear.net2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import static io.mybear.net2.Connection.TaskType.*;
import static org.csource.fastdfs.ProtoCommon.*;


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
    public long length; //data length
    public long offset = -10;
    public int metaDataLength;
    public byte cmd;
    public long uploadFileSize;
    public FileChannel uploadChannel;
    public long downloadPosition;
    public FileChannel downloadChannel;
    public TaskType taskType = TaskType.Query;
    public PacketState packetState;
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
    private State state = State.connecting;
    private Direction direction = Direction.in;
    protected SelectionKey processKey;
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
        this.packetState = PacketState.header;
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
        offset = -10;
        taskType = TaskType.Query;
        readBuffer.limit(10).position(0);
        this.handler.onConnected(this);

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

    void toHead(ByteBuffer byteBuffer) throws IOException {
        if (downloadChannel != null) {
            if (downloadPosition == downloadChannel.size()) {
                downloadChannel.close();
                downloadChannel = null;
                downloadPosition = 0;
                switch (taskType) {
                    case DownloadWithUpload:
                        this.uploadChannel.close();
                        this.uploadFileSize = 0;
                        taskType = Upload;
                        return;
                    case DownloadWithQuery:
                        taskType = Query;
                    case Upload:
                    case Download:
                        taskType = Query;
                        break;
                    default:
                        throw new IOException("暂不支持");
                }
            } else {
                switch (taskType) {
                    case DownloadWithUpload:
                    case DownloadWithQuery:
                        taskType = Download;
                        return;
                    default:
                        throw new IOException("暂不支持");
                }
            }
        }
        if (this.uploadChannel != null && uploadChannel.position() == uploadFileSize) {
            uploadChannel.close();
            uploadChannel = null;
            uploadFileSize = 0;
        }
        length = 0;
        offset = -10;
        taskType = TaskType.Query;
        byteBuffer.limit(10).position(0);
        this.packetState = PacketState.header;
    }

    void toDownloadWithHead(ByteBuffer byteBuffer) throws IOException {
        byteBuffer.limit(10).position(0);
        this.packetState = PacketState.header;
    }

//    void toFix(ByteBuffer byteBuffer, int length) {
//        byteBuffer.clear();
//        byteBuffer.limit(length);
//        this.packetState = PacketState.fix;
//    }

    void toPacket(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        byteBuffer.limit(byteBuffer.capacity());
        this.packetState = PacketState.packet;
    }

    void toMetaData(ByteBuffer byteBuffer, int metaDatalength) {
        this.metaDataLength = metaDatalength;
        byteBuffer.clear();
        byteBuffer.limit(metaDatalength);
        this.packetState = PacketState.metaData;
    }

    void toFix(ByteBuffer byteBuffer) {
        long rest = (this.length - this.offset) % byteBuffer.capacity();
        byteBuffer.clear();
        byteBuffer.limit((int) rest);
        this.packetState = PacketState.fix;
    }

    long getMetaDataLength(int cmd, long totalLen) {
        switch (cmd) {
            case FDFS_PROTO_CMD_QUIT:
                return 0;
            case TRACKER_PROTO_CMD_SERVER_LIST_GROUP:
                return 0;
            case TRACKER_PROTO_CMD_SERVER_LIST_STORAGE:
                return totalLen;
            case TRACKER_PROTO_CMD_SERVER_DELETE_STORAGE:
                return totalLen;
            case TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE:
                return totalLen;
            case TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ONE:
                return totalLen;
            case TRACKER_PROTO_CMD_SERVICE_QUERY_UPDATE:
                return totalLen;
            case TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ONE:
                return totalLen;
            case TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ALL:
                return totalLen;
            case TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ALL:
                return totalLen;
            case TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ALL:
                return totalLen;
            case TRACKER_PROTO_CMD_RESP:
                return 0;
            case FDFS_PROTO_CMD_ACTIVE_TEST:
                return 0;
            case STORAGE_PROTO_CMD_UPLOAD_FILE:
                return 15;
            case STORAGE_PROTO_CMD_DELETE_FILE:
                return totalLen;
            case STORAGE_PROTO_CMD_SET_METADATA:
                return totalLen;
            case STORAGE_PROTO_CMD_DOWNLOAD_FILE:
                return totalLen;
            case STORAGE_PROTO_CMD_GET_METADATA:
                return totalLen;
            case STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE:
                return totalLen;
            case STORAGE_PROTO_CMD_QUERY_FILE_INFO:
                return totalLen;
            case STORAGE_PROTO_CMD_UPLOAD_APPENDER_FILE:
                return 15;//待修改
            case STORAGE_PROTO_CMD_APPEND_FILE:
                return 15;
            case STORAGE_PROTO_CMD_MODIFY_FILE:
                return 15;
            case STORAGE_PROTO_CMD_TRUNCATE_FILE:
                return 16;
            default:
                return -1;
        }
    }


    /**
     * 异步读取数据,only nio thread call
     *
     * @throws IOException
     */
    protected void asynRead() throws IOException {
        if (this.isClosed) {
            return;
        }
        switch (taskType) {
            case DownloadWithQuery:
            case Query:
                break;
            case DownloadWithUpload:
            case Upload: {
                long uploadPosition = uploadChannel.position();
                this.uploadChannel.transferFrom(channel, uploadPosition, uploadFileSize - uploadPosition);
                if (uploadChannel.position() != uploadFileSize) {
                    return;
                } else {
                    this.handler.handleEnd(this, readBuffer);
                    toHead(readBuffer);
                    return;
                }
            }
            case Download: {
                doWriteQueue();
                toDownloadWithHead(readBuffer);
                break;
            }
            default:
                break;
        }
        int got = 0;
        got = channel.read(readBuffer);
        offset += got;
        while (true) {
            switch (packetState) {
                case header: {
                    if (readBuffer.position() < 10) {
                        return;
                    } else {
                        this.length = readBuffer.getLong(0);
                        this.cmd = readBuffer.get(8);
                        int state = readBuffer.get(9);
                        toMetaData(readBuffer, (int) getMetaDataLength(cmd, length));
                        continue;
                    }
                }
                case metaData: {
                    if (readBuffer.hasRemaining()) {
                        return;
                    } else {
                        this.handler.handleMetaData(this, readBuffer);
                        if (this.uploadFileSize == 0) {
                            toFix(readBuffer);
                        }
                        TaskType taskType = this.taskType;
                        if (taskType == TaskType.Download) {
                            return;
                        }
                        continue;
                    }
                }
                case fix: {
                    if (readBuffer.hasRemaining()) {
                        return;
                    } else {
                        this.handler.handle(this, readBuffer);
                        toPacket(readBuffer);
                        continue;
                    }
                }
                case packet:
                default: {
                    if (offset == length) {
                        this.handler.handleEnd(this, readBuffer);
                        toHead(readBuffer);
                        continue;
                    } else if (readBuffer.hasRemaining()) {
                        return;
                    } else {
                        this.handler.handle(this, readBuffer);
                        readBuffer.clear();
                        continue;
                    }
                }
            }

        }
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
        header, fix, packet, metaData
    }

    public enum TaskType {
        Query, Upload, Download, DownloadWithQuery, DownloadWithUpload, DownloadWithDownload
    }

    // 连接的方向，in表示是客户端连接过来的，out表示自己作为客户端去连接对端Sever
    public enum Direction {
        in, out
    }

}
