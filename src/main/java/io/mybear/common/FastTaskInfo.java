package io.mybear.common;

import io.mybear.net2.ByteBufferArray;
import io.mybear.net2.Connection;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by jamie on 2017/6/21.
 */
public class FastTaskInfo extends Connection {
    public ByteBufferArray data; //buffer for write or recv

    //header
    public long length; //data length
    public byte cmd;
    public byte state;

    public long offset;
    public long req_count; //request count
    public TaskFinishCallback finish_callback;
//    struct fast_task_info *next;

    public FastTaskInfo(SocketChannel channel) {
        super(channel);
    }

    @Override
    public String getCharset() {
        return "ASCII";
    }

    /**
     * 处理head
     *
     * @param readBufferArray
     * @param readBuffer
     * @param readBufferOffset
     * @return
     */
    @Override
    protected int parseProtocolPakage(ByteBufferArray readBufferArray, ByteBuffer readBuffer, int readBufferOffset) {
        return CommonPacketUtil.parsePackets(readBufferArray, readBuffer, readBufferOffset, this);
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public long getReq_count() {
        return req_count;
    }

    public void setReq_count(long req_count) {
        this.req_count = req_count;
    }

    public TaskFinishCallback getFinish_callback() {
        return finish_callback;
    }

    public void setFinish_callback(TaskFinishCallback finish_callback) {
        this.finish_callback = finish_callback;
    }

}
