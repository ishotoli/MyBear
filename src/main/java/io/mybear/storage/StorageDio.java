package io.mybear.storage;

import io.mybear.common.FileBeforeOpenCallback;
import io.mybear.common.StorageDioThreadData;
import io.mybear.common.StorageFileContext;
import io.mybear.common.StorageUploadInfo;
import io.mybear.storage.storageNio.Connection;
import io.mybear.storage.storageNio.FastTaskInfo;
import io.mybear.storage.storageNio.TimeUtil;
import io.mybear.storage.trunkMgr.TrunkShared;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.CRC32;

import static io.mybear.storage.trunkMgr.TrunkShared.*;

/**
 * Created by jamie on 2017/6/21.
 */
public class StorageDio {
    public static final int _FILE_TYPE_APPENDER = 1;
    public static final int FILE_TYPE_TRUNK = 2; //if trunk file, since V3.0
    public static final int _FILE_TYPE_SLAVE = 4;
    public static final int _FILE_TYPE_REGULAR = 8;
    public static final int _FILE_TYPE_LINK = 16;
    public static final int g_disk_reader_threads = 1;
    public static final int g_disk_writer_threads = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageDio.class);
    public static Object g_dio_thread_lock = new Object();
    public static ExecutorService[] g_dio_contexts;
    public static StorageDioThreadData[] g_dio_thread_data;
    public static boolean g_disk_rw_separated = false;
    CRC32 crc32 = new CRC32();

    public static void init() {
        g_dio_thread_lock = new Object();
        int threads_count_per_path = g_disk_reader_threads + g_disk_writer_threads;
        if (g_fdfs_store_paths == null) {
            g_fdfs_store_paths = new Path[]{Paths.get("data")};
        }
        int context_count = threads_count_per_path * g_fdfs_store_paths.length;
        g_dio_contexts = new ExecutorService[context_count];
        for (int i = 0; i < g_dio_contexts.length; i++) {
            // ArrayBlockingQueue
            g_dio_contexts[i] = Executors.newSingleThreadExecutor();
        }
        g_dio_thread_data = new StorageDioThreadData[g_fdfs_store_paths.length];
        for (int i = 0; i < g_dio_thread_data.length; i++) {
            StorageDioThreadData threadData = g_dio_thread_data[i] = new StorageDioThreadData();
            threadData.count = threads_count_per_path;
            threadData.contexts = Arrays.copyOfRange(g_dio_contexts, i, i * threads_count_per_path);
            threadData.reader = threadData.contexts;
            threadData.writer = Arrays.copyOfRange(g_dio_contexts, i * threads_count_per_path, i * threads_count_per_path + g_disk_reader_threads);
        }


    }

    public static ExecutorService get_thread_index(FastTaskInfo pTask, final int store_path_index, final char file_op) {
        ExecutorService[] contexts;
        int count;
        StorageDioThreadData pThreadData = g_dio_thread_data[store_path_index];
        if (g_disk_rw_separated) {
            if (file_op == FDFS_TRUNK_FILE_ALLOC_SIZE_OFFSET) {
                contexts = pThreadData.reader;
                count = g_disk_reader_threads;
            } else {
                contexts = pThreadData.writer;
                count = g_disk_writer_threads;
            }
        } else {
            contexts = pThreadData.contexts;
            count = contexts.length;
        }
        return contexts[pTask.hashCode() % count];
    }

    public static boolean queuePush(Connection pTask) {
        int index = 0;
//        StorageFileContext fileContext = pTask.getStorageClientInfo().file_context;
//        index=fileContext.dio_thread_index;
        g_dio_contexts[index].execute((FastTaskInfo) pTask);
        return true;
    }

    public static int dio_read_file(FastTaskInfo pTask) {
        StorageFileContext pFileContext = pTask.file_context;
        FileChannel channel = pFileContext.fileChannel;
        int result = 0;
        long end = 0;
        do {
            try {
                //dio_open_file(pFileContext);
                end = pFileContext.end;
                long got = channel.transferTo(pFileContext.offset, end - pFileContext.offset, pTask.getChannel());
                if (got == -1) {
                    pFileContext.fileChannel.close();
                    return -1;
                } else if (got == 0 && pTask.isIdleTimeout()) {
                    pFileContext.fileChannel.close();
                    pTask.close("超时");
                    return -1;
                }
                if (got != 0) {
                    pTask.setLastWriteTime(TimeUtil.currentTimeMillis());
                    pFileContext.offset += got;
                }

            } catch (IOException e) {
                result = -1;
                e.printStackTrace();
                LOGGER.error("");
            }
//            synchronized (g_dio_thread_lock) {
//                ++g_storage_stat.total_file_read_count;
//                if (result == 0) ++g_storage_stat.success_file_read_count;
//            }
        } while (false);

        if (pTask.offset < end) {
            //  pTask.enableWrite(false);
            StorageDio.queuePush(pTask);
        } else {
            try {
                pFileContext.fileChannel.close();
                if (pFileContext.done_callback != null)
                    pFileContext.done_callback.apply(pTask);
                pTask.close("任务完成");
            } catch (IOException e) {
                e.printStackTrace();
                result = -1;
            }
        }

        return result;

    }

    public static int dio_write_file(FastTaskInfo pTask) {
        if (!pTask.getChannel().isOpen()) return -1;
        StorageFileContext pFileContext = pTask.file_context;
        int result = 0;
        FileChannel channel = pFileContext.fileChannel;
        StorageUploadInfo uploadInfo = (StorageUploadInfo) pFileContext.extra_info;
        long end = pFileContext.end;
        do {
            if (channel == null || !channel.isOpen()) {
                FileBeforeOpenCallback callback = uploadInfo.getBeforeOpenCallback();
                if (callback != null) {
                    result = callback.apply(pTask);
                    if (result != 0) {
                        break;
                    }
                }
            }
            try {
                dio_open_file(pFileContext);
                channel = pFileContext.fileChannel;
                pFileContext.offset += channel.transferFrom(pTask.getChannel(), pFileContext.offset, end - pFileContext.offset);
            } catch (IOException e) {
                result = -1;
                e.printStackTrace();
                LOGGER.error("");
            }
            synchronized (g_dio_thread_lock) {
//                g_storage_stat.total_file_write_count++;
//                if (result == 0) {
//                    g_storage_stat.success_file_write_count++;
//                }
            }
            if (result != 0) break;
        } while (false);
        if (pFileContext.offset < end) {
//            pTask.enableWrite(false);
            StorageDio.queuePush(pTask);
        } else {
            try {
                LOGGER.info("任务完成");
                pFileContext.fileChannel.close();
                if (pFileContext.done_callback != null)
                    pFileContext.done_callback.apply(pTask);
            } catch (IOException e) {
                e.printStackTrace();
                result = -1;
            }
        }

        return result;
    }

    public static void dio_open_file(StorageFileContext pFileContext) throws IOException {
        try {
            FileChannel fileChannel = pFileContext.fileChannel;
            if (fileChannel == null) {
                Files.createFile(pFileContext.filename);
                pFileContext.fileChannel = FileChannel.open(pFileContext.filename, pFileContext.openFlags);

                if (pFileContext.offset > 0) {
                    fileChannel.position(pFileContext.offset);
                }
            }
        } catch (IOException e) {
//            synchronized (g_dio_thread_lock) {
//                g_storage_stat.total_file_open_count += 2;
//            }
            throw e;
        }
//
//        synchronized (g_dio_thread_lock) {
//            g_storage_stat.total_file_open_count += 1;
//        }
    }

    public void terminate() {
        g_dio_contexts = null;
    }

    /**
     * 之后还要修改
     *
     * @param pTask
     * @param fileContext
     * @return
     */
    public int dio_truncate_file(FastTaskInfo pTask, StorageFileContext fileContext) {
        int result = 0;
        try {
            do {
                StorageUploadInfo uploadInfo = ((StorageUploadInfo) fileContext.extra_info);
                if (!fileContext.fileChannel.isOpen()) {
                    FileBeforeOpenCallback callBack = uploadInfo.getBeforeOpenCallback();
                    if (callBack != null) {
                        result = callBack.apply(pTask);
                        if (result != 0) {
                            break;
                        }
                    }
                    dio_open_file(fileContext);
                }
                fileContext.fileChannel.truncate(fileContext.offset);
                if (uploadInfo.getBeforeCloseCallback() != null) {
                    result = uploadInfo.getBeforeCloseCallback().apply(pTask);
                }

	/* file write done, close it */
                fileContext.fileChannel.close();
                fileContext.fileChannel = null;
                if (fileContext.done_callback == null) {
                    result = fileContext.done_callback.apply(pTask);
                }
                return 0;
            } while (false);
        } catch (IOException e) {
            e.printStackTrace();
            result = -1;
        }
//        pTask.cx
//        pClientInfo -> clean_func(pTask);

        if (fileContext.done_callback != null) {
            fileContext.done_callback.apply(pTask);
        }
        return result;
    }

    public int dio_delete_normal_file(FastTaskInfo fastTaskInfo) {
        StorageFileContext fileContext = fastTaskInfo.file_context;
        try {
            Files.delete(fileContext.filename);
        } catch (IOException e) {
            e.printStackTrace();
            fileContext.log_callback.accept(fileContext);
        }
        return fileContext.done_callback.apply(fastTaskInfo);
    }

    public int dio_delete_trunk_file(FastTaskInfo pTask) {
        return 0;
    }

    public int dio_discard_file(FastTaskInfo pTask) {


        return 0;
    }

    public void dio_read_finish_clean_up(FastTaskInfo pTask) {

    }

    public void dio_write_finish_clean_up(FastTaskInfo pTask) {

    }

    public void dio_append_finish_clean_up(FastTaskInfo pTask) {

    }

    public void dio_trunk_write_finish_clean_up(FastTaskInfo pTask) {

    }

    public void dio_modify_finish_clean_up(FastTaskInfo pTask) {

    }

