package io.mybear.tracker;

import io.mybear.common.context.ApplicationContext;
import io.mybear.tracker.trackerNio.ReactorBufferPool;
import io.mybear.tracker.trackerNio.SharedBufferPool;
import io.mybear.tracker.trackerNio.TrackerByteBufferArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.stream.IntStream;

public class TrackerByteBufferArrayTest {
    private static final Logger logger = LoggerFactory.getLogger(TrackerByteBufferArrayTest.class);

    private SharedBufferPool shearedBufferPool;
    private ReactorBufferPool reactorBufferPool;
    private TrackerByteBufferArray byteBufferArray;

    @Before
    public void setUp() throws Exception {
        new ApplicationContext("tracker.conf");
        shearedBufferPool = new SharedBufferPool(20 * 100, 20);
        reactorBufferPool = new ReactorBufferPool(shearedBufferPool, Thread.currentThread()
                , 1000);
        byteBufferArray = reactorBufferPool.allocateTrackerByteBufferArray();
    }

    @Test
    public void testGetLastByteBuffer() {
        ByteBuffer byteBuffer = byteBufferArray.addNewBuffer();
        logger.debug("byteBuffer: {}", byteBuffer);
        IntStream.range(0, 10).forEach(i -> byteBuffer.put((byte) i));
        logger.debug("byteBuffer: {}", byteBuffer);
    }

    @Test
    public void testReadByte() {
        ByteBuffer buf1 = byteBufferArray.addNewBuffer();
        logger.debug("buf1: {}", buf1);
        IntStream.range(0, 20).forEach(i -> buf1.put((byte) i));
        logger.debug("after put: {}", buf1);

        ByteBuffer buf2 = byteBufferArray.addNewBuffer();
        logger.debug("buf2: {}", buf2);
        IntStream.range(20, 30).forEach(i -> buf2.put((byte) i));
        logger.debug("after put: {}", buf2);
        byte b = byteBufferArray.readByte(0);

        Assert.assertEquals(b, (byte) 0);

        b = byteBufferArray.readByte(19);
        Assert.assertEquals(b, (byte) 19);

        b = byteBufferArray.readByte(20);
        Assert.assertEquals(b, (byte) 20);

        b = byteBufferArray.readByte(29);
        Assert.assertEquals(b, (byte) 29);

        b = byteBufferArray.readByte(39);
        Assert.assertEquals(b, (byte) 0);
    }

    @Test
    public void testReadLong() {
        ByteBuffer buf1 = byteBufferArray.addNewBuffer();
        logger.debug("byteBuffer: {}", buf1);
        ByteBuffer buf2 = byteBufferArray.addNewBuffer();
        logger.debug("byteBuffer: {}", buf2);

        long l0 = 54674891436546l;
        putLong(buf1, l0, 0);
        long l1 = 8794654561345484l;
        putLong(buf1, l1, 0);
        long l2 = 343435435437878l;
        putLong(buf1, l2, 0);
        putLong(buf2, l2, 4);
        long l3 = 1231365467897543l;
        putLong(buf2, l3, 0);
        long l4 = 7835135494344573l;
        putLong(buf2, l4, 0);
//        logger.debug("buf1: {}", buf1.array());
//        logger.debug("buf2: {}", buf2.array());

        long l = byteBufferArray.readLong(0);
        logger.debug("l0: {}", Long.toBinaryString(l0));
        logger.debug("l: {}", Long.toBinaryString(l));
        Assert.assertEquals(l0, l);
        l = byteBufferArray.readLong(8);
        Assert.assertEquals(l1, l);
        l = byteBufferArray.readLong(16);
        Assert.assertEquals(l2, l);
        l = byteBufferArray.readLong(24);
        Assert.assertEquals(l3, l);
        l = byteBufferArray.readLong(32);
        Assert.assertEquals(l4, l);
    }

    @Test
    public void testReadInt() {
        ByteBuffer buf1 = byteBufferArray.addNewBuffer();
        ByteBuffer buf2 = byteBufferArray.addNewBuffer();

        // 占用两格测试一个int存储在两格buf中的情况
        buf1.put((byte) 0);
        buf1.put((byte) 0);

        int i0 = 546748545;
        putInt(buf1, i0, 0);
        int i1 = 321546547;
        putInt(buf1, i1, 0);
        int i2 = 456735415;
        putInt(buf1, i2, 0);
        int i3 = 321546547;
        putInt(buf1, i3, 0);
        int i4 = 354313121;
        putInt(buf1, i4, 0);
        putInt(buf2, i4, 2);
        int i5 = 687845451;
        putInt(buf2, i5, 0);
        int i6 = 321535465;
        putInt(buf2, i6, 0);
        int i7 = 545481515;
        putInt(buf2, i7, 0);
        int i8 = 984436511;
        putInt(buf2, i8, 0);
//        logger.debug("buf1: {}", buf1.array());
//        logger.debug("buf2: {}", buf2.array());

        int i = byteBufferArray.readInt(2);
        Assert.assertEquals(i0, i);
        i = byteBufferArray.readInt(6);
        Assert.assertEquals(i1, i);
        i = byteBufferArray.readInt(10);
        Assert.assertEquals(i2, i);
        i = byteBufferArray.readInt(14);
        Assert.assertEquals(i3, i);
        i = byteBufferArray.readInt(18);
        logger.debug("i4: {}", Integer.toBinaryString(i4));
        logger.debug("i: {}", Integer.toBinaryString(i));
        Assert.assertEquals(i4, i);
        i = byteBufferArray.readInt(22);
        Assert.assertEquals(i5, i);
        i = byteBufferArray.readInt(26);
        Assert.assertEquals(i6, i);
        i = byteBufferArray.readInt(30);
        Assert.assertEquals(i7, i);
        i = byteBufferArray.readInt(34);
        Assert.assertEquals(i8, i);
    }

    private void putLong(ByteBuffer buf, long l, int offset) {
        byte[] bytes = NumberUtil.long2byte(l);
        IntStream.range(0, 8).filter((i) -> buf.hasRemaining() && i >= offset).forEach(i -> buf.put(bytes[i]));
    }

    private void putInt(ByteBuffer buf, int num, int offset) {
        byte[] bytes = NumberUtil.int2byte(num);
        IntStream.range(0, 4).filter((i) -> buf.hasRemaining() && i >= offset).forEach(i -> buf.put(bytes[i]));
    }
}
