package io.mybear.storage.trackerClient;

import io.mybear.common.utils.BasicTypeConversionUtil;
import io.mybear.common.utils.ProtocolUtil;
import io.mybear.storage.storageNio.ByteBufferArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by jamie on 2017/8/14.
 */
public class ConnectionInfo {
    public static boolean g_use_connection_pool = false;
    Socket socket;
    InputStream in;
    OutputStream out;
    SocketChannel channel;
    byte[] buff = new byte[10];
    ByteBufferArray pool;

    private ConnectionInfo() {

    }

    public static ConnectionInfo connect(String ip, int post, int connect_timeout, ByteBufferArray pool) throws IOException {
        Socket socket = new Socket(ip, post);
        socket.setSoTimeout(connect_timeout);
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.socket = socket;
        connectionInfo.in = socket.getInputStream();
        connectionInfo.out = socket.getOutputStream();
        connectionInfo.channel = socket.getChannel();
        connectionInfo.pool = pool;
        return connectionInfo;
    }

    /**
     * @param buffer
     * @throws IOException
     */
    public void write(ByteBuffer buffer) throws IOException {
        buffer.flip();
        int n = channel.write(buffer);
        if (n == -1) {
            throw new IOException("连接被关闭");
        }
        if (!buffer.hasRemaining()) {
            return;
        } else {
            int limit = buffer.limit();
            for (int i = buffer.position(); i < limit; i++) {
                out.write(buffer.get(i));
            }
            out.flush();
        }
    }

    /**
     * @param
     * @throws IOException
     */
    public byte[] recvHeader() throws IOException {
        int n = in.read(buff, 0, 10);
        if (n != 10) {
            throw new IOException("连接被关闭");
        }
        return buff;
    }

    /**
     * @return
     * @throws IOException
     */
    public void recvResponse(ByteBufferArray bufferArray) throws IOException {
        byte[] head = recvHeader();
        int len = (int) BasicTypeConversionUtil.buff2long(head);
        for (int i = 0; i < len; i++) {
            int n = in.read();
            if (n == -1) {
                throw new IOException("连接被关闭");
            }
            bufferArray.write((byte) (n));
        }
    }

    /**
     * @param cmd
     * @return
     * @throws IOException
     */
    int dealNoBodyCmd(final byte cmd) throws IOException {
        ByteBuffer buffer = pool.getLastByteBuffer();
        if (buffer != null) {
            buffer = pool.addNewBuffer();
        }
        ProtocolUtil.buildHeader(buffer, 0, cmd, 0);
        write(buffer);
        buffer.clear();
        recvHeader();
        return 0;
    }

    /**
     * @param port
     * @param cmd
     * @return
     */
    int fdfsDealNoBodyCmdEx(String ip_addr, final int port, final int cmd) {
//
//        ConnectionInfo * conn;
//        ConnectionInfo server_info;
//        int result;
//
//        strcpy(server_info.ip_addr, ip_addr);
//        server_info.port = port;
//        server_info.sock = -1;
//        connect(ip_addr,)
//        if ((conn = tracker_connect_server( & server_info, &result)) ==NULL)
//        {
//            return result;
//        }
//
//        result = fdfs_deal_no_body_cmd(conn, cmd);
//        tracker_disconnect_server_ex(conn, result != 0);
//        return result;
        return 0;
    }

    void close() {
        try {
            in.close();
            out.close();
            channel.close();
            socket.close();
            buff = null;
            pool.recycle();
            pool = null;
        } catch (Exception e) {
        }
    }
//    void tracker_disconnect_server_ex(ConnectionInfo *conn, \
//            const bool bForceClose)
//    {
//        if (g_use_connection_pool)
//        {
//            conn_pool_close_connection_ex(&g_connection_pool, \
//            conn, bForceClose);
//        }
//        else
//        {
//            conn_pool_disconnect_server(conn);
//        }
//    }


}
