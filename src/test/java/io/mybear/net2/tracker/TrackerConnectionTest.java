package io.mybear.net2.tracker;

import io.mybear.common.ApplicationContext;
import io.mybear.tracker.TrackerProto;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackerConnectionTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(TrackerConnectionTest.class);

    private TrackerConnection connection;
    private SocketChannel mockChannel;
    private TrackerByteBufferArray mockBufferArray;
    private TrackerNioHandler mockHandler;

    @Override
    protected void setUp() throws Exception {
        new ApplicationContext("tracker.conf");

        mockChannel = EasyMock.createMock(SocketChannel.class);
        connection = new TrackerConnection(mockChannel);

        mockHandler = EasyMock.createMock(TrackerNioHandler.class);
        connection.setHandler(mockHandler);

        mockBufferArray = EasyMock.createMock(TrackerByteBufferArray.class);
        connection.readBufferArray = mockBufferArray;
    }

    public void testAsynRead() throws IOException{
        ByteBuffer byteBuffer = ByteBuffer.allocate(20);
        EasyMock.expect(mockBufferArray.getLastByteBuffer()).andReturn(byteBuffer);
        EasyMock.expect(mockBufferArray.getCurPacageLength()).andReturn(0l);
        EasyMock.replay(mockBufferArray);

        EasyMock.expect(mockChannel.read(byteBuffer)).andReturn(0);
        EasyMock.replay(mockChannel);
        connection.asynRead();
    }

    /**
     * 测试场景
     * [buffer1-start,package1-start]        [package1-end,buffer1-position]         [buffer1-limit]
     */
    public void testAsynRead0() throws IOException{
        long pkgLen = 5;
        ByteBuffer byteBuffer = ByteBuffer.allocate(20);

        EasyMock.expect(mockBufferArray.getLastByteBuffer()).andReturn(byteBuffer);
        EasyMock.expect(mockBufferArray.getTotalBytesLength()).andReturn(pkgLen
                + TrackerMessage.PACKAGE_HEADER_SIZE);
        EasyMock.expect(mockBufferArray.readLong(0)).andReturn(pkgLen);
        EasyMock.expect(mockBufferArray.readByte(8)).andReturn(TrackerProto.TRACKER_PROTO_CMD_STORAGE_BEAT);

        // 第二个报文调用
        EasyMock.expect(mockBufferArray.getTotalBytesLength()).andReturn(pkgLen
            + TrackerMessage.PACKAGE_HEADER_SIZE);

        // 回收空间
        EasyMock.expect(mockBufferArray.compact(15)).andReturn(0l);

        EasyMock.replay(mockBufferArray);

        EasyMock.expect(mockChannel.read(byteBuffer)).andReturn(1);
        EasyMock.replay(mockChannel);

        connection.asynRead();
    }

    /**
     * 测试场景
     * [buffer1-start,package1-start]                  [package1-end,buffer1-position,buffer1-limit]
     */
    public void testAsynRead1() throws IOException{
        long pkgLen = 10;
        ByteBuffer byteBuffer = ByteBuffer.allocate(20);

        EasyMock.expect(mockBufferArray.getLastByteBuffer()).andReturn(byteBuffer);
        EasyMock.expect(mockBufferArray.getTotalBytesLength()).andReturn(pkgLen
            + TrackerMessage.PACKAGE_HEADER_SIZE);
        EasyMock.expect(mockBufferArray.readLong(0)).andReturn(pkgLen);
        EasyMock.expect(mockBufferArray.readByte(8)).andReturn(TrackerProto.TRACKER_PROTO_CMD_STORAGE_BEAT);

        // 第二个报文调用
        EasyMock.expect(mockBufferArray.getTotalBytesLength()).andReturn(pkgLen
            + TrackerMessage.PACKAGE_HEADER_SIZE);

        // 回收空间
        EasyMock.expect(mockBufferArray.compact(20)).andReturn(0l);
        EasyMock.replay(mockBufferArray);

        EasyMock.expect(mockChannel.read(byteBuffer)).andReturn(1);
        EasyMock.replay(mockChannel);

        connection.asynRead();
    }

    /**
     * 测试场景
     * [buffer1-start,package1-start]  [package1-end][package2-start]  [buffer1-position,buffer1-limit]
     * [buffer2-start]                 [package2-end, buffer2-position]              [buffer2-limit]
     */
    public void testAsynRead2() throws IOException{
        long pkgLen1 = 5;
        long pkgLen2 = 10;
        ByteBuffer byteBuffer = ByteBuffer.allocate(20);

        EasyMock.expect(mockBufferArray.getLastByteBuffer()).andReturn(byteBuffer);
        EasyMock.expect(mockBufferArray.getTotalBytesLength()).andReturn(pkgLen1 + pkgLen2
            + TrackerMessage.PACKAGE_HEADER_SIZE * 2);
        EasyMock.expect(mockBufferArray.readLong(0)).andReturn(pkgLen1);
        EasyMock.expect(mockBufferArray.readByte(8)).andReturn(TrackerProto.TRACKER_PROTO_CMD_STORAGE_BEAT);

        // 第二个报文调用
        EasyMock.expect(mockBufferArray.getTotalBytesLength()).andReturn(pkgLen1 + pkgLen2
            + TrackerMessage.PACKAGE_HEADER_SIZE * 2);
        EasyMock.expect(mockBufferArray.readLong(pkgLen1 + TrackerMessage.PACKAGE_HEADER_SIZE))
                .andReturn(pkgLen2);
        EasyMock.expect(mockBufferArray.readByte(pkgLen1 + TrackerMessage.PACKAGE_HEADER_SIZE + 8))
                .andReturn(TrackerProto.TRACKER_PROTO_CMD_STORAGE_BEAT);

        // 第三个报文调用
        EasyMock.expect(mockBufferArray.getTotalBytesLength()).andReturn(pkgLen1 + pkgLen2
            + TrackerMessage.PACKAGE_HEADER_SIZE * 2);

        // 回收空间
        EasyMock.expect(mockBufferArray.compact(pkgLen1 + pkgLen2 + TrackerMessage.PACKAGE_HEADER_SIZE * 2)).andReturn(15l);
        EasyMock.replay(mockBufferArray);

        EasyMock.expect(mockChannel.read(byteBuffer)).andReturn(1);
        EasyMock.replay(mockChannel);

        connection.asynRead();
    }

    /**
     * 测试场景
     * [buffer1-start,package1-start]  [package1-end][package2-start]  [buffer1-position,buffer1-limit]
     * [buffer2-start]     [package2-end, package3-start]     [buffer2-position]     [buffer1-limit]
     */
    public void testAsynRead3() throws IOException{
        long pkgLen1 = 5;
        long pkgLen2 = 10;
        long pkgLen3 = 10;
        ByteBuffer byteBuffer = ByteBuffer.allocate(20);

        long total = pkgLen1 + pkgLen2 + TrackerMessage.PACKAGE_HEADER_SIZE * 2 + 11;
        EasyMock.expect(mockBufferArray.getLastByteBuffer()).andReturn(byteBuffer);
        EasyMock.expect(mockBufferArray.getTotalBytesLength()).andReturn(total);
        EasyMock.expect(mockBufferArray.readLong(0)).andReturn(pkgLen1);
        EasyMock.expect(mockBufferArray.readByte(8)).andReturn(TrackerProto.TRACKER_PROTO_CMD_STORAGE_BEAT);

        // 第二个报文调用
        EasyMock.expect(mockBufferArray.getTotalBytesLength()).andReturn(total);
        EasyMock.expect(mockBufferArray.readLong(pkgLen1 + TrackerMessage.PACKAGE_HEADER_SIZE))
            .andReturn(pkgLen2);
        EasyMock.expect(mockBufferArray.readByte(pkgLen1 + TrackerMessage.PACKAGE_HEADER_SIZE + 8))
            .andReturn(TrackerProto.TRACKER_PROTO_CMD_STORAGE_BEAT);

        // 第三个报文调用
        EasyMock.expect(mockBufferArray.getTotalBytesLength()).andReturn(total);
        EasyMock.expect(mockBufferArray.readLong(pkgLen1 + pkgLen2 + TrackerMessage.PACKAGE_HEADER_SIZE * 2))
            .andReturn(pkgLen3);
        EasyMock.expect(mockBufferArray.readByte(pkgLen1 + pkgLen2 + TrackerMessage.PACKAGE_HEADER_SIZE * 2 + 8))
            .andReturn(TrackerProto.TRACKER_PROTO_CMD_STORAGE_BEAT);

        // 回收空间
        EasyMock.expect(mockBufferArray.compact(pkgLen1 + pkgLen2 + TrackerMessage.PACKAGE_HEADER_SIZE * 2)).andReturn(15l);
        EasyMock.replay(mockBufferArray);

        EasyMock.expect(mockChannel.read(byteBuffer)).andReturn(1);
        EasyMock.replay(mockChannel);

        connection.asynRead();
    }

    /**
     * 测试场景
     * [buffer1-start,package1-start]                               [buffer1-position,buffer1-limit]
     * [buffer2-start]        [package1-end, package2-start]        [buffer2-position,buffer2-limit]
     * [buffer3-start]                [package2-end, buffer3-position]               [buffer3-limit]
     */
    public void testAsynRead4() throws IOException{
        long pkgLen1 = 15;
        long pkgLen2 = 10;
        ByteBuffer byteBuffer = ByteBuffer.allocate(20);

        long total = pkgLen1 + pkgLen2 + TrackerMessage.PACKAGE_HEADER_SIZE * 2;
        EasyMock.expect(mockBufferArray.getLastByteBuffer()).andReturn(byteBuffer);
        EasyMock.expect(mockBufferArray.getTotalBytesLength()).andReturn(total);
        EasyMock.expect(mockBufferArray.readLong(0)).andReturn(pkgLen1);
        EasyMock.expect(mockBufferArray.readByte(8)).andReturn(TrackerProto.TRACKER_PROTO_CMD_STORAGE_BEAT);

        // 第二个报文调用
        EasyMock.expect(mockBufferArray.getTotalBytesLength()).andReturn(total);
        EasyMock.expect(mockBufferArray.readLong(pkgLen1 + TrackerMessage.PACKAGE_HEADER_SIZE))
            .andReturn(pkgLen2);
        EasyMock.expect(mockBufferArray.readByte(pkgLen1 + TrackerMessage.PACKAGE_HEADER_SIZE + 8))
            .andReturn(TrackerProto.TRACKER_PROTO_CMD_STORAGE_BEAT);

        // 第三个报文调用
        EasyMock.expect(mockBufferArray.getTotalBytesLength()).andReturn(pkgLen1 + pkgLen2
            + TrackerMessage.PACKAGE_HEADER_SIZE * 2);

        // 回收空间
        EasyMock.expect(mockBufferArray.compact(pkgLen1 + pkgLen2 + TrackerMessage.PACKAGE_HEADER_SIZE * 2)).andReturn(5l);
        EasyMock.replay(mockBufferArray);

        EasyMock.expect(mockChannel.read(byteBuffer)).andReturn(1);
        EasyMock.replay(mockChannel);

        connection.asynRead();
    }
}
