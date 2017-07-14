package io.mybear.tracker;

/**
 * Created by jamie on 2017/6/21.
 */
public class TrackerProto {
    public static final int FDFS_PROTO_PKG_LEN_SIZE = 8;

    // 解析fdfs消息头部
    public static final byte FDFS_PROTO_CMD_HEAD = 0;

    // storage heart beat
    public static final byte TRACKER_PROTO_CMD_STORAGE_BEAT = 83;
}
