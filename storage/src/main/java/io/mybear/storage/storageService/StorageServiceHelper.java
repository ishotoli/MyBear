package io.mybear.storage.storageService;

import io.mybear.common.ThrowingConsumer;
import io.mybear.common.constants.SizeOfConstant;
import io.mybear.common.constants.config.FdfsGlobal;
import io.mybear.storage.StorageDio;
import io.mybear.storage.StorageFileContext;
import io.mybear.storage.StorageUploadInfo;
import io.mybear.storage.storageNio.StorageClientInfo;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;

import static io.mybear.common.constants.ErrorNo.ENOENT;
import static io.mybear.common.constants.SizeOfConstant.SIZE_OF_TRACKER_HEADER;
import static io.mybear.common.constants.TrackerProto.TRACKER_PROTO_CMD_RESP;
import static io.mybear.common.constants.config.StorageGlobal.STORAGE_FILE_SIGNATURE_METHOD_HASH;
import static io.mybear.common.constants.config.StorageGlobal.g_file_signature_method;

/**
 * Created by jamie on 2017/8/1.
 */
public class StorageServiceHelper {
    public static ByteBuffer getHeadFromBufferPool(StorageClientInfo pClientInfo, long size) {
        return pClientInfo.getMyBufferPool().allocateByteBuffer().putLong(size).put(TRACKER_PROTO_CMD_RESP).put((byte) 0);
    }

    public static ByteBuffer getHeadFromBufferPool(StorageClientInfo pClientInfo) {
        return pClientInfo.getMyBufferPool().allocateByteBuffer().putLong(0).put(TRACKER_PROTO_CMD_RESP).put((byte) 0);
    }

    public static ByteBuffer getHeadSetStateFromBufferPool(StorageClientInfo pClientInfo, int state) {
        return pClientInfo.getMyBufferPool().allocateByteBuffer().putLong(0).put(TRACKER_PROTO_CMD_RESP).put((byte) state);
    }

    /**
     * storage_read_from_file
     *
     * @param pClientInfo
     * @param file_offset
     * @param download_bytes
     * @param done_callback
     * @param store_path_index
     * @return
     * @throws IOException
     */
    public static int storage_read_from_file(StorageClientInfo pClientInfo,
                                             final long file_offset,
                                             final long download_bytes,
                                             ThrowingConsumer<StorageClientInfo> done_callback,
                                             final int store_path_index) throws IOException {
        StorageFileContext pFileContext = pClientInfo.fileContext;
        pClientInfo.cleanFunc = StorageDio::dio_read_finish_clean_up;
        pClientInfo.totalLength = SIZE_OF_TRACKER_HEADER + download_bytes;
        pClientInfo.totalOffset = 0;
        pFileContext.op = StorageDio.FDFS_STORAGE_FILE_OP_READ;
        pFileContext.openFlags = StandardOpenOption.READ;
        pFileContext.offset = file_offset;
        pFileContext.start = file_offset;
        pFileContext.end = file_offset + download_bytes;
        pFileContext.dioExecutorService = StorageDio.getThreadIndex(pClientInfo, store_path_index, pFileContext.op);
        pFileContext.done_callback = done_callback;
        ByteBuffer head = getHeadFromBufferPool(pClientInfo, download_bytes);
        if (sendDataImmediately(pClientInfo, head)) {
            pClientInfo.dealFunc = StorageDio::dio_read_file;
            pClientInfo.toDownloadData();
            //之后dio接着写数据,之后变成nio通知dio写数据
            StorageDio.queuePush(pClientInfo);
            pClientInfo.flagData = Boolean.FALSE;
            pClientInfo.enableWrite(true);
        } else {
            //nio通知模式
            pClientInfo.dealFunc = (c) -> {
                c.dealFunc = StorageDio::dio_read_file;
            }
            ;
            pClientInfo.toDownloadHead();
            pClientInfo.write(head);
            //write之后,写入通知不会取消,会一直通知能写入
        }
        return 0;
    }

