package io.mybear.tracker;

import io.mybear.common.ApplicationContext;
import io.mybear.net2.ConnectionFactory;
import io.mybear.net2.ExecutorUtil;
import io.mybear.net2.NIOAcceptor;
import io.mybear.net2.NIOConnector;
import io.mybear.net2.NIOReactorPool;
import io.mybear.net2.NameableExecutor;
import io.mybear.net2.NamebleScheduledExecutor;
import io.mybear.net2.NetSystem;
import io.mybear.net2.SharedBufferPool;
import io.mybear.net2.SystemConfig;
import io.mybear.net2.tracker.TrackerConnectionFactory;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jamie on 2017/6/21.
 */
public class FdfsTrackerd {
    private static final Logger logger = LoggerFactory.getLogger(FdfsTrackerd.class);

    public static void main(String[] args) throws IOException{
        ApplicationContext context = new ApplicationContext("tracker.conf");

        // Business Executor ，用来执行那些耗时的任务
        NameableExecutor businessExecutor = ExecutorUtil.create("BusinessExecutor", 10);
        // 定时器Executor，用来执行定时任务
        NamebleScheduledExecutor timerExecutor = ExecutorUtil.createSheduledExecute("Timer", 5);

        SharedBufferPool sharedPool = new SharedBufferPool(1024 * 1024 * 100, 1024);
        new NetSystem(sharedPool, businessExecutor, timerExecutor);
        // Reactor pool
        NIOReactorPool reactorPool = new NIOReactorPool("Reactor Pool", 5, sharedPool);
        NIOConnector connector = new NIOConnector("NIOConnector", reactorPool);
        connector.start();
        NetSystem.getInstance().setConnector(connector);
        NetSystem.getInstance().setNetConfig(new SystemConfig());

        ConnectionFactory connectionFactory = new TrackerConnectionFactory();
        String ip = context.getProperty("bind_addr");
        int port = context.getIntValue("port", TrackerGlobal.DEFAULT_SERVER_PORT);
        NIOAcceptor server = new NIOAcceptor("Tracker Server", ip, port, connectionFactory, reactorPool);
        server.start();

        //
        TrackerGlobal.gUpTime.setTime(System.currentTimeMillis());

        // server started
        logger.info(server.getName() + " is started and listening on " + server.getPort());
    }
}
