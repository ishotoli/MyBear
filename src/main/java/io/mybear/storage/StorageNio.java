package io.mybear.storage;
//
//import io.mybear.common.FastTaskInfo;
//import io.mybear.common.StorageClientInfo;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.nio.ByteBuffer;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.ServerSocketChannel;
//import java.nio.channels.SocketChannel;
//import java.util.Objects;
//
///**
// * Created by jamie on 2017/6/21.
// */
//public class StorageNio {
//    private static final Logger logger = LoggerFactory.getLogger(StorageNio.class);
//    private static final int FDFS_STORAGE_STAGE_NIO_INIT = 0;
//    private static final int FDFS_STORAGE_STAGE_NIO_RECV = 1;
//    private static final int FDFS_STORAGE_STAGE_NIO_SEND = 2;
//    private static final int FDFS_STORAGE_STAGE_NIO_CLOSE = 4; //close socket
//    private static final int FDFS_STORAGE_STAGE_DIO_THREAD = 8;
//
//
//    private static final int FDFS_STORAGE_FILE_OP_READ = 'R';
//    private static final int FDFS_STORAGE_FILE_OP_WRITE = 'W';
//    private static final int FDFS_STORAGE_FILE_OP_APPEND = 'A';
//    private static final int FDFS_STORAGE_FILE_OP_DELETE = 'D';
//    private static final int FDFS_STORAGE_FILE_OP_DISCARD = 'd';
//
//    /**
//     * @param sock
//     * @param event
//     */
//    void storageRecvNotifyRead(SelectionKey selectedKey, short event, Object arg) {
//        FastTaskInfo pTask;
//        StorageClientInfo pClientInfo;
//        long remain_bytes;
//        int bytes;
//        int result;
//        SocketChannel socketChannel = (SocketChannel) selectedKey.channel();
//        while (true) {
//            if (!selectedKey.isValid() || !socketChannel.isConnected()) {
//                logger.error("file: %d, line: %d call read failed errno: %d, error info: %s");//未完成
//                break;
//            }
//            Object attachment = selectedKey.attachment();
//            if (attachment == null) { //quit flag
//                return;
//            }
//            pTask = (FastTaskInfo) attachment;
//            pClientInfo = (StorageClientInfo) pTask.arg;
//
//            if ((pClientInfo.stage & FDFS_STORAGE_STAGE_DIO_THREAD) == FDFS_STORAGE_STAGE_DIO_THREAD) {
//                pClientInfo.stage &= ~FDFS_STORAGE_STAGE_DIO_THREAD;
//            }
//            switch (pClientInfo.stage) {
//                case FDFS_STORAGE_STAGE_NIO_INIT:
//                    result = storageNioInit(pTask);
//                    break;
//                case FDFS_STORAGE_STAGE_NIO_RECV:
//                    pTask.offset = 0;
//                    remain_bytes = pClientInfo.total_length - pClientInfo.total_offset;
//                    if (remain_bytes > pTask.size) {
//                        pTask.length = pTask.size;
//                    } else {
//                        pTask.length = remain_bytes;
//                    }
//                    if (setRecvEvent(pTask) == 0) {
//                        clientSockRead(pTask -> event.fd,
//                                IOEVENT_READ, pTask);
//                    }
//                    result = 0;
//                    break;
//                case FDFS_STORAGE_STAGE_NIO_SEND:
//                    result = storageSendAddEvent(pTask);
//                    break;
//                case FDFS_STORAGE_STAGE_NIO_CLOSE:
//                    result = EIO;   //close this socket
//                    break;
//                default:
//                    logError("file: "__FILE__", line: %d, " \
//                            "invalid stage: %d", __LINE__, \
//                            pClientInfo -> stage);
//                    result = EINVAL;
//                    break;
//            }
//
//            if (result != 0) {
//                addToDeletedList(pTask);
//            }
//        }
//    }
//
//    static int storageNioInit(FastTaskInfo pTask) {
//        StorageClientInfo * pClientInfo;
//        struct storage_nio_thread_data *pThreadData;
//
//        pClientInfo = (StorageClientInfo *) pTask -> arg;
//        pThreadData = g_nio_thread_data + pClientInfo -> nio_thread_index;
//
//        pClientInfo -> stage = FDFS_STORAGE_STAGE_NIO_RECV;
//        return ioevent_set(pTask, & pThreadData -> thread_data,
//        pTask -> event.fd, IOEVENT_READ, client_sock_read,
//                g_fdfs_network_timeout);
//    }
//
//    static int setRecvEvent(FastTaskInfo pTask) {
//        int result;
//
//        if (pTask.event.callback == clientSockRead) {
//            return 0;
//        }
//
//        pTask.event.callback = client_sock_read;
//        if (ioevent_modify( & pTask -> thread_data -> ev_puller,
//        pTask -> event.fd, IOEVENT_READ, pTask) !=0)
//        {
//            result = errno != 0 ? errno : ENOENT;
//            add_to_deleted_list(pTask);
//
//            logError("file: "__FILE__", line: %d, "\
//                    "ioevent_modify fail, " \
//                    "errno: %d, error info: %s", \
//                    __LINE__, result, STRERROR(result));
//            return result;
//        }
//        return 0;
//    }
//
//    static void clientSockRead(int sock, short event, FastTaskInfo pTask) {
//        int bytes;
//        int recv_bytes;
//        StorageClientInfo pClientInfo = (StorageClientInfo) pTask.arg;
//        if (pClientInfo.canceled) {
//            return;
//        }
//
//        if (pClientInfo.stage != FDFS_STORAGE_STAGE_NIO_RECV) {
//            if (event & IOEVENT_TIMEOUT) {
//                pTask -> event.timer.expires = g_current_time +
//                        g_fdfs_network_timeout;
//                fast_timer_add( & pTask -> thread_data -> timer,
//                &pTask -> event.timer);
//            }
//
//            return;
//        }
//
//        if (event & IOEVENT_TIMEOUT) {
//            if (pClientInfo -> total_offset == 0 && pTask -> req_count > 0) {
//                pTask -> event.timer.expires = g_current_time +
//                        g_fdfs_network_timeout;
//                fast_timer_add( & pTask -> thread_data -> timer,
//				&pTask -> event.timer);
//            } else {
//                logError("file: "__FILE__", line: %d, " \
//                        "client ip: %s, recv timeout, " \
//                        "recv offset: %d, expect length: %d", \
//                        __LINE__, pTask -> client_ip, \
//                        pTask -> offset, pTask -> length);
//
//                task_finish_clean_up(pTask);
//            }
//
//            return;
//        }
//
//        if (event & IOEVENT_ERROR) {
//            logDebug("file: "__FILE__", line: %d, " \
//                    "client ip: %s, recv error event: %d, "
//                    "close connection", __LINE__, pTask -> client_ip, event);
//
//            task_finish_clean_up(pTask);
//            return;
//        }
//
//        fast_timer_modify( & pTask -> thread_data -> timer,
//		&pTask -> event.timer, g_current_time +
//                g_fdfs_network_timeout);
//        while (1) {
//            if (pClientInfo -> total_length == 0) //recv header
//            {
//                recv_bytes = sizeof(TrackerHeader) - pTask -> offset;
//            } else {
//                recv_bytes = pTask -> length - pTask -> offset;
//            }
//
//		/*
//		logInfo("total_length=%"PRId64", recv_bytes=%d, "
//			"pTask->length=%d, pTask->offset=%d",
//			pClientInfo->total_length, recv_bytes,
//			pTask->length, pTask->offset);
//		*/
//
//            bytes = recv(sock, pTask -> data + pTask -> offset, recv_bytes, 0);
//            if (bytes < 0) {
//                if (errno == EAGAIN || errno == EWOULDBLOCK) {
//                } else if (errno == EINTR) {
//                    continue;
//                } else {
//                    logError("file: "__FILE__", line: %d, " \
//                            "client ip: %s, recv failed, " \
//                            "errno: %d, error info: %s", \
//                            __LINE__, pTask -> client_ip, \
//                            errno, STRERROR(errno));
//
//                    task_finish_clean_up(pTask);
//                }
//
//                return;
//            } else if (bytes == 0) {
//                logDebug("file: "__FILE__", line: %d, " \
//                        "client ip: %s, recv failed, " \
//                        "connection disconnected.", \
//                        __LINE__, pTask -> client_ip);
//
//                task_finish_clean_up(pTask);
//                return;
//            }
//
//            if (pClientInfo -> total_length == 0) //header
//            {
//                if (pTask -> offset + bytes < sizeof(TrackerHeader)) {
//                    pTask -> offset += bytes;
//                    return;
//                }
//
//                pClientInfo -> total_length = buff2long(((TrackerHeader *) \
//                        pTask -> data)->pkg_len);
//                if (pClientInfo -> total_length < 0) {
//                    logError("file: "__FILE__", line: %d, " \
//                            "client ip: %s, pkg length: " \
//                            "%"PRId64" < 0", \
//                            __LINE__, pTask -> client_ip, \
//                            pClientInfo -> total_length);
//
//                    task_finish_clean_up(pTask);
//                    return;
//                }
//
//                pClientInfo -> total_length += sizeof(TrackerHeader);
//                if (pClientInfo -> total_length > pTask -> size) {
//                    pTask -> length = pTask -> size;
//                } else {
//                    pTask -> length = pClientInfo -> total_length;
//                }
//            }
//
//            pTask -> offset += bytes;
//            if (pTask -> offset >= pTask -> length) //recv current pkg done
//            {
//                if (pClientInfo -> total_offset + pTask -> length >= \
//                pClientInfo -> total_length)
//                {
//				/* current req recv done */
//                    pClientInfo -> stage = FDFS_STORAGE_STAGE_NIO_SEND;
//                    pTask -> req_count++;
//                }
//
//                if (pClientInfo -> total_offset == 0) {
//                    pClientInfo -> total_offset = pTask -> length;
//                    storage_deal_task(pTask);
//                } else {
//                    pClientInfo -> total_offset += pTask -> length;
//
//				/* continue write to file */
//                    storage_dio_queue_push(pTask);
//                }
//
//                return;
//            }
//        }
//
//        return;
//    }
//
//    /**
//     * @param pTask
//     * @return
//     */
//    public int storageSendAddEvent(Object pTask) {
//        return 0;
//    }
//
//    /**
//     * @param pTask
//     */
//    public void taskFinishCeanUp(Object pTask) {
//
//    }
//
//    /**
//     * @param pTask
//     */
//    public void addToDeletedList(Object pTask) {
//
//    }
//}
