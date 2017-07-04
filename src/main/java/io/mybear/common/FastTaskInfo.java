package io.mybear.common;

import io.mybear.net2.ByteBufferArray;
import io.mybear.net2.Connection;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by jamie on 2017/6/21.
 */
public class FastTaskInfo extends Connection {



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
        return 0;
    }

    public FileChannel getFileChannel() {
        return fileChannel;
    }

    public void setFileChannel(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }
}
