package io.mybear.tracker;

import io.mybear.common.ApplicationContext;
import io.mybear.net2.*;
import io.mybear.net2.tracker.TrackerConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by jamie on 2017/6/21.
 */
public class FdfsTrackerd {
    private static final Logger logger = LoggerFactory.getLogger(FdfsTrackerd.class);

    public static void main(String[] args) throws IOException{
        ApplicationContext context = new ApplicationContext("tracker.conf");

        // Business Executor ，用来执行那些耗时的任务
        NameableExecutor businessExecutor = ExecutorUtil.create("BusinessExecutor", 1);
        // 定时器Executor，用来执行定时任务
        NamebleScheduledExecutor timerExecutor = ExecutorUtil.createSheduledExecute("Timer", 1);

        SharedBufferPool sharedPool = new SharedBufferPool(1024 * 1024 * 100, 1024);
        new NetSystem(sharedPool, businessExecutor, timerExecutor);
        // Reactor pool
        NIOReactorPool reactorPool = new NIOReactorPool("Reactor Pool", 2, sharedPool);
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
