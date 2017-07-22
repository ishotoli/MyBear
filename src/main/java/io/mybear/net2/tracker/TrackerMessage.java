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

    public byte[] getRestByteArray() {
        byte[] bytes = new byte[(int) (this.getPkgLen() - this.position)];
        if (this.data.getWritedBlockLst().size() == 1) {
            this.data.getLastByteBuffer().get(bytes);
        } else {
            System.out.println("此函数没写完整");
        }
        return bytes;
    }

    public long readLong() {
        long l = data.readLong(position);
        position += 8;
        return l;
    }

    public int readInt() {
        int i = data.readInt(position);
        position += 4;
        return i;
    }

    public byte readByte() {
        return data.readByte(position++);
    }

    public byte[] getTrackerReplay() {
        byte[] bytes = new byte[10];
        bytes[0] = (byte) ((this.pkgLen >> 56) & 0xff);
        bytes[1] = (byte) ((this.pkgLen >> 48) & 0xff);
        bytes[2] = (byte) ((this.pkgLen >> 40) & 0xff);
        bytes[3] = (byte) ((this.pkgLen >> 32) & 0xff);
        bytes[4] = (byte) ((this.pkgLen >> 24) & 0xff);
        bytes[5] = (byte) ((this.pkgLen >> 16) & 0xff);
        bytes[6] = (byte) ((this.pkgLen >> 8) & 0xff);
        bytes[7] = (byte) (this.pkgLen & 0xff);
        bytes[8] = this.cmd;
        bytes[9] = this.status;
        return bytes;
    }
}
