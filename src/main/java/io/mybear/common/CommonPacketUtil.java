package io.mybear.common;


import io.mybear.net2.ByteBufferArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class CommonPacketUtil {

    public final static int mybear_packetHeaderSize = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonPacketUtil.class);

    private static final boolean validateHeader(int offset, int position) {
        return offset + mybear_packetHeaderSize <= position;
    }

    /**
     * 获取报文长度
     *
     * @param buffer   报文buffer
     * @param offset   buffer解析位置偏移量
     * @param position buffer已读位置偏移量
     * @return 报文长度(Header长度+内容长度)
     */
    private static final long getPacketLength(ByteBuffer buffer, int offset, int position) {
        long len = buffer.getLong(offset) + 10;
        return len;
    }


    /**
     * 将当前解析bytebuffer被截断的部分复制到新的buffer里，新buffer作为bufferArray的队列最新位置
     *
     * @param readBuffer 当前（最后一个bytebuffer）
     * @param offset     上次解析的位置偏移量
     */
    private static void copyToNewBuffer(ByteBufferArray bufferArray, ByteBuffer readBuffer, int offset) {
        ByteBuffer newReadBuffer = bufferArray.addNewBuffer();
        // 复制新的包的数据到下一段里
        readBuffer.position(offset);
        newReadBuffer.put(readBuffer);
        // 为了计算TotalBytesLength，设置当前buffer终点
        readBuffer.position(offset);
    }


    /**
     * 解析出Packet边界,Packet为MSQL格式的报文，其他报文可以类比，
     *
     * @param readBuffer       当前（最后一个bytebuffer）
     * @param readBufferOffset 上次解析的位置偏移量
     * @return 下次解析的位置偏移量
     */
    public static int parsePackets(ByteBufferArray bufferArray, ByteBuffer readBuffer, final int readBufferOffset,
                                   FastTaskInfo con) {
        int offset = readBufferOffset;
        int position = readBuffer.position();
        if (con.cmd == 0) {
            if (readBuffer.position() > 8) {
                con.setCmd(readBuffer.get(8));
                con.setLength(readBuffer.getLong(0));
                con.setState(readBuffer.get(9));
            } else {
                return 0;
            }
        }
        if (con.getLength() > con.getOffset()) {
            con.setOffset(con.getOffset() + readBuffer.position() - readBufferOffset);
        } else {
            offset = readBuffer.position() - readBufferOffset;
        }

        return offset;
    }
}