//
//    public int dio_check_trunk_file_ex(int fd, String filename, final long offset) {
//
//    }
//
//    public int dio_check_trunk_file_when_upload(FastTaskInfo pTask) {
//
//    }
//
//    public int dio_check_trunk_file_when_sync(FastTaskInfo pTask) {
//
//    }

    public int dio_write_chunk_header(FastTaskInfo pTask) {
        StorageFileContext fileContext = pTask.file_context;
        StorageUploadInfo uploadInfo = (StorageUploadInfo) fileContext.extra_info;
//        FDFSTrunkHeader trunkHeader = new FDFSTrunkHeader();
        ByteBuffer trunkHeader = ByteBuffer.allocate(17 + 6 + 1);
        if ((uploadInfo.getFileType() & _FILE_TYPE_LINK) > 0) {
            trunkHeader.put(TrunkShared.FDFS_TRUNK_FILE_TYPE_LINK);
        } else {
            trunkHeader.put(FDFS_TRUNK_FILE_TYPE_REGULAR);
        }
        trunkHeader.putInt(uploadInfo.getTrunkInfo().getFile().getSize());
        trunkHeader.putInt((int) (fileContext.end - fileContext.start));
        trunkHeader.putInt((int) fileContext.crc32);
        trunkHeader.putInt(uploadInfo.getStartTime());
//        char[]  uploadInfo.getFileExtName();
//        for (int i = 0; i < ; i++) {
//            trunkHeader.putChar();
//        }

        try {
            fileContext.fileChannel.position(fileContext.start);
        } catch (IOException e) {
//            result = errno != 0 ? errno : EIO;
//            logError("file: "__FILE__", line: %d, " \
//                    "lseek file: %s fail, " \
//                    "errno: %d, error info: %s", \
//                    __LINE__, pFileContext->filename, \
//                    result, STRERROR(result));
//            return result;
        }
        try {
            fileContext.fileChannel.write(trunkHeader);
        } catch (IOException e) {
//            result = errno != 0 ? errno : EIO;
//            logError("file: "__FILE__", line: %d, " \
//                    "write to file: %s fail, " \
//                    "errno: %d, error info: %s", \
//                    __LINE__, pFileContext->filename, \
//                    result, STRERROR(result));
//            return result;
        }
        return 0;


    }

}
