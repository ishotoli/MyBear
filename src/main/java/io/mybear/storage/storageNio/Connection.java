package io.mybear.storage.storageNio;


import io.mybear.storage.StorageDio;
import io.mybear.storage.parserHandler.ParserHandler;
import io.mybear.storage.parserHandler.UploadFileParserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static io.mybear.storage.storageNio.Connection.PacketState.*;
import static java.nio.channels.SelectionKey.OP_READ;


/**
 * @author cjw
 *         这个类除了writeBuffer可以被其它线程操作外,其它成员变量都不允许呗操作
 */
public abstract class Connection implements ClosableConnection {
    private static final int OP_NOT_READ = ~OP_READ;
    private static final int OP_NOT_WRITE = ~SelectionKey.OP_WRITE;
    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);
    /**
     * 如果不在nio中处理数据,readBuffer此时是null
     */
    public ByteBuffer readBuffer;//除了MappedByteBuffer (线程安全),其它类型ByteBuffer只能在nio里使用
    /**
     * 如果是从dio通知的唤醒nio的,那么flagData肯定有数据
     */
    public volatile Object flagData;
    /**
     * 不可变引用
     */
    protected String host;
    protected int port;
    protected int localPort;
    protected long id;
    protected SocketChannel channel;
    protected long startupTime;
    @SuppressWarnings("rawtypes")
    protected NIOHandler handler;
    protected boolean isClosed;
    protected boolean isSocketClosed;
    protected long lastReadTime;
    protected long lastWriteTime;
    protected int netInBytes;
    protected int netOutBytes;
    protected int pkgTotalSize;
    protected int pkgTotalCount;
    private Direction direction = Direction.in;
    private SelectionKey processKey;
    private ReactorBufferPool myBufferPool;
    /**
     * 可变对象成员
     */
    private long length = 0; //data length
    private long offset = 0;
    private int metaDataLength;
    private byte cmd;
    private ParserHandler parserHandler;
    private PacketState packetState;
    private State state = State.connecting;
    private int readBufferOffset;
    private long lastLargeMessageTime;
    private long idleTimeout;
    private long lastPerfCollectTime;
    private int maxPacketSize;
    private int packetHeaderSize;


    public Connection(SocketChannel channel) {
        this.channel = channel;
        this.isClosed = false;
        this.startupTime = TimeUtil.currentTimeMillis();
        this.lastReadTime = startupTime;
        this.lastWriteTime = startupTime;
        this.lastPerfCollectTime = startupTime;
        this.packetState = header;
        this.offset = 0;
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


    public boolean isConnected() {
        return (this.state == State.connected);
    }

    public byte getCmd() {
        return cmd;
    }

    public long getLength() {
        return length;
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
        if (readBuffer != null) {
            if (!(readBuffer instanceof MappedByteBuffer)) {
                myBufferPool.recycle(readBuffer);
            }
        }
        Object writeBuffer = this.flagData;
        if (writeBuffer != null) {
            if (writeBuffer instanceof ByteBuffer) {
                myBufferPool.recycle((ByteBuffer) writeBuffer);
            } else if (writeBuffer instanceof ByteBufferArray) {
                ((ByteBufferArray) writeBuffer).recycle();
            }
            this.flagData = null;
        }
    }


    @SuppressWarnings("unchecked")
    public void register(Selector selector, ReactorBufferPool myBufferPool) throws IOException {
        this.myBufferPool = myBufferPool;
        processKey = channel.register(selector, OP_READ, this);
        NetSystem.getInstance().addConnection(this);
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
        this.flagData = bufferArray;
        this.disableRead();
        this.enableWrite(true);
    }

    public void write(ByteBuffer buffer) {
        buffer.flip();
        this.flagData = buffer;
        this.enableWriteDisableRead(true);
    }

    boolean writeDownloadByteBuffer(ByteBuffer data) throws IOException {
        ByteBuffer writeBuffer = (ByteBuffer) data;
        if (writeBuffer.hasRemaining()) {
            channel.write(writeBuffer);
            lastWriteTime = TimeUtil.currentTimeMillis();
        }
        if (!writeBuffer.hasRemaining()) {
            this.myBufferPool.recycle(writeBuffer);
            if (!(packetState == downloadHead)) {
                toHead();
                return true;
            } else {
                this.flagData = Boolean.FALSE;
                this.packetState = downloadData;
                StorageDio.queuePush(this);
                return false;
            }
        }
        return true;
    }

    /**
     * @return
     * @throws IOException
     */
    private boolean write() throws IOException {
        Object flagData = this.flagData;//被dio线程设置writeBuffer后被唤醒
        if (packetState == downloadHead) {
            return writeDownloadByteBuffer((ByteBuffer) flagData);

        }
        if (packetState == downloadData) {
            if (flagData == Boolean.TRUE) {
                // LOGGER.debug("flagData= Boolean.False;发送下载数据,需要通知dio,dio每处理一次通知完毕后,把flagData == Boolean.False,直至结束");
                StorageDio.queuePush(this);
                return false;
            }
            return false;
        } else {
            LOGGER.debug("处理普通命令,文件上传的数据发送,之后toHead");
            if (flagData instanceof ByteBuffer) {
                ByteBuffer writeBuffer = (ByteBuffer) flagData;
                if (writeBuffer.hasRemaining()) {
                    channel.write(writeBuffer);
                    lastWriteTime = TimeUtil.currentTimeMillis();
                }
                if (!writeBuffer.hasRemaining()) {
                    this.myBufferPool.recycle(writeBuffer);
                    this.flagData = null;
                    toHead();
                    return true;
                }
            }
            int written = 0;
            ByteBufferArray arry = (ByteBufferArray) flagData;
            for (ByteBuffer buffer : arry.getWritedBlockLst()) {
                if (buffer != null) {
                    while (buffer.hasRemaining()) {
                        written = channel.write(buffer);
                        if (written > 0) {
                            lastWriteTime = TimeUtil.currentTimeMillis();
                            netOutBytes += written;
                            //io.mybear.net2.NetSystem.getInstance().addNetOutBytes(written);
                        } else {
                            break;
                        }
                    }

                }
            }
            if (arry.getLastByteBuffer().hasRemaining()) {
                return false;
            } else {
                flagData = null;
                arry.recycle();
                if (!(packetState == downloadHead)) {
                    toHead();
                }
                return true;
            }
        }
    }

    public void disableWrite() {
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() & OP_NOT_WRITE);
        } catch (Exception e) {
            LOGGER.warn("can't disable write " + e + " con " + this);
        }

    }

    public void disableWriteEnableRead() {
        try {
            SelectionKey key = this.processKey;
            key.interestOps((key.interestOps() & OP_NOT_WRITE) | OP_READ);
        } catch (Exception e) {
            LOGGER.warn("can't disable write then enable read " + e + " con " + this);
        }

    }

    public void enableWriteDisableRead(boolean wakeup) {
        boolean needWakeup = false;
        try {
            SelectionKey key = this.processKey;
            key.interestOps((key.interestOps() & OP_NOT_READ) | SelectionKey.OP_WRITE);
            needWakeup = true;
        } catch (Exception e) {
            LOGGER.warn("can't enable write then disable read" + e);

        }
        if (needWakeup && wakeup) {
            processKey.selector().wakeup();
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
        Object data = flagData;
        if (data != null) {
            if (data != Boolean.FALSE && data != Boolean.TRUE) {
                if (flagData instanceof ByteBuffer) {
                    myBufferPool.recycle((ByteBuffer) flagData);
                } else if (flagData instanceof ByteBufferArray) {
                    ((ByteBufferArray) flagData).recycle();
                } else {
                    //mapped
                }
            }

        }
        flagData = null;
        enableRead();
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

    private boolean isInReactorThread() {
        return (Thread.currentThread().getName().contains("-RW"));
    }

    public void toUpload() throws IOException {
        assert isInReactorThread();
        this.packetState = Connection.PacketState.upload;
        if (readBuffer.capacity() <= 1024) {
            myBufferPool.recycle(readBuffer);
            RandomAccessFile memoryMappedFile = new RandomAccessFile("d:/sss" + "0", "rw");
            LOGGER.debug("需要池化Mapped");
            readBuffer = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 96 * 1024);
        }
        //this.flagData=Boolean.FALSE;
    }

    public void toDownload() {
        assert isInReactorThread();
        this.packetState = PacketState.downloadHead;
    }

    private void process() throws IOException {
        if (readBuffer != null && !isClosed) {//异步读函数,没有readBuffer意味着不需要处理上传数据,消除重复读
            if (packetState == upload) {
                if (flagData == Boolean.TRUE) {
                    LOGGER.debug("flagData= Boolean.False;处理上传数据,需要通知dio,dio每处理一次通知完毕后,把flagData == Boolean.True");
                    return;
                } else {
                    LOGGER.debug("正在上传处理,不用通知");
                    return;
                }
            } else {
                LOGGER.debug("处理命令,之后,把readBuffer=null");
                return;
            }
        } else if (packetState == downloadHead) {
            if (flagData == Boolean.TRUE) {
                LOGGER.debug("flagData= Boolean.False;发送下载数据,需要通知dio,dio每处理一次通知完毕后,把flagData == Boolean.True,直至结束");
                return;
            } else {
                if (flagData instanceof ByteBuffer || flagData instanceof ByteBufferArray) {
                    LOGGER.debug("发送下载数据头,之后,把flagData == Boolean.TRUE");
                    return;
                }
            }
        } else {
            LOGGER.debug("处理普通命令,文件上传的数据发送,之后toHead");
            return;
        }
    }

    protected void read0() throws IOException {
        int got = 0;
        boolean needReadChannel = false;//避免多次读取channel却是返回0
        got = channel.read(readBuffer);
        if (got == 0) return;
        if (got == -1) {
            closeSocket();
            return;
        }
        lastReadTime = TimeUtil.currentTimeMillis();
        offset += got;
        LOGGER.debug("解析报文开始->读取通道");
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

    /**
     * 异步读取数据,only nio thread call
     *
     * @throws IOException
     */
    protected void asynRead() throws IOException {
        if (readBuffer != null && !isClosed) {//异步读函数,没有readBuffer意味着不需要处理上传数据,消除重复读
            if (packetState == upload) {
                //LOGGER.debug("数据来了");
                if (flagData == Boolean.TRUE) {
                    // LOGGER.debug("flagData= Boolean.False;处理上传数据,需要通知dio,
                    // 先 flagData = Boolean.FALSE;这一步很关键,再放入队列,
                    // dio每处理一次通知完毕后,把flagData == Boolean.True");
                    int got;
                    this.readBuffer.clear();//每次进入这里都要清除信息
                    assert (this.readBuffer instanceof MappedByteBuffer);
                    got = this.getChannel().read(this.readBuffer);
                    if (got == -1) {
                        close("socket被关闭");
                    }
                    if (!this.readBuffer.hasRemaining()) {
//                        LOGGER.debug("readBuffer满了");
                    } else {
//                        LOGGER.debug("readBuffer没满");
                    }
                    //this.offset+=got;
                    flagData = Boolean.FALSE;
                    UploadFileParserHandler.upload(this, channel);
                    return;
                } else {
                    //LOGGER.debug("正在上传处理,不用通知");
                    return;
                }
            } else {
                read0();
                LOGGER.debug("处理命令,之后,把readBuffer=null");
                return;
            }
        }
    }

    void parseEndFunction() throws IOException {
        parserHandler.handleEnd(this, readBuffer);
        if (packetState != upload) {
            if (readBuffer != null) {
                myBufferPool.recycle(readBuffer);
            }
            readBuffer = null;
            disableRead();
        } else {
            UploadFileParserHandler.upload(this, channel);
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

    public NIOHandler getHandler() {
        return handler;
    }

    public void setHandler(NIOHandler handler) {
        this.handler = handler;
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
        header, fix, packet, metaData, upload, downloadHead, downloadData
    }

    // 连接的方向，in表示是客户端连接过来的，out表示自己作为客户端去连接对端Sever
    public enum Direction {
        in, out
    }

}
