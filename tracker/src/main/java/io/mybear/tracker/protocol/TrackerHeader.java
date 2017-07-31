package io.mybear.tracker.protocol;

public class TrackerHeader {
    // body length, not including header
    private long pkgLen;

    // command code
    private byte cmd;

    // status code for response
    private byte status;

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
}
