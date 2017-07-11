package io.mybear.net2.tracker;

import io.mybear.common.ApplicationContext;
import io.mybear.net2.Connection;
import io.mybear.net2.ReactorBufferPool;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TrackerConnection extends Connection {
    public static final int PACKAGE_HEADER_SIZE = 10;
    public static final int PACKAGE_LENGTH_SIZE = 8;
    public static final int PACKAGE_CMD_SIZE = 1;
    public static final int PACKAGE_STATE_SIZE = 1;

    private long readBufferOffset;
    protected TrackerByteBufferArray readBufferArray;

    public TrackerConnection(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void register(Selector selector, ReactorBufferPool myBufferPool) throws IOException {
        super.register(selector, myBufferPool);

        this.readBufferArray = myBufferPool.allocateTrackerByteBufferArray();
        readBufferArray.addNewBuffer();
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
                    readAgain = false;
                    break;
                }
                default: {// readed some bytes

                    if (readBuffer.hasRemaining()) {
                        // 没有可读的机会，等待下次读取
                        readAgain = false;
                    }

                    // 子类负责解析报文
                    readBufferOffset = parseProtocolPakage(this.readBufferArray, readBuffer, readBufferOffset);
                    // 解析后处理
                    ((TrackerNioHandler)handler).handle(this, this.readBufferArray);
                }
            }
        }
        if (got == -1) {
            return;
        }
        if (readBufferArray.getCurPacageLength() > 0) {
            // pkgTotalCount+=readBufferArray
            // pkgTotalSize += length;
            // todo 把完整解析的数据报文拿出来供处理

        }
    }

    protected long parseProtocolPakage(TrackerByteBufferArray bufferArray, ByteBuffer readBuffer,
        long readBufferOffset){
        long offset = readBufferOffset, length = 0;
        int position = readBuffer.position();
        while (offset < position) {
            long curPacakgeLen = bufferArray.getCurPacageLength();
            byte packetCmd = bufferArray.getCurPacageCmd();
            if (curPacakgeLen == 0) {// 还没有解析包头获取到长度
                if (!validateHeader(offset, position)) {
                    copyToNewBuffer(bufferArray, readBuffer, offset);
                    offset = 0;
                    break;
                }
                length = getPacketLength(bufferArray, offset);
                // 读取到了包头和长度
                bufferArray.setCurPacketLength(length);
                // 解析报文类型
//                packetCmd = getPacketCmd(readBuffer, offset);
                bufferArray.setCurPacketCmd(packetCmd);
                offset += PACKAGE_HEADER_SIZE;
            } else {
                // 判断当前的数据包是否完整读取
                long totalPacketSize = bufferArray.calcTotalPacketSize();
                int totalLength = bufferArray.getTotalBytesLength();
                long exceededSize = totalLength - totalPacketSize;
                if (exceededSize >= 0) {// 刚好当前报文结束,或者有空间结余
                    bufferArray.increatePacketIndex();
                    offset = position - exceededSize;
                } else {// 当前数据包还没读完
                    offset = 0;
                    // 立即返回，否则会将当前ByteBuffer当成新报文去读
                    break;
                }

            }

        }
        // 返回下一次读取的标记位置
        return offset;
    }


    protected long parseProtocolPakage(TrackerByteBufferArray bufferArray, long readBufferOffset){
        long offset = readBufferOffset, length = 0;
        long position = bufferArray.getTotalBytesLength();
        while (offset < position) {
            long curPacakgeLen = bufferArray.getCurPacageLength();
            byte packetCmd = bufferArray.getCurPacageCmd();
            if (curPacakgeLen == 0) {// 还没有解析包头获取到长度
                if (!validateHeader(offset, position)) {
//                    copyToNewBuffer(bufferArray, readBuffer, offset);
//                    offset = 0;
                    break;
                }
                length = getPacketLength(bufferArray, offset);
                // 读取到了包头和长度
                bufferArray.setCurPacketLength(length);
                // 解析报文类型
                packetCmd = getPacketCmd(bufferArray, offset);
                bufferArray.setCurPacketCmd(packetCmd);
                offset += PACKAGE_HEADER_SIZE;
            } else {
                // 判断当前的数据包是否完整读取
                long totalPacketSize = bufferArray.calcTotalPacketSize();
                int totalLength = bufferArray.getTotalBytesLength();
                long exceededSize = totalLength - totalPacketSize;
                if (exceededSize >= 0) {// 刚好当前报文结束,或者有空间结余
                    bufferArray.increatePacketIndex();
                    offset = position - exceededSize;
                } else {// 当前数据包还没读完
                    offset = 0;
                    // 立即返回，否则会将当前ByteBuffer当成新报文去读
                    break;
                }

            }

        }
        // 返回下一次读取的标记位置
        return offset;
    }

    @Override
    protected void cleanup() {
        // 清理资源占用
        this.readBufferArray.recycle();
        super.cleanup();
    }

    @Override
    public String getCharset() {
        return ApplicationContext.getInstance().getProperty("charset");
    }

    private boolean validateHeader(long offset, long position){
        return offset + PACKAGE_HEADER_SIZE <= position;
    }

    private void copyToNewBuffer(TrackerByteBufferArray byteBufferArray, ByteBuffer readByteBuffer
        , long offset){

    }

    private long getPacketLength(TrackerByteBufferArray bufferArray, long offset){
        return bufferArray.readLong(offset);
    }

    private byte getPacketCmd(TrackerByteBufferArray bufferArray, long offset){
        return bufferArray.readByte(offset);
    }
}
