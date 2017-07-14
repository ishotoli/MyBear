package io.mybear.net2.tracker;

public class TrackerMessage {
    public static final int PACKAGE_HEADER_SIZE = 10;
    public static final int PACKAGE_LENGTH_SIZE = 8;
    public static final int PACKAGE_CMD_SIZE = 1;
    public static final int PACKAGE_STATE_SIZE = 1;

    // 报文体长度
    private long pkgLen;

    // 命令
    private byte cmd;

    // 响应状态
    private byte status;

    private TrackerByteBufferArray data;

    private long position;

    public long getPkgLen() {
        return pkgLen;
    }

    public void setPkgLen(long pkgLen) {
        this.pkgLen = pkgLen;
    }

    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public TrackerByteBufferArray getData() {
        return data;
    }

    public void setData(TrackerByteBufferArray data) {
        this.data = data;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public long readLong(){
        long l = data.readLong(position);
        position += 8;
        return l;
    }

    public int readInt(){
        int i = data.readInt(position);
        position += 4;
        return i;
    }

    public byte readByte(){
        return data.readByte(position++);
    }
}
