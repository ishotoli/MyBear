package io.mybear.storage.storageNio;

/**
 * 连接ID生成器
 *
 * @author mycat
 */
public class ConnectIdGenerator {

    private static final long MAX_VALUE = Long.MAX_VALUE;
    private static ConnectIdGenerator instance = new ConnectIdGenerator();
    private final Object lock = new Object();
    private long connectId = 0L;

    public static ConnectIdGenerator getINSTNCE() {
        return instance;
    }

    public long getId() {
        synchronized (lock) {
            if (connectId >= MAX_VALUE) {
                connectId = 0L;
            }
            return ++connectId;
        }
    }
}