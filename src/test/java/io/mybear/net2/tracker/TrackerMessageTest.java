package io.mybear.net2.tracker;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TrackerMessageTest {
    private TrackerMessage message;
    private TrackerByteBufferArray mockTrackerByteBufferArray;

    @Before
    public void setup(){
        message = new TrackerMessage();
        mockTrackerByteBufferArray = EasyMock.createMock(TrackerByteBufferArray.class);
        message.setData(mockTrackerByteBufferArray);
    }

    @Test
    public void testRead(){
        EasyMock.expect(mockTrackerByteBufferArray.readLong(0)).andReturn(0l);
        EasyMock.expect(mockTrackerByteBufferArray.readInt(8)).andReturn(0);
        EasyMock.expect(mockTrackerByteBufferArray.readByte(12)).andReturn((byte)0);
        EasyMock.replay(mockTrackerByteBufferArray);

        message.setPosition(0);
        Assert.assertEquals(0l, message.readLong());
        Assert.assertEquals(8l, message.getPosition());

        Assert.assertEquals(0, message.readInt());
        Assert.assertEquals(12l, message.getPosition());

        Assert.assertEquals(0, message.readByte());
        Assert.assertEquals(13l, message.getPosition());
    }

}
