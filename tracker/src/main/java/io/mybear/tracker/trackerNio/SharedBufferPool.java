package io.mybear.tracker.trackerNio;

import io.mybear.common.context.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.LinkedTransferQueue;

/**
 * 线程安全的共享ByteBufferPool，可以用于在ReactorPool之间调剂
 *
 * @author wuzhih
 */
public class SharedBufferPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(SharedBufferPool.class);
    private final LinkedTransferQueue<ByteBuffer> freeBuffers = new LinkedTransferQueue<ByteBuffer>();
    private final long bufferSize;
    private final int chunkSize;
    private final boolean debug = true;
    private volatile int newCreated;
    private long capactiy;

    public SharedBufferPool(long bufferSize, int chunkSize) {
        long size = bufferSize / chunkSize;
        size = (bufferSize % chunkSize == 0) ? size : size + 1;
        this.capactiy = size;
        this.bufferSize = bufferSize;
        this.chunkSize = chunkSize;
    }

    /**
     * 申请一个ByteBuffer 内存，用完需要在合适的时间释放，
     *
     * @return
     * @see io.mybear.net2.SharedBufferPool#recycle
     */
    public ByteBuffer allocate() {
        ByteBuffer node = freeBuffers.poll();
        if (node == null) {
            newCreated++;
            node = this.createDirectBuffer(chunkSize);
        } else {
            node.clear();
        }

        return node;
    }

    public void recycle(ArrayList<ByteBuffer> buffers) {
        int count = buffers.size();
        for (int i = 0; i < count; i++) {
            recycle(buffers.get(i));
        }
    }

    /**
     * 回收ByteBuffer
     *
     * @param buffer
     */
    public void recycle(ByteBuffer buffer) {
        if (debug) {
            if (!checkValidBuffer(buffer)) {
                return;
            }
        }
        System.out.println("recycle buffer " + Thread.currentThread().getName());
        this.freeBuffers.add(buffer);
    }

    private boolean checkValidBuffer(ByteBuffer buffer) {
        // 拒绝回收null和容量大于chunkSize的缓存
        if (buffer == null || !buffer.isDirect()) {
            return false;
        } else if (buffer.capacity() != chunkSize) {
            LOGGER.warn("cant' recycle  a buffer not equals my pool chunksize " + chunkSize + "  he is "
                    + buffer.capacity());
            throw new RuntimeException("bad size");

            // return false;
        }
        return true;
    }

    public LinkedTransferQueue<ByteBuffer> getFreeBuffers() {
        return freeBuffers;
    }

    public long getBufferSize() {
        return bufferSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getNewCreated() {
        return newCreated;
    }

    public long getCapactiy() {
        return capactiy;
    }

    private ByteBuffer createDirectBuffer(int size) {
        if (ApplicationContext.debug) {
            return ByteBuffer.allocate(size);
        }

        // for performance
        return ByteBuffer.allocateDirect(size);
    }

}
