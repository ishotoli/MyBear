package io.mybear.storage.storageDiskRecovery;

import io.mybear.common.utils.SharedFunc;
import io.mybear.storage.storageSync.StorageBinLogReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static io.mybear.common.constants.CommonConstant.FDFS_STORAGE_STATUS_NONE;
import static io.mybear.common.utils.SharedFunc.fileExists;

/**
 * Created by jamie on 2017/8/12.
 */
public class StoragediskRecovery {
    public static final Logger log = LoggerFactory.getLogger(StoragediskRecovery.class);
    public static final String RECOVERY_BINLOG_FILENAME = ".binlog.recovery";
    public static final String RECOVERY_MARK_FILENAME = ".recovery.mark";
    public static final String MARK_ITEM_BINLOG_OFFSET = "binlog_offset";
    public static final String MARK_ITEM_FETCH_BINLOG_DONE = "fetch_binlog_done";
    public static final String MARK_ITEM_SAVED_STORAGE_STATUS = "saved_storage_status";
    final static Map<String, FileChannel> fileCache = new HashMap<>();
    public static int saved_storage_status = FDFS_STORAGE_STATUS_NONE;

    /**
     * todo 性能优化
     *
     * @param filename
     * @param content
     * @throws IOException
     */
    private static synchronized void write(String filename, String content) throws IOException {
        FileChannel fileChannel;
        if (null == (fileChannel = fileCache.get(filename))) {
            fileChannel = FileChannel.open(Paths.get(filename));
            fileCache.put(filename, fileChannel);
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(content.getBytes(StandardCharsets.US_ASCII));
        byteBuffer.flip();
        fileChannel.truncate(0).write(byteBuffer);
    }

    public static void writeToMarkFile(String filename, int saved_storage_status, long binlog_offset) throws IOException {
        String s = String.format("%s=%d\n" +
                        "%s=%d\n" +
                        "%s=1\n",
                MARK_ITEM_SAVED_STORAGE_STATUS, saved_storage_status,
                MARK_ITEM_BINLOG_OFFSET, binlog_offset, MARK_ITEM_FETCH_BINLOG_DONE);
        write(filename, s);
    }

    private static String getFullFilename(String pBasePath, String filename) {
        return String.format("%s/data/%s", pBasePath, filename);
    }

    private static void initMarkFile(String pBasePath, int fetch_binlog_done) throws IOException {
        String full_filename = getFullFilename(pBasePath, RECOVERY_MARK_FILENAME);
        String buff = String.format(
                "%s=%d\n" +
                        "%s=0\n" +
                        "%s=%d\n",
                MARK_ITEM_SAVED_STORAGE_STATUS, saved_storage_status,
                MARK_ITEM_BINLOG_OFFSET,
                MARK_ITEM_FETCH_BINLOG_DONE, fetch_binlog_done);
        write(full_filename, buff);
    }

    private static void initBinlogFile(String pBasePath) throws IOException {
        String full_filename = getFullFilename(pBasePath, RECOVERY_BINLOG_FILENAME);
        File file = new File(full_filename);
        file.createNewFile();
    }

    public static void recoveryFinish(String pBasePath) throws IOException {
        String full_filename = getFullFilename(pBasePath, RECOVERY_BINLOG_FILENAME);
        if (fileExists(full_filename)) {
            if (SharedFunc.unlink(full_filename)) {
                throw new IOException("不能删除 " + full_filename);
            }
        }
    }

    public static void storage_disk_recovery_restore(String pBasePath) {
        String full_binlog_filename = getFullFilename(pBasePath, RECOVERY_BINLOG_FILENAME);
        String full_mark_filename = getFullFilename(pBasePath, RECOVERY_MARK_FILENAME);
        Socket srcStorage;
        int result;
        StorageBinLogReader reader;
        if (!(fileExists(full_mark_filename) && fileExists(full_binlog_filename))) {
            return;
        }

        log.info("disk recovery: begin recovery data path: %s ...", pBasePath);


    }

    /**
     * 因此当非首次启动时，检测到StorePath下不存在这两级的256个子目录，
     * \那么程序就会认为该StorePath数据丢失，开始进行这个StorePath的磁盘恢复。
     */
    public void recoveryStart() throws IOException {
        String basePath = "";
    /*
    1）创建状态文件 .recovery.mark；.binlog.recovery，并初始化。
     */
        File mark = new File(basePath + RECOVERY_MARK_FILENAME);
        if (!mark.exists()) {
            mark.createNewFile();
        }
        File binlog = new File(basePath + RECOVERY_BINLOG_FILENAME);
        if (!binlog.exists()) {
            mark.createNewFile();
        }

    }

    public void storage_do_recovery() {

    }
}