    static int storage_write_to_file(StorageClientInfo pClientInfo,
                                     final long file_offset,
                                     final long upload_bytes,
                                     final int buff_offset,
                                     ThrowingConsumer<StorageClientInfo> deal_func,
                                     ThrowingConsumer<StorageClientInfo> done_callback,
                                     ThrowingConsumer<StorageClientInfo> clean_func,
                                     final int store_path_index) throws IOException {
        StorageFileContext pFileContext = pClientInfo.fileContext;
        int result;
        pClientInfo.toUpload();
        pClientInfo.dealFunc = deal_func;
        pClientInfo.cleanFunc = clean_func;

        pFileContext.fileChannel = null;
        pFileContext.buffOffset = buff_offset;
        pClientInfo.readBuffer.position(buff_offset);
        pFileContext.offset = file_offset;
        pFileContext.start = file_offset;
        pFileContext.end = file_offset + upload_bytes;
        pFileContext.dioExecutorService = StorageDio.getThreadIndex(pClientInfo, store_path_index, pFileContext.op);
        pFileContext.done_callback = done_callback;

        if (pFileContext.calcCrc32) {
            // pFileContext.crc32 = CRC32_XINIT;
        }

        if (pFileContext.calcCrc32) {
            if (g_file_signature_method == STORAGE_FILE_SIGNATURE_METHOD_HASH) {
                //  INIT_HASH_CODES4(pFileContext.file_hash_codes)
            } else {
                // my_md5_init( & pFileContext.md5_context);
            }
        }
        StorageDio.queuePush(pClientInfo);
        pClientInfo.flagData = Boolean.FALSE;
        return 0;
    }

    /**
     * 带flip
     * 如果马上写入完整则可以发送文件数据了，如果没有发送完整就要等待nio通知再写入
     *
     * @param pClientInfo
     * @param data
     * @return true 则写入完整，false则没有写入完整
     * @throws IOException
     */
    static boolean sendDataImmediately(StorageClientInfo pClientInfo, ByteBuffer data) throws IOException {
        data.flip();
        pClientInfo.getChannel().write(data);
        return !data.hasRemaining();
    }

    public static int set_file_utimes(String filename, final long new_time) {
//        struct timeval tvs[2];
//
//        tvs[0].tv_sec = new_time;
//        tvs[0].tv_usec = 0;
//        tvs[1].tv_sec = new_time;
//        tvs[1].tv_usec = 0;
//        if (utimes(filename, tvs) != 0)
//        {
//            logWarning("file: "__FILE__", line: %d, " \
//                    "call utimes file: %s fail" \
//                    ", errno: %d, error info: %s", \
//                    __LINE__, filename, errno, STRERROR(errno));
//            return errno != 0 ? errno : ENOENT;
//        }

        return 0;
    }

    public static void CHECK_AND_WRITE_TO_STAT_FILE1(StorageClientInfo pClientInfo) {
//        pthread_mutex_lock( & stat_count_thread_lock); \
//            \
//        if (pClientInfo -> pSrcStorage == NULL) \
//        { \
//            pClientInfo -> pSrcStorage = get_storage_server( \
//                    pClientInfo -> storage_server_id); \
//        } \
//        if (pClientInfo -> pSrcStorage != NULL) \
//        { \
//            pClientInfo -> pSrcStorage -> last_sync_src_timestamp = \
//            pFileContext -> timestamp2log; \
//            g_sync_change_count++; \
//        } \
//            \
//        g_storage_stat.last_sync_update = g_current_time; \
//        ++g_stat_change_count; \
//        pthread_mutex_unlock( & stat_count_thread_lock);
    }


