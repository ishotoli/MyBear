package io.mybear.storage;

import io.mybear.common.ScheduleArray;
import io.mybear.common.ScheduleEntry;
import io.mybear.storage.trunkMgr.TrunkShared;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
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
    private static final Logger logger = LoggerFactory.getLogger(FdfsStoraged.class);
    public static long g_up_time = g_current_time;

    public static void main(String[] args) throws Exception {
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
        confFilename = args[1];
        Path conf = Paths.get(g_fdfs_base_path);
        if (!Files.exists(conf)) {
            return;
        }
        String pidFilename = String.format("%s/data/fdfs_storaged.pid", g_fdfs_base_path);
        logger.info(pidFilename);
        File file = new File(pidFilename);
        if (!file.exists()) {
            file.createNewFile();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        try (FileLock fileLock = fc.tryLock()) {
            if (DEBUG_FLAG) {

            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }

    static void usage(String program) {
        logger.error("Usage: %s <config_file> [start | stop | restart]\n", program);
    }
}
