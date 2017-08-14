package io.mybear.tracker.trackerNio;

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
        int size = (int) (this.getPkgLen() - this.position);
        byte[] bytes = new byte[(size)];
        int j = 0;
        for (long i = this.position; i < size; i++, j++) {
            bytes[j] = data.readByte(i);
        }
        return bytes;
    }

    public String readIP() {
        long size = position + 15;
        byte[] ip = new byte[15];
        int j = 0;
        long i = position;
        for (; i < size; i++, j++) {
            ip[j] = data.readByte(i);
        }
        position = i;
        return new String(ip);
    }

    public int readPort() {
        int res = data.readInt(position);
        position += 4;
        return res;
    }

    public String readGroupname() {
        byte[] groupname = new byte[16];
        for (int i = 0; i < 16; i++) {
            groupname[i] = readByte();
        }
        return new String(groupname);
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