    public static void CHECK_AND_WRITE_TO_STAT_FILE1_WITH_BYTES(LongAdder total_bytes, LongAdder success_bytes, long bytes) {
//
//        pthread_mutex_lock( & stat_count_thread_lock); \
//            \
//        if (pClientInfo -> pSrcStorage == NULL) \
//
//        { \
//            pClientInfo -> pSrcStorage = get_storage_server( \
//                    pClientInfo -> storage_server_id); \
//        } \
//        if (pClientInfo -> pSrcStorage != NULL) \
//
//        { \
//            pClientInfo -> pSrcStorage -> last_sync_src_timestamp = \
//            pFileContext -> timestamp2log; \
//            g_sync_change_count++; \
//        } \
//            \
//        g_storage_stat.last_sync_update = g_current_time; \
//        total_bytes += bytes; \
//        success_bytes += bytes; \
//        ++g_stat_change_count; \
//
//        pthread_mutex_unlock( & stat_count_thread_lock);
    }


    public static void CHECK_AND_WRITE_TO_STAT_FILE2(LongAdder total_count, LongAdder success_count) {
//
//        pthread_mutex_lock( & stat_count_thread_lock); \
//        total_count++; \
//        success_count++; \
//        ++g_stat_change_count; \
//
//        pthread_mutex_unlock( & stat_count_thread_lock);
    }

    public static void CHECK_AND_WRITE_TO_STAT_FILE2_WITH_BYTES(LongAdder total_count, LongAdder success_count,
                                                                LongAdder total_bytes, LongAdder success_bytes, long bytes) {
//
//        pthread_mutex_lock( & stat_count_thread_lock); \
//        total_count++; \
//        success_count++; \
//        total_bytes += bytes; \
//        success_bytes += bytes; \
//        ++g_stat_change_count; \
//
//        pthread_mutex_unlock( & stat_count_thread_lock);
    }

    public static void CHECK_AND_WRITE_TO_STAT_FILE3(LongAdder total_count, LongAdder success_count, long timestamp) {
//
//        pthread_mutex_lock( & stat_count_thread_lock); \
//        total_count++; \
//        success_count++; \
//        timestamp = g_current_time; \
//        ++g_stat_change_count;  \
//
//        pthread_mutex_unlock( & stat_count_thread_lock);
    }


    public static void CHECK_AND_WRITE_TO_STAT_FILE3_WITH_BYTES(LongAdder total_count, LongAdder success_count,
                                                                long timestamp, LongAdder total_bytes, LongAdder success_bytes, long bytes) {
//
//        pthread_mutex_lock( & stat_count_thread_lock); \
//        total_count++; \
//        success_count++; \
//        timestamp = g_current_time; \
//        total_bytes += bytes; \
//        success_bytes += bytes; \
//        ++g_stat_change_count;  \
//
//        pthread_mutex_unlock( & stat_count_thread_lock);
    }

    public static void storage_log_access_log(StorageClientInfo pTask, String action, final int status) {
//        StorageClientInfo * pClientInfo;
//        struct timeval tv_end;
//        int time_used;
//
//        pClientInfo = (StorageClientInfo *) pTask -> arg;
//        gettimeofday( & tv_end, NULL);
//        time_used = (tv_end.tv_sec - pClientInfo -> file_context. \
//        tv_deal_start.tv_sec) *1000
//                + (tv_end.tv_usec - pClientInfo -> file_context. \
//        tv_deal_start.tv_usec) /1000;
//        logAccess( & g_access_log_context, &(pClientInfo -> file_context. \
//        tv_deal_start),"%s %s %s %d %d %" PRId64 " " \
//        "%" PRId64, pTask -> client_ip, action, \
//        pClientInfo -> file_context.fname2log, status, time_used, \
//        pClientInfo -> request_length, pClientInfo -> total_length);
    }


    public static void STORAGE_ACCESS_STRCPY_FNAME2LOG(String filename, StorageClientInfo pClientInfo) {
//
//        do \
//
//    { \
//        if (g_use_access_log) \
//        { \
//            if (filename_len < sizeof(pClientInfo -> \
//                    file_context.fname2log)) \
//            { \
//                memcpy(pClientInfo -> file_context.fname2log, \
//                        filename, filename_len + 1); \
//            } \
//            else \
//            { \
//                memcpy(pClientInfo -> file_context.fname2log, \
//                        filename, sizeof(pClientInfo -> \
//                                file_context.fname2log)); \
//                *(pClientInfo -> file_context.fname2log + \
//                sizeof(pClientInfo -> file_context. \
//                        fname2log) - 1) ='\0'; \
//            } \
//        } \
//    } while(0)
    }


