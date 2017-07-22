package io.mybear.net2.tracker;

import io.mybear.common.ApplicationContext;
import io.mybear.net2.Connection;
import io.mybear.net2.ReactorBufferPool;
import io.mybear.tracker.types.FdfsGroupInfo;
import io.mybear.tracker.types.FdfsStorageDetail;
import io.mybear.tracker.types.FdfsStorageStat;
import io.mybear.tracker.types.TrackerClientInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TrackerConnection extends Connection {

    protected TrackerByteBufferArray readBufferArray;
    protected TrackerByteBufferArray writeBufferArray;
    private long readBufferOffset;
    private TrackerClientInfo clientInfo;

    private TrackerMessage message = new TrackerMessage();

    public TrackerConnection(SocketChannel channel) {
        super(channel);

        // for test
        TrackerClientInfo clientInfo = new TrackerClientInfo();
        clientInfo.setGroup(new FdfsGroupInfo());
        clientInfo.setStorage(new FdfsStorageDetail());
        clientInfo.getStorage().setState(new FdfsStorageStat());
        this.clientInfo = clientInfo;
        // for test
    }

    @Override
    public void register(Selector selector, ReactorBufferPool myBufferPool) throws IOException {
        super.register(selector, myBufferPool);

        this.readBufferArray = myBufferPool.allocateTrackerByteBufferArray();
        readBufferArray.addNewBuffer();

        this.writeBufferArray = myBufferPool.allocateTrackerByteBufferArray();
        writeBufferArray.addNewBuffer();
    }

    @Override
    protected void asynRead() throws IOException {
        if (this.isClosed) {
            return;
        }

        boolean readAgain = true;
        int got = 0;
        while (readAgain) {
            ByteBuffer readBuffer = readBufferArray.getLastByteBuffer();
            got = channel.read(readBuffer);
            switch (got) {
                case 0: {
                    // 如果空间不够了，继续分配空间读取
                    if (readBuffer.remaining() == 0) {
                        readBufferArray.addNewBuffer();
                    } else {
                        readAgain = false;
                    }
                    break;
                }
                case -1: {
//                    readAgain = false;
//                    break;
                    return;
                }
                default: {// readed some bytes

                    if (readBuffer.hasRemaining()) {
                        // 没有可读的机会，等待下次读取
                        readAgain = false;
                    }

                    // 循环处理读到的所有报文
                    long newOffset = 0;
                    do{
                        if(newOffset > 0){
                            readBufferOffset = newOffset;
                        }

                        message.setData(readBufferArray);
                        message.setPosition(readBufferOffset);
                        newOffset = parseProtocolPakage();

                        // 解析后处理
                        if(newOffset > readBufferOffset) {
                            ((TrackerNioHandler) handler).handle(this, this.message);
                        }
                    }while(newOffset > readBufferOffset);
                    readBufferOffset = newOffset;

                    // 报文处理完成后，腾出readBufferArray空间。
                    readBufferOffset = readBufferArray.compact(readBufferOffset);
                }
            }
        }
    }

    public void write(byte[] bytes) {
        writeBufferArray.write(bytes);
        this.enableWrite(true);
    }

    public TrackerByteBufferArray getWriteBufferArray() {
        if (writeBufferArray == null) {
            writeBufferArray = getMyBufferPool().allocateTrackerByteBufferArray();
            writeBufferArray.addNewBuffer();
        }
        return writeBufferArray;
    }

    @Override
    public void doWriteQueue() {
        try {
            boolean noMoreData;

            for (;;) {
                int written = 0;
                if (writeBufferArray == null) {
                    break;
                }
                for (ByteBuffer buffer : writeBufferArray.getWritedBlockLst()) {
                    buffer.flip();

                    // ByteBuffer buffer = writeBuffer;
                    if (buffer != null) {
                        while (buffer.hasRemaining()) {
                            written = channel.write(buffer);
                            if (written > 0) {
//                                netOutBytes += written;
//                                NetSystem.getInstance().addNetOutBytes(written);
                            } else {
                                break;
                            }
                        }

                    }
                }

                if (writeBufferArray.getLastByteBuffer().hasRemaining()) {
                    noMoreData = false;
                } else {
                    writeBufferArray.recycle();
                    writeBufferArray = null;
                    noMoreData = true;
                }

                if (noMoreData) {
                    if ((processKey.isValid()
                            && (processKey.interestOps() & SelectionKey.OP_WRITE) != 0)) {
                        disableWrite();
                    }
                } else {
                    if ((processKey.isValid()
                            && (processKey.interestOps() & SelectionKey.OP_WRITE) == 0)) {
                        enableWrite(false);
                    }
                }
            }
        } catch (IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("caught err:", e);
            }
            close("err:" + e);
        }
    }

    protected long parseProtocolPakage(){
        long length, limit = message.getData().getTotalBytesLength();
        byte packetCmd;

        // 检查报文头部是否读取完整
        if (message.getPosition() + TrackerMessage.PACKAGE_HEADER_SIZE > limit) {
            return readBufferOffset;
        }
        length = message.readLong();

        // 读取到了包头和长度
        // 解析报文类型
        packetCmd = message.readByte();
        // message指针设置在头部结束的位置，等待读取数据
        message.setPosition(message.getPosition() + TrackerMessage.PACKAGE_STATE_SIZE);

        // 检查报文体是否读取完整
        if(message.getPosition() + length > limit){
            return readBufferOffset;
        }

        // 返回下一次读取的标记位置
        message.setPkgLen(length);
        message.setCmd(packetCmd);

        // 返回本次报文结束的位置
        return message.getPosition() + length;
    }

    @Override
    protected void cleanup() {
        // 清理资源占用
        this.readBufferArray.recycle();
        clientInfo = null;
        message = null;
        super.cleanup();
    }

    @Override
    public String getCharset() {
        return ApplicationContext.getInstance().getProperty("charset");
    }

    public TrackerClientInfo getClientInfo() {
        return clientInfo;
    }

    public TrackerMessage getMessage() {
        return message;
    }
}
