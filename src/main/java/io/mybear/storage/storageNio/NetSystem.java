package io.mybear.storage.storageNio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.NetworkChannel;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 存放当前所有连接的信息，包括客户端和服务端等，以及Network部分所使用共用对象
 *
 * @author wuzhih
 */
public class NetSystem {
    public static final int RUNNING = 0;
    public static final int SHUTING_DOWN = -1;
    private static final Logger LOGGER = LoggerFactory.getLogger(NetSystem.class);
    // private static final Logger LOGGER = Logger.getLogger("NetSystem");
    private static NetSystem INSTANCE;
    private final SharedBufferPool bufferPool;
    // 用来执行那些耗时的任务
    private final NameableExecutor executor;
    // 用来执行定时任务
    private final NamebleScheduledExecutor timer;
    private final ConcurrentMap<Long, Connection> allConnections;
    private long netInBytes;
    private long netOutBytes;
    private SystemConfig netConfig;
    private NIOConnector connector;

    public NetSystem(SharedBufferPool bufferPool, NameableExecutor executor, NamebleScheduledExecutor timer)
            throws IOException {
        this.bufferPool = bufferPool;
        this.executor = executor;
        this.timer = timer;
        this.allConnections = new ConcurrentHashMap<Long, Connection>();
        INSTANCE = this;
    }

    public static NetSystem getInstance() {
        return INSTANCE;
    }

    public SharedBufferPool getBufferPool() {
        return bufferPool;
    }

    public NIOConnector getConnector() {
        return connector;
    }

    public void setConnector(NIOConnector connector) {
        this.connector = connector;
    }

    public int getWriteQueueSize() {
        int total = 0;
        for (Connection con : allConnections.values()) {
            total += con.getWriteQueue().size();
        }

        return total;

    }

    public SystemConfig getNetConfig() {
        return netConfig;
    }

    public void setNetConfig(SystemConfig netConfig) {
        this.netConfig = netConfig;
    }

    public NameableExecutor getExecutor() {
        return executor;
    }

    public NamebleScheduledExecutor getTimer() {
        return timer;
    }

    public long getNetInBytes() {
        return netInBytes;
    }

    public void addNetInBytes(long bytes) {
        netInBytes += bytes;
    }

    public long getNetOutBytes() {
        return netOutBytes;
    }

    public void addNetOutBytes(long bytes) {
        netOutBytes += bytes;
    }

    /**
     * 添加一个连接到系统中被监控
     *
     * @param c
     */
    public void addConnection(Connection c) {
        allConnections.put(c.getId(), c);
    }

    public ConcurrentMap<Long, Connection> getAllConnectios() {
        return allConnections;
    }

    /**
     * 定时执行该方法，回收部分资源。
     */
    public void checkConnections() {
        Iterator<Entry<Long, Connection>> it = allConnections.entrySet().iterator();
        while (it.hasNext()) {
            Connection c = it.next().getValue();

            // 删除空连接
            if (c == null) {
                it.remove();
                continue;
            }

            // 清理已关闭连接，否则空闲检查。
            if (c.isClosed()) {
                c.cleanup();
                it.remove();
            } else {
                c.idleCheck();
            }
        }
    }

    /**
     * 定时执行该方法，回收部分资源。
     */
    public void firstReadIdleCheck() {
        Iterator<Entry<Long, Connection>> it = allConnections.entrySet().iterator();
        while (it.hasNext()) {
            Connection c = it.next().getValue();

            // 删除空连接
            if (c == null) {
                it.remove();
                continue;
            }

            // 清理已关闭连接，否则空闲检查。
            if (c.isClosed()) {
                c.cleanup();
                it.remove();
            } else {
                //这里其实存在线程安全问题,即c.getLastWriteTime(),c.getLastReadTime()可能不是最新的
                // 但是对于一问一答的C/S架构,在超时时间(默认30s)内不能把上传文件之前的信息(通常不到1024byte)或者metadata读出一点,那应该是超时了
                //读完了数据,要求allConnections马上删除此链接
                if (TimeUtil.currentTimeMillis() - Math.max(c.getLastWriteTime(), c.getLastReadTime()) > c.getIdleTimeout()) {
                    LOGGER.info(toString() + " idle timeout");
                    c.close(" idle ");
                }
            }
        }
    }

    /**
     * 离开nio读的时候,利用此方法
     *
     * @param con
     */
    public void removeConnection(Connection con) {
        this.allConnections.remove(con.getId());
    }

    public void setSocketParams(Connection con, boolean isFrontChannel) throws IOException {
        int sorcvbuf = 0;
        int sosndbuf = 0;
        int soNoDelay = 0;
        if (isFrontChannel) {
            sorcvbuf = netConfig.getFrontsocketsorcvbuf();
            sosndbuf = netConfig.getFrontsocketsosndbuf();
            soNoDelay = netConfig.getFrontSocketNoDelay();
        } else {
            sorcvbuf = netConfig.getBacksocketsorcvbuf();
            sosndbuf = netConfig.getBacksocketsosndbuf();
            soNoDelay = netConfig.getBackSocketNoDelay();
        }
        NetworkChannel channel = con.getChannel();
        channel.setOption(StandardSocketOptions.SO_RCVBUF, sorcvbuf);
        channel.setOption(StandardSocketOptions.SO_SNDBUF, sosndbuf);
        channel.setOption(StandardSocketOptions.TCP_NODELAY, soNoDelay == 1);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);

        con.setMaxPacketSize(netConfig.getMaxPacketSize());
        con.setPacketHeaderSize(netConfig.getPacketHeaderSize());

    }

}