    public static void STORAGE_ACCESS_LOG(StorageClientInfo pClientInfop, String action, int status) {
//        do \
//
//        { \
//            if (g_use_access_log && (status != STORAGE_STATUE_DEAL_FILE)) \
//            { \
//                storage_log_access_log(pTask, action, status); \
//            } \
//        } while (0)
    }

    public static int storage_sync_copy_file_rename_filename(StorageFileContext pFileContext) {
//        char full_filename[MAX_PATH_SIZE + 256];
//        char true_filename[128];
//        int filename_len;
//        int result;
//        int store_path_index;
//
//        filename_len = strlen(pFileContext->fname2log);
//        if ((result=storage_split_filename_ex(pFileContext->fname2log, \
//                &filename_len, true_filename, &store_path_index)) != 0)
//        {
//            return result;
//        }
//
//        snprintf(full_filename, sizeof(full_filename), \
//                "%s/data/%s", g_fdfs_store_paths.paths[store_path_index], \
//                true_filename);
//        if (rename(pFileContext->filename, full_filename) != 0)
//        {
//            result = errno != 0 ? errno : EPERM;
//            logWarning("file: "__FILE__", line: %d, " \
//                    "rename %s to %s fail, " \
//                    "errno: %d, error info: %s", __LINE__, \
//                    pFileContext->filename, full_filename, \
//                    result, STRERROR(result));
//            return result;
//        }

        return 0;
    }

    public static final void STORAGE_STAT_FILE_FAIL_LOG(int result, String client_ip, String type_caption, String filename) {
//        if (result == ENOENT) \
//        { \
//            logWarning("file: "__FILE__", line: %d, " \
//                    "client ip: %s, %s file: %s not exist", \
//                    __LINE__, client_ip, type_caption, filename); \
//        } \
//            else \
//        { \
//            logError("file: "__FILE__", line: %d, " \
//                    "call stat fail, client ip: %s, %s file: %s, "\
//                    "error no: %d, error info: %s", __LINE__, client_ip, \
//                    type_caption, filename, result, STRERROR(result)); \
//        }
    }

    public static void STORAGE_NIO_NOTIFY_CLOSE(StorageClientInfo pTask) {
//     do { \
//         ((StorageClientInfo *) pTask -> arg)->stage = FDFS_STORAGE_STAGE_NIO_CLOSE; \
//         storage_nio_notify(pTask); \
//     } while (0)
    }

    public static long COMBINE_RAND_FILE_SIZE(long file_size) {
        int r;
        r = (ThreadLocalRandom.current().nextInt() & 0x007FFFFF) | 0x80000000;
        return (((long) r) << 32) | file_size;
    }

    static int storage_delete_file_auto(StorageFileContext pFileContext) {
        if (((StorageUploadInfo) pFileContext.extra_info).isTRUNK()) {
//            return trunk_file_delete(pFileContext->filename,
//                    &(pFileContext->extra_info.upload.trunk_info));
        } else {
            if (new File(pFileContext.filename).delete()) {
                return 0;
            } else {
                return ENOENT;
            }
        }
        return 0;
    }

    private static void STORAGE_ACCESS_STRCPY_FNAME2LOG(char[] filename, int filename_len,
                                                        StorageClientInfo pClientInfo) {
        if (FdfsGlobal.g_use_access_log) {
            if (filename_len < SizeOfConstant.SIZE_OF_FNAME2LOG) {
                //memcpy(des,src,leng);
                System.arraycopy(filename, 0, pClientInfo.fileContext.fname2log, 0, filename_len + 1);
            } else {
                System.arraycopy(filename, 0, pClientInfo.fileContext.fname2log, 0, SizeOfConstant.SIZE_OF_FNAME2LOG);
            }
        }
    }

}
