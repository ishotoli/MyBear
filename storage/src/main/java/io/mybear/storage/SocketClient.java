package io.mybear.storage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class SocketClient {

    public Selector selector;

    public SocketClient(String ip, int port) {
        SocketChannel channel = null;
        try {
            //channel = SocketChannel.open(new InetSocketAddress(ip,port));  
            channel = SocketChannel.open();
            // 设置通道为非阻塞    
            channel.configureBlocking(false);
            // 获得一个通道管理器    
            this.selector = Selector.open();
            // 客户端连接服务器,其实方法执行并没有实现连接   
            channel.connect(new InetSocketAddress(ip, port));
            /**while(!channel.finishConnect()){
             System.out.println("尝试连接....");
             }*/
            // 注册连接事件。    
            channel.register(this.selector, SelectionKey.OP_CONNECT);
        } catch (ClosedChannelException e1) {
            System.out.println("关闭的通道,无法注册到选择器");
            e1.printStackTrace();
        } catch (IOException e2) {
            System.out.println("连接异常!");
            try {
                if (channel != null) channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            e2.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SocketClient sc = null;
        try {
            sc = new SocketClient("localhost", 9999);
            sc.pollSelect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 轮询选择器
     *
     * @throws IOException
     */
    public void pollSelect() throws Exception {
        /* (阻塞)轮询选择器,直到有事件 */
        while (this.selector.select() > 0) {
            /* 获取事件通知列表 */
            Iterator<SelectionKey> ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey selectKey = (SelectionKey) ite.next();
                // 删除已选的key,以防重复处理
                ite.remove();
                process(selectKey);
            }
        }
    }

    /**
     * 处理事件
     *
     * @param selectKey
     */
    public void process(SelectionKey selectKey) throws Exception {
        if (selectKey.isConnectable()) {
            connect(selectKey);
        } else if (selectKey.isReadable()) {
            read(selectKey);
        }
    }

    /**
     * 连接事件
     *
     * @param selectKey
     * @throws Exception
     */
    public void connect(SelectionKey selectKey) throws Exception {
        try {
            SocketChannel channel = (SocketChannel) selectKey.channel();
            /* 如果正在连接,则完成连接 */
            if (channel.isConnectionPending()) {
                /**
                 * connect()方法尚未被调用,调用finishConnect()方法,
                 * 那么将产生NoConnectionPendingException
                 */
                channel.finishConnect();
            }
            /**
             * 在非阻塞模式下调用connect()方法之后,SocketChannel又被切换回了阻塞模式;那么如果
             * 有必要的话，调用线程会阻塞直到连接建立完成,finishConnect()方法接着就会返回true
             * 值。
             */
            /* 设置成非阻塞 */
            channel.configureBlocking(false);
            /* 给服务端发送信息 */
            channel.write(ByteBuffer.wrap(new String("编号001客户端连接成功!").getBytes()));
            /* 注册读事件 */
            channel.register(this.selector, SelectionKey.OP_READ);
        } catch (ClosedChannelException e) {
            throw new IOException("关闭的通道,无法注册到选择器");
        } catch (IOException e) {
            throw new IOException("连接服务或配置失败!");
        }
    }

    /**
     * 读事件
     *
     * @param selectKey
     * @throws Exception
     */
    public void read(SelectionKey selectKey) throws Exception {
        try {
            // 服务器可读通道
            SocketChannel channel = (SocketChannel) selectKey.channel();
            // 创建读取的缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(100);
            channel.read(buffer);
            byte[] data = buffer.array();
            String msg = new String(data).trim();
            System.out.println(msg);
            ByteBuffer outBuffer = ByteBuffer.wrap(msg.getBytes());
            // 将消息回送给服务端
            //channel.write(outBuffer);
        } catch (Exception e) {
            throw new IOException("服务端将通道关闭,无法从通道读入缓冲或将缓冲数据写回通道!");
        }
    }
}  