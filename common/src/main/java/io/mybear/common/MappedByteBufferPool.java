package io.mybear.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * @author zhanyingda 2017/08/12
 */
public class MappedByteBufferPool {
    public static final Logger log = LoggerFactory.getLogger(MappedByteBufferPool.class);
    private static MappedByteBuffer largeBuffer;
    //回收ByteBuffer的List
    private static List<ByteBuffer> buffList;
    //要节分的ByteBufer大小
    private static int largeBufferSize;
    //每个分片的大小
    private static int sliceBufferSize;
    //用户连接数
    private static int connectCount;

    public static void init(int largeBufferSize, int connectCount) throws IOException {
        RandomAccessFile memoryMappedFile = new RandomAccessFile("d:/largeBufferPool.txt", "rw");
        largeBuffer = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, largeBufferSize);
        memoryMappedFile.close();
        buffList = new LinkedList<ByteBuffer>();
        largeBufferSize = largeBufferSize;
        connectCount = connectCount;
        sliceBufferSize = largeBufferSize / connectCount;
    }

    public static int getSliceBufferSize() {
        return sliceBufferSize;
    }

    /**
     * 分配ByteBuffer
     *
     * @return
     */
    public static synchronized ByteBuffer allocateBuffer() {
        ByteBuffer buffer;
        if (!buffList.isEmpty()) {
            buffer = buffList.get(0);
            buffer.clear();
            buffList.remove(0);
        } else {
            buffer = getSliceBuffer();
        }
        if (log.isDebugEnabled()) {
            log.info("分配了 " + (buffer == null ? 0 : buffer.capacity()) + "的空间");
        }
        return buffer;

    }

    /**
     * 分片
     *
     * @return
     */
    private static ByteBuffer getSliceBuffer() {
        if (largeBuffer.position() + sliceBufferSize > largeBuffer.capacity()) {
            if (log.isDebugEnabled()) {
                log.info("没有更多的可分配空间了");
            }
            return null;
        }

        largeBuffer.limit(largeBuffer.position() + sliceBufferSize);
        ByteBuffer buffer = largeBuffer.slice();

        largeBuffer.position(largeBuffer.limit());
        largeBuffer.limit(largeBuffer.capacity());
        return buffer;
    }

    /**
     * 回收ByteBuffer
     *
     * @param buffer
     */
    public static synchronized void recycleBuffer(ByteBuffer buffer) {
        buffList.add(buffer);
        if (log.isDebugEnabled()) {
            log.info("回收了 " + buffer.capacity() + "的空间");
        }
    }


}
