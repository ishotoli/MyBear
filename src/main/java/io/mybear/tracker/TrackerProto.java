package io.mybear.tracker;

/**
 * Created by jamie on 2017/6/21.
 */
public class TrackerProto {
    public static final int FDFS_PROTO_PKG_LEN_SIZE = 8;

    // 解析fdfs消息头部
    public static final byte FDFS_PROTO_CMD_HEAD = 0;

    // storage启动后连接tracker
    public static final byte TRACKER_PROTO_CMD_STORAGE_JOIN = 81;

    // storage发送的心跳
    public static final byte TRACKER_PROTO_CMD_STORAGE_BEAT = 83;

    // tracker回复
    public static final byte TRACKER_PROTO_CMD_RESP = 100;
}
