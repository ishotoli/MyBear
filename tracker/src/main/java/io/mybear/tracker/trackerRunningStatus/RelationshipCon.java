package io.mybear.tracker.trackerRunningStatus;

import java.nio.ByteBuffer;

/**
 * Created by jamie on 2017/8/10.
 */
public interface RelationshipCon {
    public void sendData(ByteBuffer buffer);

    public void recData(ByteBuffer buffer);
}
