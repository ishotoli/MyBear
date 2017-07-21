package io.mybear.storage;

import io.mybear.common.*;
import io.mybear.storage.storageNio.Connection;
import io.mybear.storage.storageNio.StorageClientInfo;
import io.mybear.storage.trunkMgr.TrunkMem;
import io.mybear.storage.trunkMgr.TrunkShared;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.CRC32;

import static io.mybear.storage.StorageGlobal.*;
import static io.mybear.storage.trunkMgr.TrunkShared.FDFS_TRUNK_FILE_ALLOC_SIZE_OFFSET;
import static io.mybear.storage.trunkMgr.TrunkShared.FDFS_TRUNK_FILE_TYPE_REGULAR;

/**
 * Created by jamie on 2017/6/21.
 */
public class StorageDio {
    public static final int _FILE_TYPE_APPENDER = 1;
    public static final int FILE_TYPE_TRUNK = 2; //if trunk file, since V3.0
    public static final int _FILE_TYPE_SLAVE = 4;
    public static final int _FILE_TYPE_REGULAR = 8;
    public static final int _FILE_TYPE_LINK = 16;

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageDio.class);
    public static Object g_dio_thread_lock = new Object();
    public static ExecutorService[] g_dio_contexts;
    public static StorageDioThreadData[] g_dio_thread_data;
    public static boolean g_disk_rw_separated = false;
    CRC32 crc32 = new CRC32();

    public static void init() {
        g_dio_thread_lock = new Object();
        int threads_count_per_path = g_disk_reader_threads + g_disk_writer_threads;
        FdfsStorePaths paths = TrunkShared.getFdfsStorePaths();
        int pathConut = paths.getCount();
        int context_count = threads_count_per_path * pathConut;
        g_dio_contexts = new ExecutorService[context_count];
        for (int i = 0; i < g_dio_contexts.length; i++) {
            // ArrayBlockingQueue
            g_dio_contexts[i] = Executors.newSingleThreadExecutor();
        }
        g_dio_thread_data = new StorageDioThreadData[pathConut];
        for (int i = 0; i < g_dio_thread_data.length; i++) {
            StorageDioThreadData threadData = g_dio_thread_data[i] = new StorageDioThreadData();
            threadData.count = threads_count_per_path;
            threadData.contexts = Arrays.copyOfRange(g_dio_contexts, i, i + g_disk_reader_threads);
            threadData.reader = threadData.contexts;
            threadData.writer = Arrays.copyOfRange(g_dio_contexts, i + g_disk_reader_threads, i + g_disk_reader_threads + g_disk_writer_threads);
        }
    }

    /**
     * 写完了
     *
     * @param pTask
     * @param store_path_index
     * @param file_op
     * @return
     */
    public static ExecutorService getThreadIndex(StorageClientInfo pTask, final int store_path_index, final int file_op) {
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
        StorageClientInfo storageClientInfo = ((StorageClientInfo) pTask);
        storageClientInfo.fileContext.dioExecutorService.execute(storageClientInfo);
        return true;
    }

    /**
     * 写完了
     *
     * @param pTask
     */
    public static int dio_read_file(StorageClientInfo pTask) {
        StorageFileContext pFileContext = pTask.fileContext;
        FileChannel channel = pFileContext.fileChannel;
        if (!pTask.getChannel().isOpen()) return -1;
        try {
            dio_open_file(pFileContext);
            g_storage_stat.total_file_read_count.increment();
            long end = pFileContext.end;
            long got = channel.transferTo(pFileContext.offset, end - pFileContext.offset, pTask.getChannel());
            pFileContext.offset += got;
            g_storage_stat.success_file_read_count.increment();
            if (pTask.fileContext.offset == end) {
                pTask.getChannel().close();
                pFileContext.fileChannel.close();
                if (pFileContext.done_callback != null) {
                    pFileContext.done_callback.accept(pTask);
                }
                pTask.close("任务完成");
            } else {
                nioNotify(pTask);
            }
            return 0;
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage());
        }
        pTask.close("异常");
        try {
            pFileContext.fileChannel.close();
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage());
        }
        if (pFileContext.done_callback != null) {
            pFileContext.done_callback.accept(pTask);
        }
        return -1;
    }


    public static void nioNotify(StorageClientInfo pTask) {
        pTask.flagData = Boolean.TRUE;

    }

    /**
     * 写完了
     *
     * @param pTask
     */
    public static int dio_write_file(StorageClientInfo pTask) {
        if (!pTask.getChannel().isOpen()) return -1;
        StorageFileContext pFileContext = pTask.fileContext;
        try {
            FileChannel channel = pFileContext.fileChannel;
            StorageUploadInfo uploadInfo = (StorageUploadInfo) pFileContext.extra_info;
            long end = pFileContext.end;
            int pos = pTask.readBuffer.position();
            int limit = pTask.readBuffer.limit();
            if (channel == null || !channel.isOpen()) {
                FileBeforeOpenCallback callback = uploadInfo.getBeforeOpenCallback();
                if (callback != null) {
                    callback.accept(pTask);
                }
                dio_open_file(pFileContext);
            }
            g_storage_stat.total_file_write_count.increment();
            channel = pFileContext.fileChannel;
            pTask.readBuffer.flip();
            pFileContext.offset += channel.write(pTask.readBuffer);
            g_storage_stat.success_file_write_count.increment();
            if (pFileContext.calcCrc32) {
                pTask.readBuffer.position(pos).limit(limit);
                pFileContext.crc32.update(pTask.readBuffer);
            }
            if (pFileContext.calcFileHash) {
                if (g_file_signature_method == STORAGE_FILE_SIGNATURE_METHOD_HASH) {
                    pTask.readBuffer.position(pos).limit(limit);
                    // HashUtil.hashCODES4(pTask.readBuffer, pFileContext.fileHashCodes);
                } else {
                    pTask.readBuffer.position(pos).limit(limit);
                    pFileContext.MD5CTX.update(pTask.readBuffer);
                }
            }
            if (pFileContext.offset < end) {
                nioNotify(pTask);
            } else {
                if (g_file_signature_method == STORAGE_FILE_SIGNATURE_METHOD_HASH) {
                    pTask.readBuffer.position(pos).limit(limit);
                    //   HashUtil.finishHashCodes4(pFileContext.fileHashCodes);
                } else {
                    //pFileContext.MD5CTX.digest()
                }
//            LOGGER.debug("CRC32:" + pFileContext.crc32.getValue());
//            LOGGER.debug("md5" + HashUtil.bytes2HexString(pFileContext.MD5CTX.digest()));
//            LOGGER.info("任务完成");
                pFileContext.fileChannel.close();
                pFileContext.fileChannel = null;
                pFileContext.randomAccessFile.close();
                pFileContext.randomAccessFile = null;
                if (pFileContext.done_callback != null)
                    pFileContext.done_callback.accept(pTask);
            }
            return 0;
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage());
        }
        pTask.cleanFunc.accept(pTask);
        if (pFileContext.done_callback != null)
            pFileContext.done_callback.accept(pTask);
        return -1;
    }

    public static void dio_open_file(StorageFileContext pFileContext) throws IOException {
        FileChannel fileChannel = pFileContext.fileChannel;
        if (fileChannel == null || (!fileChannel.isOpen())) {
            g_storage_stat.total_file_open_count.increment();
            File file = new File(pFileContext.filename);
            file.createNewFile();
            pFileContext.randomAccessFile = new RandomAccessFile(pFileContext.filename, "rw");//之后要修改
            pFileContext.fileChannel = pFileContext.randomAccessFile.getChannel();
            g_storage_stat.success_file_open_count.increment();
            if (pFileContext.offset > 0) {
                pFileContext.fileChannel.position(pFileContext.offset);
            }
            g_storage_stat.success_file_open_count.increment();
        }
    }


    public void terminate() {
        g_dio_contexts = null;
    }

    /**
     * 写完了
     *
     * @param pTask
     * @param
     * @return
     */
    public int dio_truncate_file(StorageClientInfo pTask) {
        StorageFileContext pFileContext = pTask.fileContext;
        StorageUploadInfo uploadInfo = ((StorageUploadInfo) pTask.fileContext.extra_info);
        try {
            if (pFileContext.fileChannel == null || !pFileContext.fileChannel.isOpen()) {
                FileBeforeOpenCallback callBack = uploadInfo.getBeforeOpenCallback();
                if (callBack != null) {
                    callBack.accept(pTask);
                }
                dio_open_file(pFileContext);
                pFileContext.fileChannel.truncate(pFileContext.offset);
                if (uploadInfo.getBeforeCloseCallback() != null) {
                    uploadInfo.getBeforeCloseCallback().accept(pTask);
                }
                pFileContext.fileChannel.close();
                pFileContext.fileChannel = null;
                if (pFileContext.done_callback == null) {
                    pFileContext.done_callback.accept(pTask);
                }
            }
            return 0;
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage());
        }
        pTask.cleanFunc.accept(pTask);
        if (pFileContext.done_callback == null) {
            pFileContext.done_callback.accept(pTask);
        }
        return -1;
    }

    /**
     * 写完了
     *
     * @param task
     */
    public int dio_delete_normal_file(StorageClientInfo task) {
        StorageFileContext fileContext = task.fileContext;
        int result = 0;
        if (!new File(fileContext.filename).delete()) {
            result = -1;
            fileContext.log_callback.accept(task);
        }
        fileContext.done_callback.accept(task);
        return result;
    }

    /**
     * 写完了
     *
     * @param pTask
     * @return
     */
    public int dio_delete_trunk_file(StorageClientInfo pTask) {
        StorageFileContext fileContext = pTask.fileContext;
        int result = 0;
        if (TrunkMem.trunkFileDelete(fileContext.filename, ((StorageUploadInfo) fileContext.extra_info).getTrunkInfo()) != 0)
            ;
        {
            fileContext.log_callback.accept(pTask);
            result = -1;
        }
        fileContext.done_callback.accept(pTask);
        return 0;
    }

    public int dio_discard_file(StorageClientInfo pTask) {
        StorageFileContext fileContext = pTask.fileContext;
        int result = 0;
        fileContext.offset += pTask.readBuffer.position();//确定是这个意思?
        if (fileContext.offset >= fileContext.end) {
            fileContext.done_callback.accept(pTask);
        } else {
            pTask.readBuffer.clear();
            nioNotify(pTask);
        }
        return 0;
    }

    public void dio_read_finish_clean_up(StorageClientInfo pTask) {
        StorageFileContext fileContext = pTask.fileContext;
        if (fileContext.fileChannel != null && fileContext.fileChannel.isOpen()) {
            try {
                fileContext.fileChannel.close();
                fileContext.fileChannel = null;
                if (fileContext.randomAccessFile != null) {
                    fileContext.randomAccessFile.close();
                    fileContext.randomAccessFile = null;
                }
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        fileContext.fileChannel = null;
    }

    public void dio_write_finish_clean_up(StorageClientInfo pTask) {
        StorageFileContext fileContext = pTask.fileContext;
        if (fileContext.fileChannel != null && fileContext.fileChannel.isOpen()) {
            try {
                fileContext.fileChannel.close();
                fileContext.fileChannel = null;
                if (fileContext.offset < fileContext.end) {
                    try {
                        Files.delete(Paths.get(fileContext.filename));
                    } catch (IOException e) {
                        LOGGER.error("client ip: %s,delete useless file %s fail,error info: %s", pTask.getChannel().getRemoteAddress().toString(), fileContext.filename, e.getLocalizedMessage());
                    }
                }
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        fileContext.fileChannel = null;
    }

    public void dio_append_finish_clean_up(StorageClientInfo pTask) {
        StorageFileContext fileContext = pTask.fileContext;
        if (fileContext.fileChannel != null && fileContext.fileChannel.isOpen()) {
            try {
                if (fileContext.offset > fileContext.start && fileContext.offset < fileContext.end) {
                    try {
                        fileContext.fileChannel.truncate(fileContext.start);
                        LOGGER.debug("client ip: %s, append file fail,call ftruncate of file %s to size: %d",
                                pTask.getChannel().getRemoteAddress().toString(), fileContext.filename, fileContext.start);
                    } catch (IOException e) {
                        LOGGER.error("client ip: %s,delete useless file %s fail,error info: %s",
                                pTask.getChannel().getRemoteAddress().toString(), fileContext.filename, e.getLocalizedMessage());
                    }
                }
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage());
            } finally {
                try {
                    fileContext.fileChannel.close();
                } catch (IOException e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            }
        }
        fileContext.fileChannel = null;
    }

    public void dio_modify_finish_clean_up(StorageClientInfo pTask) {
        StorageFileContext fileContext = pTask.fileContext;
        if (fileContext.fileChannel != null && fileContext.fileChannel.isOpen()) {
            try {
                if (fileContext.offset >= fileContext.start && fileContext.offset < fileContext.end) {
                    LOGGER.error("client ip: %s,delete useless file %s fail,error info: %s", pTask.getChannel().getRemoteAddress().toString(), fileContext.filename);
                }
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage());
            } finally {
                try {
                    fileContext.fileChannel.close();
                } catch (IOException e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            }
        }
        fileContext.fileChannel = null;
    }
    public void dio_trunk_write_finish_clean_up(StorageClientInfo pTask) {
        StorageFileContext fileContext = pTask.fileContext;
        if (fileContext.fileChannel != null && fileContext.fileChannel.isOpen()) {
            try {
                fileContext.fileChannel.close();
                fileContext.fileChannel = null;
                if (fileContext.offset > fileContext.start && fileContext.offset < fileContext.end) {
                    if (TrunkMem.trunkFileDelete(fileContext.filename, ((StorageUploadInfo) pTask.extraArg).getTrunkInfo()) != 0) {
                        //  这里没有错误提示?
                    }
                }
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        fileContext.fileChannel = null;
    }



//
//    public int dio_check_trunk_file_ex(int fd, String filename, final long offset) {
//
//    }
//
//    public int dio_check_trunk_file_when_upload(StorageClientInfo pTask) {
//
//    }
//
//    public int dio_check_trunk_file_when_sync(StorageClientInfo pTask) {
//
//    }

    public int dio_write_chunk_header(StorageClientInfo pTask) {
        StorageFileContext fileContext = pTask.fileContext;
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
        trunkHeader.putInt((int) fileContext.crc32.getValue());
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
