package io.mybear.common.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Created by jamie on 2017/7/22.
 */
public class ProtocolUtil {
    public static ByteBuffer buildHeader(long len, byte cmd, int state) {
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) (len + 10)).putLong(0, len + 10).put(cmd).put((byte) state);
        byteBuffer.position(10);
        return byteBuffer;
    }

    public static ByteBuffer buildHeader(ByteBuffer byteBuffer, long len, byte cmd, int state) {
        byteBuffer.clear();
        byteBuffer.putLong(0, len + 10).put(8, cmd).put((byte) state);
        byteBuffer.position(10);
        return byteBuffer;
    }

    public static void setIP(ByteBuffer byteBuffer, String ip) {
        ByteBuffer ipBuffer = ByteBuffer.allocate(15);
        ipBuffer.put(ip.getBytes(StandardCharsets.US_ASCII));
        ipBuffer.position(0).limit(15);
        byteBuffer.put(ipBuffer);
    }

    public static String getIP(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[15];
        byteBuffer.get(bytes);
        return new String(bytes);
    }

    public static void setGroupName(ByteBuffer byteBuffer, String name) {
        ByteBuffer groupName = ByteBuffer.allocate(16);
        groupName.put(name.getBytes(StandardCharsets.US_ASCII));
        groupName.position(0).limit(16);
        byteBuffer.put(groupName);
    }

    public static void setStorePathIndex(ByteBuffer byteBuffer, int index) {
        byteBuffer.put((byte) index);
    }


    public static String getGroupName(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[16];
        byteBuffer.get(bytes);
        return new String(bytes);
    }

    public static void setPort(ByteBuffer byteBuffer, int port) {
        byteBuffer.putLong(port);
    }
}
