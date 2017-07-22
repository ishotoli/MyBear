package io.mybear.net2.tracker;

import io.mybear.net2.ReactorBufferPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * ByteBuffer数组，扩展的时候从BufferPool里动态获取可用的Buffer 非线程安全类
 * @author wuzhih
 *
 * copy from Mycat-NIO io.mycat.net2.ByteBufferArray
 * 修改为适用于fdfs报文格式
 * @author 258662572@qq.com
 * @date 2017-7-11
 */
public class TrackerByteBufferArray {
    private static final Logger logger = LoggerFactory.getLogger(TrackerByteBufferArray.class);

    /** packageLengths数组容量 */
    private static final int CAPACITY = 4;

    private final ReactorBufferPool bufferPool;

    private final ArrayList<ByteBuffer> writedBlockLst = new ArrayList<>(4);
    private final int blockCapasity;
    // 此Array中包括的消息报文长度（byte 字节的长度而不是writedBlockLst中的次序）
    private long[] packetLengths = new long[CAPACITY];
    private byte[] packetCmds = new byte[CAPACITY];
    private int curPacageIndex = 0;

    public TrackerByteBufferArray(ReactorBufferPool bufferPool) {
        super();
        this.bufferPool = bufferPool;
        blockCapasity = bufferPool.getSharedBufferPool().getChunkSize();
    }

    /**
     * 返回当前所有bytebuffer里的字节数长度总和
     * 
     * @return
     */
    public long getTotalBytesLength() {
        long totalLen = 0;
        int endBlock = writedBlockLst.size();
        for (int i = 0; i < endBlock; i++) {
            ByteBuffer bytBuf = writedBlockLst.get(i);
            totalLen += bytBuf.position();
        }
        return totalLen;
    }

    /**
     * 得到队列中最后一个ByteBuffer，也即当前可以写入的位置，如果队列为空，则返回NULL
     * 
     * @return
     */
    public ByteBuffer getLastByteBuffer() {
        return writedBlockLst.get(writedBlockLst.size() - 1);

    }

    /**
     * 申请一个ByteBuffer，并且放入队列，并且返回此ByteBuffer
     * 
     * @return
     */
    public ByteBuffer addNewBuffer() {
        ByteBuffer buf = this.bufferPool.allocateByteBuffer();
        writedBlockLst.add(buf);
        return buf;
    }

    public ArrayList<ByteBuffer> getWritedBlockLst() {
        return writedBlockLst;
    }

    /**
     * 将一个数组写入队列中
     * 
     * @param src
     */
    public ByteBuffer write(byte[] src) {
        ByteBuffer curWritingBlock = null;
        if (this.writedBlockLst.isEmpty()) {
            curWritingBlock = this.addNewBuffer();
        } else {
            curWritingBlock = getLastByteBuffer();
        }
        int offset = 0;
        int remains = src.length;
        while (remains > 0) {
            int writeable = curWritingBlock.remaining();
            if (writeable >= remains) {
                // can write whole srce
                curWritingBlock.put(src, offset, remains);
                break;
            } else {
                // can write partly
                curWritingBlock.put(src, offset, writeable);
                offset += writeable;
                remains -= writeable;
                curWritingBlock = addNewBuffer();
                continue;
            }

        }
        return curWritingBlock;
    }

    public long getCurPacageLength() {
        return this.packetLengths[this.curPacageIndex];
    }

    /**
     * 回收此对象，用完需要在合适的地方释放，否則產生內存泄露問題
     */
    public void recycle() {
        bufferPool.recycle(this);
    }

    public void clear() {
        curPacageIndex = 0;
        this.writedBlockLst.clear();

        for (int i = 0; i < packetLengths.length; i++) {
            packetLengths[i] = 0;
        }

        for (int i = 0; i < packetCmds.length; i++) {
            packetCmds[i] = 0;
        }

    }


    public byte readByte(long offset){
        final int blockCapasity = this.blockCapasity;
        int blockIndex = (int)(offset / blockCapasity);
        int blockOffset = (int)(offset % blockCapasity);
//        logger.debug("read byte: {}", writedBlockLst.get(blockIndex).get(blockOffset));
        return writedBlockLst.get(blockIndex).get(blockOffset);
    }

    public int readInt(long offset){
        return (readByte(offset++) & 0xFF) << 24
            | (readByte(offset++) & 0xFF) << 16
            | (readByte(offset++) & 0xFF) << 8
            | (readByte(offset++) & 0xFF);
    }

    public long readLong(long offset){
        return (readByte(offset++) & 0xFFL) << 56
            | (readByte(offset++) & 0xFFL) << 48
            | (readByte(offset++) & 0xFFL) << 40
            | (readByte(offset++) & 0xFFL) << 32
            | (readByte(offset++) & 0xFFL) << 24
            | (readByte(offset++) & 0xFFL) << 16
            | (readByte(offset++) & 0xFFL) << 8
            | (readByte(offset++) & 0xFFL);
    }

    public long compact(long offset){
        int blockSize = writedBlockLst.size();
        if(blockSize == 1){
            ByteBuffer buf = getLastByteBuffer();
            if(offset >= 0 && offset <= buf.position()){
                buf.limit(buf.position());
                buf.position((int)offset);
                buf.compact();
            }
            return 0;
        }else if(blockSize > 0){
            while(offset > this.blockCapasity){
                ByteBuffer buf = writedBlockLst.remove(0);
                bufferPool.recycle(buf);
                offset = offset - this.blockCapasity;
            }
        }
        return offset;
    }
}
