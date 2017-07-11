package io.mybear.net2.tracker;

import io.mybear.common.ApplicationContext;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackerConnectionTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(TrackerConnectionTest.class);

    private TrackerConnection connection;
    private SocketChannel mockChannel;
    private TrackerByteBufferArray mockBufferArray;

    @Override
    protected void setUp() throws Exception {
        new ApplicationContext("tracker.conf");

        mockChannel = EasyMock.createMock(SocketChannel.class);
        connection = new TrackerConnection(mockChannel);
        connection.setHandler(new TrackerNioHandler());

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
    public void testParseProtocolPakage0(){
        ByteBuffer readBuffer = ByteBuffer.allocate(20);

        long readBufferOffset = 0l;
        connection.parseProtocolPakage(mockBufferArray, readBuffer, readBufferOffset);
    }

    /**
     * 测试场景
     * [buffer1-start,package1-start]                  [package1-end,buffer1-position,buffer1-limit]
     */
    public void testParseProtocolPakage1(){
        ByteBuffer readBuffer = ByteBuffer.allocate(20);

        long readBufferOffset = 0l;
        connection.parseProtocolPakage(mockBufferArray, readBuffer, readBufferOffset);
    }

    /**
     * 测试场景
     * [buffer1-start,package1-start]  [package1-end][package2-start]  [buffer1-position,buffer1-limit]
     * [buffer2-start]                 [package2-end, buffer2-position]              [buffer2-limit]
     */
    public void testParseProtocolPakage2(){
        ByteBuffer readBuffer = ByteBuffer.allocate(20);

        long readBufferOffset = 0l;
        connection.parseProtocolPakage(mockBufferArray, readBuffer, readBufferOffset);
    }

    /**
     * 测试场景
     * [buffer1-start,package1-start]  [package1-end][package2-start]  [buffer1-position,buffer1-limit]
     * [buffer2-start]     [package2-end, package3-start]     [buffer2-position]     [buffer1-limit]
     */
    public void testParseProtocolPakage3(){
        ByteBuffer readBuffer = ByteBuffer.allocate(20);

        long readBufferOffset = 0l;
        connection.parseProtocolPakage(mockBufferArray, readBuffer, readBufferOffset);
    }

    /**
     * 测试场景
     * [buffer1-start,package1-start]                               [buffer1-position,buffer1-limit]
     * [buffer2-start]        [package1-end, package2-start]        [buffer2-position,buffer2-limit]
     * [buffer3-start]                [package2-end, buffer3-position]               [buffer3-limit]
     */
    public void testParseProtocolPakage4(){
        ByteBuffer readBuffer = ByteBuffer.allocate(20);

        long readBufferOffset = 0l;
        connection.parseProtocolPakage(mockBufferArray, readBuffer, readBufferOffset);
    }
}
