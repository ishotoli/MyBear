package io.mybear.storage;

import io.mybear.common.*;
import io.mybear.net2.*;
import io.mybear.storage.trunkMgr.TrunkShared;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.mybear.common.FdfsGlobal.g_fdfs_base_path;

/**
 * Created by jamie on 2017/6/21.
 */
public class FdfsStoraged {
    public static final int SCHEDULE_ENTRIES_MAX_COUNT = 9;
    public static final int MAX_PATH_SIZE = 256;
    public static final long g_current_time = System.currentTimeMillis();
    public static final boolean DEBUG_FLAG = true;
    private static final Logger LOGGER = LoggerFactory.getLogger(FdfsStoraged.class);
    public static long g_up_time = g_current_time;

    public static void main(String[] args) throws Exception {
        main0(new String[]{"storage.conf", "start"});
    }

    public static void main0(String[] args) throws Exception {
        String confFilename;
        int result;
        int sock;
        int waitCount;
        Thread scheduleTid;
        ScheduleEntry[] scheduleEntry = new ScheduleEntry[SCHEDULE_ENTRIES_MAX_COUNT];
        ScheduleArray scheduleArray;
        boolean stop = false;
        if (args.length < 2) {
            usage(args[0]);
            return;
        }
        TrunkShared.trunkSharedInit();
        confFilename = args[0];
        Path conf = ProcessAction.getBasePathFromConfFile(confFilename);
        Path pidFilename = Paths.get(g_fdfs_base_path, "/data/fdfs_storaged.pid");
        if (!ProcessAction.processAction(pidFilename, args[1])) {
            return;
        }
        StorageGlobal.exeName = Paths.get(args[0]).toAbsolutePath();
        if (DEBUG_FLAG && StorageGlobal.exeName == null) {
            LOGGER.error("exit abnormally!\n");
            return;
        }
        String bindAddr = storageFuncInit(conf);
        socketServer(bindAddr, StorageGlobal.G_SERVER_PORT);


    }

    /**
     * @param filename
     * @return bind_addr
     */
    static String storageFuncInit(final Path filename) {
        IniFileReader iniContext = StorageGlobal.iniReader;
        String g_client_bind_addr = iniContext.getStrValue("client_bind");
        String pGroupName = iniContext.getStrValue("group_name");
        String pRunByGroup = iniContext.getStrValue("run_by_group");
        String pRunByUser = iniContext.getStrValue("run_by_user");
        String pFsyncAfterWrittenBytes = iniContext.getStrValue("fsync_after_written_bytes");
        String pThreadStackSize = iniContext.getStrValue("thread_stack_size");
        String pBuffSize = iniContext.getStrValue("buff_size");
        String pIfAliasPrefix = iniContext.getStrValue("if_alias_prefix");
        String pHttpDomain = iniContext.getStrValue("http.domain_name");
        String pRotateAccessLogSize = iniContext.getStrValue("rotate_access_log_size");
        String pRotateErrorLogSize = iniContext.getStrValue("rotate_error_log_size");

        do {
            if (iniContext.getBoolValue("disabled", false)) {
                LOGGER.info("");
                break;
            }
            StorageGlobal.g_subdir_count_per_path = iniContext.getIntValue("subdir_count_per_path", StorageGlobal.DEFAULT_DATA_DIR_COUNT_PER_PATH);
            if (StorageGlobal.g_subdir_count_per_path < 0 || StorageGlobal.g_subdir_count_per_path > 256) {
                LOGGER.info("");
                break;
            }
        } while (false);
        String pBindAddr = iniContext.getStrValue("bind_addr");

        if (pBindAddr == null) pBindAddr = "";
        else LOGGER.info("");


        int result;
        long fsync_after_written_bytes;

        long buff_size;
        long rotate_access_log_size;
        long rotate_error_log_size;
        return pBindAddr;

    }

    static void socketServer(String g_bind_addr, int g_server_port) throws IOException {
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
        final StorageNio SINGLE = new StorageNio();

        ConnectionFactory frontFactory = new ConnectionFactory() {
            @Override
            protected Connection makeConnection(SocketChannel channel) throws IOException {
                return new FastTaskInfo(channel);
            }

            @Override
            protected NIOHandler getNIOHandler() {
                return SINGLE;
            }
        };
        NIOAcceptor server = new NIOAcceptor("Server", g_bind_addr, g_server_port, frontFactory, reactorPool);
        server.start();
    }

    static void usage(String program) {
        LOGGER.error("Usage: %s <config_file> [start | stop | restart]\n", program);
    }
}
