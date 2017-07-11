package io.mybear.net2.tracker;

import io.mybear.common.ApplicationContext;
import io.mybear.net2.ReactorBufferPool;
import io.mybear.net2.SharedBufferPool;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void testGetLastByteBuffer(){
        ByteBuffer byteBuffer = byteBufferArray.addNewBuffer();
        logger.debug("byteBuffer: {}", byteBuffer);
        IntStream.range(0, 10).forEach(i->byteBuffer.put((byte)i));
        logger.debug("byteBuffer: {}", byteBuffer);
    }

    @Test
    public void testReadByte(){
        ByteBuffer buf1 = byteBufferArray.addNewBuffer();
        logger.debug("buf1: {}", buf1);
        IntStream.range(0, 20).forEach(i->buf1.put((byte)i));
        logger.debug("after put: {}", buf1);

        ByteBuffer buf2 = byteBufferArray.addNewBuffer();
        logger.debug("buf2: {}", buf2);
        IntStream.range(20, 30).forEach(i->buf2.put((byte)i));
        logger.debug("after put: {}", buf2);
        byte b = byteBufferArray.readByte(0);

        Assert.assertEquals(b, (byte)0);

        b = byteBufferArray.readByte(19);
        Assert.assertEquals(b, (byte)19);

        b = byteBufferArray.readByte(20);
        Assert.assertEquals(b, (byte)20);

        b = byteBufferArray.readByte(29);
        Assert.assertEquals(b, (byte)29);

        b = byteBufferArray.readByte(39);
        Assert.assertEquals(b, (byte)0);
    }

    @Test
    public void testReadLong(){
        ByteBuffer buf1 = byteBufferArray.addNewBuffer();
        ByteBuffer buf2 = byteBufferArray.addNewBuffer();

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
        logger.debug("buf1: {}", buf1.array());
        logger.debug("buf2: {}", buf2.array());

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


    private void putLong(ByteBuffer buf, long l, int offset){
        byte[] bytes = long2byte(l);
        IntStream.range(0, 8).filter((i)->buf.hasRemaining() && i >= offset).forEach(i->buf.put(bytes[i]));
    }

    /**
     * long to bytes (big endian)
     *
     * @param num 待转换数字
     * @return bytes
     */
    private static byte[] long2byte(long num){
        byte[] result = new byte[8];
        result[0] = (byte)((num >> 56) & 0xFFL);
        result[1] = (byte)((num >> 48) & 0xFFL);
        result[2] = (byte)((num >> 40) & 0xFFL);
        result[3] = (byte)((num >> 32) & 0xFFL);
        result[4] = (byte)((num >> 24) & 0xFFL);
        result[5] = (byte)((num >> 16) & 0xFFL);
        result[6] = (byte)((num >> 8) & 0xFFL);
        result[7] = (byte)(num & 0xFFL);

        return result;
    }
}
