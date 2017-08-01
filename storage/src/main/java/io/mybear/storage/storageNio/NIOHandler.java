package io.mybear.storage.storageNio;

import java.io.IOException;

/**
 * NIOHandler是无状态的，多个连接共享一个，用于处理连接的事件，每个方法需要不阻塞，尽快返回结果
 *
 * @author wuzh
 */
public interface NIOHandler<T extends Connection> {

    /**
     * 连接建立成功的通知事件
     *
     * @param con 当前连接
     */
    void onConnected(T con) throws IOException;

    /**
     * 连接失败
     *
     * @param con 失败的连接
     * @param e   连接异常
     */
    void onConnectFailed(T con, Throwable e);

    /**
     * 连接关闭通知
     *
     * @param con
     * @throws IOException
     */
    void onClosed(T con, String reason);


}