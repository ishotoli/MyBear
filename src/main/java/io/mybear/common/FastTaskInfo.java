package io.mybear.common;

import io.mybear.net2.Connection;

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

}
