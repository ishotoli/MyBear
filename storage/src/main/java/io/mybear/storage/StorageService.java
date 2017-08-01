package io.mybear.storage;

import com.alibaba.fastjson.JSON;
import io.mybear.common.SharedFunc;
import io.mybear.common.Stat;
import io.mybear.common.constants.CommonConstant;
import io.mybear.common.constants.SizeOfConstant;
import io.mybear.common.constants.config.FdfsGlobal;
import io.mybear.common.constants.config.StorageGlobal;
import io.mybear.common.trunk.FDFSTrunkHeader;
import io.mybear.common.trunk.FdfsTrunkFullInfo;
import io.mybear.common.trunk.FdfsTrunkPathInfo;
import io.mybear.common.trunk.TrunkShared;
import io.mybear.common.utils.*;
import io.mybear.storage.storageNio.ByteBufferArray;
import io.mybear.storage.storageNio.StorageClientInfo;
import io.mybear.storage.storageSync.StorageSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static io.mybear.common.constants.CommonConstant.FDFS_FILE_DIST_PATH_ROUND_ROBIN;
import static io.mybear.common.constants.TrackerProto.STORAGE_PROTO_CMD_RESP;
import static io.mybear.common.constants.TrackerProto.STORAGE_SET_METADATA_FLAG_OVERWRITE;
import static io.mybear.common.constants.config.StorageGlobal.g_storage_stat;
import static io.mybear.common.utils.BasicTypeConversionUtil.int2buff;
import static io.mybear.common.utils.BasicTypeConversionUtil.long2buff;
import static io.mybear.storage.FdfsStoraged.g_current_time;
import static io.mybear.storage.storageSync.StorageSync.STORAGE_OP_TYPE_SOURCE_CREATE_FILE;
import static io.mybear.storage.storageSync.StorageSync.STORAGE_OP_TYPE_SOURCE_UPDATE_FILE;
import static io.mybear.storage.StorageDio._FILE_TYPE_LINK;

/**
 * Created by jamie on 2017/6/21.
 */
public class StorageService {
    public static final String ACCESS_LOG_ACTION_UPLOAD_FILE = "upload";
    public static final String ACCESS_LOG_ACTION_DOWNLOAD_FILE = "download";
    public static final String ACCESS_LOG_ACTION_DELETE_FILE = "delete";
    public static final String ACCESS_LOG_ACTION_GET_METADATA = "get_metadata";
    public static final String ACCESS_LOG_ACTION_SET_METADATA = "set_metadata";
    public static final String ACCESS_LOG_ACTION_MODIFY_FILE = "modify";
    public static final String ACCESS_LOG_ACTION_APPEND_FILE = "append";
    public static final String ACCESS_LOG_ACTION_TRUNCATE_FILE = "truncate";
    public static final String ACCESS_LOG_ACTION_QUERY_FILE = "status";
    //文件路径分隔符
    public static final String FILE_SEPARATOR = "/";
    public static final int STORAGE_CREATE_FLAG_NONE = 0;
    public static final int STORAGE_CREATE_FLAG_FILE = 1;
    public static final int STORAGE_CREATE_FLAG_LINK = 2;
    public static final int STORAGE_DELETE_FLAG_NONE = 0;
    public static final int STORAGE_DELETE_FLAG_FILE = 1;
    public static final int STORAGE_DELETE_FLAG_LINK = 2;
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);
    private final static Logger log = LoggerFactory.getLogger(StorageService.class);
    private static final ReentrantLock lock = new ReentrantLock();
    private static final int STORAGE_STATUE_DEAL_FILE = 123456;

    public static void STORAGE_nio_notify(StorageClientInfo pTask) {

    }

    public static void STORAGE_accept_loop(int server_sock) {

    }

    public static void FDFS_PROTO_CMD_ACTIVE_TEST(StorageClientInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_DELETE_FILE(StorageClientInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static int storageProtoCmdSetMetadata(StorageClientInfo clientInfo) {
        StorageFileContext fileContext = clientInfo.fileContext;
        StorageSetMetaInfo setmeta = (StorageSetMetaInfo) clientInfo.extraArg;
        List<String[]> list = MetadataUtil.splitMetadata(setmeta.metaBuff);
        StringBuilder stringBuilder = new StringBuilder();
        fileContext.syncFlag = '\0';
        StringBuilder metaBuff = setmeta.metaBuff;
        int metaBytes = setmeta.meta_bytes;
        int result = 0;
        Path filename = Paths.get(fileContext.filename);
        try {
            do {
                if (setmeta.op_flag == STORAGE_SET_METADATA_FLAG_OVERWRITE) {
                    if (metaBuff.length() == 0) {
                        if (Files.notExists(filename)) {
                            result = 0;
                            break;
                        }
                        fileContext.syncFlag = StorageSync.STORAGE_OP_TYPE_SOURCE_DELETE_FILE;
                        if (!SharedFunc.delete(fileContext.filename)) {
                            LOGGER.error("client ip: %s, delete file %s fail", clientInfo.getChannel().getRemoteAddress(), fileContext.filename);
                            result = -1;
                        } else {
                            result = 0;
                        }
                        break;
                    }
                    if ((result = MetadataUtil.sortMetadataBuff(metaBuff)) != 0) {
                        break;
                    }
                    if (SharedFunc.fileExists(fileContext.filename)) {
                        fileContext.syncFlag = STORAGE_OP_TYPE_SOURCE_UPDATE_FILE;
                    } else {
                        fileContext.syncFlag = STORAGE_OP_TYPE_SOURCE_CREATE_FILE;
                    }
                    try {
                        Files.write(filename, metaBuff.toString().getBytes(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                        result = -1;
                    }
                    break;
                }
                if (metaBuff.length() == 0) {
                    result = 0;
                    break;
                }
                byte[] file_buff = null;
                if (Files.notExists(filename)) {
                    if (metaBuff.length() == 0) {
                        result = 0;
                        break;
                    }
                    if ((result = MetadataUtil.sortMetadataBuff(metaBuff)) != 0) {
                        break;
                    }
                    fileContext.syncFlag = STORAGE_OP_TYPE_SOURCE_CREATE_FILE;
                    Files.write(filename, metaBuff.toString().getBytes(), StandardOpenOption.APPEND);
                    break;
                } else {
                    try {
                        file_buff = Files.readAllBytes(filename);
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                List<String[]> old_meta_list = null;
                try {
                    old_meta_list = MetadataUtil.splitMetadata(new StringBuilder(new String(file_buff)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (old_meta_list == null || old_meta_list.size() == 0) {
                    break;
                }
                List<String[]> new_meta_list = null;
                try {
                    new_meta_list = MetadataUtil.splitMetadata(new StringBuilder(metaBuff));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (new_meta_list == null) {
                    break;
                }
                List<String[]> all_meta_list = null;
                int size = old_meta_list.size() + new_meta_list.size();
                try {
                    all_meta_list = new ArrayList<>(size);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (all_meta_list == null) {
                    LOGGER.error(String.format("malloc %d bytes fail.", size));
                    result = -1;
                    break;
                }
                new_meta_list.sort(MetadataUtil.comparator);
                int o = 0;
                int n = 0;
                int p = 0;
                int old_meta_count = old_meta_list.size();
                int new_meta_count = new_meta_list.size();
                List<String[]> pAllMeta = all_meta_list;
                while (o < old_meta_count && n < new_meta_count) {
                    String[] old = old_meta_list.get(o);
                    String[] ne = new_meta_list.get(o);
                    int b = old[0].length() - ne[0].length();
                    if (b < 0) {
                        pAllMeta.add(old);
                        o++;
                    } else if (b == 0) {
                        pAllMeta.add(old);
                        o++;
                        n++;
                    } else  //result > 0
                    {
                        pAllMeta.add(ne);
                        n++;
                    }
                }
                while (o < old_meta_count) {
                    pAllMeta.add(old_meta_list.get(o));
                    o++;
                }
                while (n < new_meta_count) {
                    pAllMeta.add(new_meta_list.get(n));
                    n++;
                }
                file_buff = null;
                old_meta_list = null;
                new_meta_list = null;
                StringBuilder all_meta_buff = MetadataUtil.packMetadata(all_meta_list, all_meta_list.size());
                all_meta_list = null;
                if (all_meta_buff == null) {
                    result = -1;
                    break;
                }
                fileContext.syncFlag = STORAGE_OP_TYPE_SOURCE_UPDATE_FILE;
                try {
                    Files.write(filename, all_meta_buff.toString().getBytes(), StandardOpenOption.APPEND);
                } catch (Exception e) {
                    result = -1;
                    e.printStackTrace();
                }
            } while (false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return storage_set_metadata_done_callback(clientInfo, result);
    }

    static void STORAGE_ACCESS_STRCPY_FNAME2LOG(String filename, StorageClientInfo pClientInfo) {

    }

    static void storage_log_access_log(StorageClientInfo clientInfo, String action, int status) {

    }

    static int storage_set_metadata_done_callback(StorageClientInfo con, int error) {
        StorageFileContext pFileContext = con.fileContext;
        int result = 0;
        if (error == 0) {
            if (pFileContext.syncFlag != '\0') {
                result = StorageSync.storage_binlog_write(pFileContext.timestamp2log, pFileContext.syncFlag, pFileContext.fname2log);
            } else {
                result = -1;
            }
        } else {
            result = -1;
        }
        if (result != 0) {
            g_storage_stat.total_set_meta_count.increment();
        } else {
            //CHECK_AND_WRITE_TO_STAT_FILE3
            g_storage_stat.total_set_meta_count.increment();
            g_storage_stat.success_set_meta_count.increment();
            g_storage_stat.last_source_update = g_current_time;
            StorageGlobal.g_stat_change_count.increment();
        }
        ByteBuffer byteBuffer = con.getMyBufferPool().allocateByteBuffer();
        ProtocolUtil.buildHeader(byteBuffer, 0, (byte) STORAGE_PROTO_CMD_RESP, result);
        con.write(byteBuffer);
        storage_log_access_log(con, ACCESS_LOG_ACTION_SET_METADATA, result);
        StorageDio.nioNotify(con);
        return 0;
    }

    public static void STORAGE_PROTO_CMD_DOWNLOAD_FILE(StorageClientInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_GET_METADATA(StorageClientInfo taskInfo, ByteBufferArray byteBufferArray) {
        String filename = null;
        STORAGE_ACCESS_STRCPY_FNAME2LOG(filename, taskInfo);
    }

    public static void STORAGE_PROTO_CMD_TRUNCATE_FILE(StorageClientInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_QUERY_FILE_INFO(StorageClientInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_UPLOAD_FILE(StorageClientInfo taskInfo, ByteBufferArray byteBufferArray) {
        //        long len = taskInfo.getOffset();
        //        System.out.println("len:" + len);
        //        System.out.println("taskInfo:" + taskInfo.getLength());
        //
        //        if (taskInfo.getLength() < (len)) {
        //            if (h == false) {
        //                h = true;
        //            } else {
        //                return;
        //            }
        //
        //            try {
        //                Random rand = new Random();
        //                Path file = Paths.get("d:/" + Integer.valueOf(rand.nextInt()).toString().substring(1, 4) +
        // ".jar");
        //                Files.createFile(file);
        //                ByteChannel byteChannel = Files.newByteChannel(file, StandardOpenOption.APPEND);
        //                ByteBuffer byteBuffer = byteBufferArray.getBlock(0);
        //                byteBuffer.limit(byteBuffer.position());
        //
        //                byteBuffer.position(25);
        //                byteChannel.write(byteBuffer);
        //                for (int i = 1; i < byteBufferArray.getBlockCount(); i++) {
        //                    byteBuffer = byteBufferArray.getBlock(i);
        //                    byteBuffer.flip();
        //                    byteChannel.write(byteBuffer);
        //                }
        //                byteChannel.close();
        //
        //////                ByteBufferArray r = taskInfo.getMyBufferPool().allocate();
        ////                ByteBuffer res = r.addNewBuffer();
        ////                res.clear();
        ////                res.position(8);
        ////                setStorageCMDResp(res);
        ////                res.position(9);
        ////                res.put((byte) 0);
        ////                setGroupName(res, "Hello");
        ////                setGroupName(res, file.toString());
        ////                int limit = res.position();
        ////                int pkgLen = limit - 10;
        ////                res.position(0);
        ////                res.putLong(0, pkgLen);
        ////                res.position(limit);
        ////                taskInfo.write(r);
        //
        //            } catch (Exception e) {
        //                e.printStackTrace();
        //            }
        //        }
    }

    public static void STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE(StorageClientInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_UPLOAD_APPENDER_FILE(StorageClientInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_APPEND_FILE(StorageClientInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    public static void STORAGE_PROTO_CMD_MODIFY_FILE(StorageClientInfo taskInfo, ByteBufferArray byteBufferArray) {

    }

    /**
     * 删除文件
     */
    public static void storageServerDeleteFile(StorageClientInfo pTask) {
        StorageClientInfo pClientInfo;
        StorageFileContext pFileContext;
        char[] p;
        FDFSTrunkHeader trunkHeader = null;
        char[] group_name = new char[CommonConstant.FDFS_GROUP_NAME_MAX_LEN];
        char[] true_filename = new char[128];
        char[] filename;
        int filename_len;
        int true_filename_len;
        Stat stat_buf = null;
        int result;
        int store_path_index;
        long nInPackLen;
        pClientInfo = pTask;
        pFileContext = pClientInfo.fileContext;

        nInPackLen = pClientInfo.totalLength - SizeOfConstant.SIZE_OF_TRACKER_HEADER;
        pClientInfo.totalLength = SizeOfConstant.SIZE_OF_TRACKER_HEADER;
        pFileContext.deleteFlag = STORAGE_DELETE_FLAG_NONE;
        if (nInPackLen <= CommonConstant.FDFS_GROUP_NAME_MAX_LEN) {
            log.error(CommonConstant.LOG_FORMAT, "storageServerDeleteFile", JSON.toJSONString(pTask),
                    "nInPackLen:" + nInPackLen + " < TrackerTypes.FDFS_GROUP_NAME_MAX_LEN "
                            + CommonConstant.FDFS_GROUP_NAME_MAX_LEN);
            return;
        }
        //@TODO size 和 data的值
        if (nInPackLen >= pTask.fileContext.end) {
            log.error(CommonConstant.LOG_FORMAT, "storageServerDeleteFile", JSON.toJSONString(pTask),
                    "nInPackLen:" + nInPackLen + " < pTask.size " + pTask.fileContext.end);
            return;
        }
        p = new char[(int) (pTask.getLength() + SizeOfConstant.SIZE_OF_TRACKER_HEADER)];
        //@TODO data的值
       // System.arraycopy(pTask.data, 0, p, 0, pTask.data.length);
        System.arraycopy(p, 0, group_name, 0, CommonConstant.FDFS_GROUP_NAME_MAX_LEN);
        String groupName = new String(group_name);
        if (!groupName.equals(StorageGlobal.g_group_name)) {
            log.error(CommonConstant.LOG_FORMAT, "storageServerDeleteFile", JSON.toJSONString(pTask),
                    String.format("client ip:%s, group_name: %s,not correct, should be: %s", pTask.getHost(), groupName, StorageGlobal.g_group_name));
            return;
        }
        filename = new char[p.length - CommonConstant.FDFS_GROUP_NAME_MAX_LEN];
        System.arraycopy(p, CommonConstant.FDFS_GROUP_NAME_MAX_LEN, filename, CommonConstant.FDFS_GROUP_NAME_MAX_LEN, p.length);
        filename_len = (int) (nInPackLen - CommonConstant.FDFS_GROUP_NAME_MAX_LEN);
        STORAGE_ACCESS_STRCPY_FNAME2LOG(filename, filename_len, pClientInfo);
        true_filename_len = filename_len;
        if ((store_path_index = FilenameUtil.storage_split_filename_ex(filename, true_filename_len, true_filename)) < 0) {
            log.error(CommonConstant.LOG_FORMAT, "storageServerDeleteFile", JSON.toJSONString(pTask), "获取文件名错误!");
            return;
        }
        true_filename_len -= 4;
        if ((result = FdfsGlobal.fdfs_check_data_filename(true_filename, true_filename_len)) != 0) {
            return;
        }
        char[] true_filename_bak = new char[true_filename_len];
        System.arraycopy(true_filename, 0, true_filename_bak, 0, true_filename_len);
        // 像这样的数据 CD/00/wKi0hVjqXGeAcyFfAAGSt-FxG-0872.jpg
        true_filename = true_filename_bak;
        StorageUploadInfo upload = (StorageUploadInfo) pFileContext.extra_info;
        if ((result = FilenameUtil.trunk_file_lstat(store_path_index, true_filename, true_filename_len, stat_buf,
                upload.getTrunkInfo(), trunkHeader)) != 0) {
            log.error(CommonConstant.LOG_FORMAT, "storageServerDeleteFile", JSON.toJSONString(pTask),
                    String.format("获取文件名错误! %s", new String(true_filename)));
            return;
        }
        if (StatUtil.S_ISREG(stat_buf.getSt_mode())) {
            upload.setFileType((char) StorageDio._FILE_TYPE_REGULAR);
            pFileContext.deleteFlag |= STORAGE_DELETE_FLAG_FILE;
        } else if (StatUtil.S_ISLNK(stat_buf.getSt_mode())) {
            upload.setFileType((char) StorageDio._FILE_TYPE_LINK);
            pFileContext.deleteFlag |= STORAGE_DELETE_FLAG_LINK;
        } else {
            log.error(CommonConstant.LOG_FORMAT, "storageServerDeleteFile", JSON.toJSONString(pTask),
                    String.format("client ip: %s, file %s is NOT a file", new String(pTask.getHost()),
                            pFileContext.filename));
            return;
        }
        if (FilenameUtil.IS_TRUNK_FILE_BY_ID(upload.getTrunkInfo())) {
            upload.setFileType((char) (upload.getFileType() | StorageDio._FILE_TYPE_TRUNK));
            pClientInfo.dealFunc = StorageDio::dio_delete_trunk_file;
            char[] file_name = pFileContext.filename.toCharArray();
            file_name = FilenameUtil.trunk_get_full_filename(upload.getTrunkInfo(), file_name,
                    pFileContext.filename.length());
            pFileContext.filename = new String(file_name);
        } else {
            pClientInfo.dealFunc = StorageDio::dio_delete_normal_file;
            pFileContext.filename = String.format("%s/data/%s",
                    TrunkShared.getFdfsStorePaths().getPaths()[store_path_index], true_filename);
        }
        if ((upload.getFileType() == _FILE_TYPE_LINK) && storage_is_slave_file(filename, filename_len)) {
            char[] full_filename = new char[CommonConstant.MAX_PATH_SIZE + 128];
            char[] src_filename = new char[CommonConstant.MAX_PATH_SIZE + 128];
            char[] src_fname2log = new char[128];
            char[] src_true_filename;
            int src_filename_len = 0;
            int base_path_len;
            int src_store_path_index;
            int i;
            String fullFileName = String.format("%s/data/%s", TrunkShared.fdfsStorePaths.getPaths()[store_path_index],
                    new String(true_filename));
            full_filename = fullFileName.toCharArray();
            do {
                File file = new File(fullFileName);
                try {
                    Files.readSymbolicLink(file.toPath()).toString();
                    //@TODO 这里有一段 根据文件软连接获取源文件 并删除源文件上面的 软连接的 内容
                    //                    if (unlink(src_filename) != 0) {
                    //                        result = errno != 0 ? errno : ENOENT;
                    //                        logWarning("file: "__FILE__", line: %d, "
                    //                                "client ip:%s, unlink file %s "
                    //                                "fail, errno: %d, error info: %s",
                    //                                __LINE__, pTask -> client_ip,
                    //                                src_filename, result, STRERROR(result));
                    //                        if (result == ENOENT) {
                    //                            break;
                    //                        }
                    //                        return ;
                    //                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error(CommonConstant.LOG_FORMAT, "storageServerDeleteFile", JSON.toJSONString(pTask),
                            String.format("client ip:%s, unlink file %s fail, errno: %d, error info: %s",
                                    new String(pTask.getHost()),
                                    fullFileName, result, e.getMessage()));
                    return;
                }
                base_path_len = TrunkShared.getFdfsStorePaths().getPaths()[store_path_index].length();
                if (src_filename_len > base_path_len && new String(src_filename, 0, base_path_len).equals(
                        TrunkShared.getFdfsStorePaths().getPaths()[store_path_index])) {
                    src_store_path_index = store_path_index;
                } else {
                    src_store_path_index = -1;
                    for (i = 0; i < TrunkShared.getFdfsStorePaths().getCount(); i++) {
                        base_path_len = TrunkShared.getFdfsStorePaths().getPaths()[i].length();
                        if (src_filename_len > base_path_len && new String(src_filename, 0, base_path_len).equals(
                                TrunkShared.getFdfsStorePaths().getPaths()[store_path_index])) {
                            src_store_path_index = i;
                            break;
                        }
                    }
                    if (src_store_path_index < 0) {
                        log.error(CommonConstant.LOG_FORMAT, "storageServerDeleteFile", JSON.toJSONString(pTask),
                                String.format("client ip:%s, can't get store base path of file %s",
                                        new String(pTask.getHost()), new String(src_filename)));
                        break;
                    }
                }
                //(sizeof("/data/") = 7
                src_true_filename = new char[src_filename.length - base_path_len - 5];
                System.arraycopy(src_filename, base_path_len + 5, src_true_filename, 0, src_true_filename.length);
                String fileNameBak = String.format("%c" + CommonConstant.FDFS_STORAGE_DATA_DIR_FORMAT + "/%s",
                        CommonConstant.FDFS_STORAGE_STORE_PATH_PREFIX_CHAR, src_store_path_index,
                        new String(src_true_filename));
                if (fileNameBak.length() > src_fname2log.length) {
                    System.arraycopy(fileNameBak.toCharArray(), 0, src_fname2log, 0, src_fname2log.length);
                } else {
                    src_fname2log = new char[fileNameBak.length()];
                    System.arraycopy(fileNameBak.toCharArray(), 0, src_fname2log, 0, src_fname2log.length);
                }
                //binlog先不处理
                //storage_binlog_write(g_current_time, STORAGE_OP_TYPE_SOURCE_DELETE_FILE, src_fname2log);
            } while (false);
        }
        //pFileContext.fname2log = filename;
        //return storage_do_delete_file(pTask, storage_delete_file_log_error, storage_delete_fdfs_file_done_callback,
        // store_path_index);
        return;
    }

    private static int storage_do_delete_file(StorageClientInfo pTask, final int store_path_index) {
        StorageClientInfo pClientInfo;
        StorageFileContext pFileContext;
        int result;
        pClientInfo = pTask;
        pFileContext = pClientInfo.fileContext;
        //pFileContext.;
        pFileContext.op = StorageDio.FDFS_STORAGE_FILE_OP_DELETE;
        //pFileContext.dio_thread_index = storage_dio_get_thread_index(pTask, store_path_index, pFileContext.op);
//        pFileContext.log_callback = log_callback;
//        pFileContext.done_callback = done_callback;
        //        if ((result = storage_dio_queue_push(pTask)) != 0) {
        //            return result;
        //        }
        pFileContext.log_callback = (logFile) -> {
//            log.info(CommonConstant.LOG_FORMAT, "storage_do_delete_file", JSON.toJSONString(logFile),
//                    String.format("client ip: %s, delete file %s fail ", new String(logFile.getClientIp()),
//                            logFile.file_context.filename));
        };
        pFileContext.done_callback = (done) -> {

        };
//        if ((result = storage_dio_queue_push(pTask)) != 0) {
//            return result;
//        }
        return STORAGE_STATUE_DEAL_FILE;
    }

    private static boolean storage_is_slave_file(final char[] remote_filename, final int filename_len) {
        char[] buff = new char[64];
        int file_size;
        if (filename_len < CommonConstant.FDFS_NORMAL_LOGIC_FILENAME_LENGTH) {
            log.error(CommonConstant.LOG_FORMAT, "storage_is_slave_file", "",
                    String.format("filename is too short, length: %d < %d",
                            filename_len, CommonConstant.FDFS_LOGIC_FILE_PATH_LEN + CommonConstant.FDFS_FILENAME_BASE64_LENGTH
                                    + FdfsGlobal.FDFS_FILE_EXT_NAME_MAX_LEN + 1));
            return false;
        }
        Arrays.fill(buff, (char) 0);
        char[] file_name_bak = new char[remote_filename.length - CommonConstant.FDFS_LOGIC_FILE_PATH_LEN];
        System.arraycopy(remote_filename, CommonConstant.FDFS_LOGIC_FILE_PATH_LEN, file_name_bak, 0,
                file_name_bak.length);
        buff = Base64.base64_decode_auto(TrunkShared.base64Context, file_name_bak,
                CommonConstant.FDFS_FILENAME_BASE64_LENGTH, buff);
        file_size = (int) BasicTypeConversionUtil.buff2long(buff, SizeOfConstant.SIZE_OF_INT * 2);
        if (Utils.IS_TRUNK_FILE(file_size)) {
            return filename_len > CommonConstant.FDFS_TRUNK_LOGIC_FILENAME_LENGTH;
        }
        return filename_len > CommonConstant.FDFS_NORMAL_LOGIC_FILENAME_LENGTH;
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

    /**
     * 获取文件名
     *
     * @param pClientInfo
     * @param fileSize
     * @param crc32
     * @param
     * @return
     */
    public static String storageGetFilename(StorageClientInfo pClientInfo, int startTime, long fileSize, int crc32, char[] szFormattedExt) {
        int fileNameLen;
        int storePathIndex = ((StorageUploadInfo) pClientInfo.fileContext.extra_info).getTrunkInfo().getPath()
                .getStorePathIndex();
        String filePathName = null;
        for (int i = 0; i < 10; i++) {
            String fileName;
            if ("".equals(fileName = storageGenFilename(pClientInfo, fileSize, crc32, szFormattedExt, startTime))) {
                return "";
            }
            if (storePathIndex > TrunkShared.fdfsStorePaths.getCount()) {
                log.error("method={},params={},result={}", "storageGetFilename", storePathIndex + " " + fileName,
                        "storePathIndex的值错误");
                return "";
            }
            if (storePathIndex == TrunkShared.fdfsStorePaths.getCount()) {
                filePathName = String.format("%s/data/%s", TrunkShared.fdfsStorePaths.getPaths()[storePathIndex - 1], new String(fileName));
            }
            if (storePathIndex < TrunkShared.fdfsStorePaths.getCount()) {
                filePathName = String.format("%s/data/%s", TrunkShared.fdfsStorePaths.getPaths()[storePathIndex], new String(fileName));
            }
            if (!SharedFunc.fileExists(filePathName)) {

                break;
            }
            filePathName = "";
        }
        if (filePathName == null || "".equals(filePathName)) {
            log.error("method={},params={},result={}", "storageGetFilename", storePathIndex + " ",
                    "Can't generate uniq filename");
            return "";
        }
        return filePathName;
    }

    /**
     * 生成文件名 storage_service#storage_gen_filename
     *
     * @param pClientInfo
     * @param fileSize
     * @param crc32
     * @return
     */
    public static String storageGenFilename(StorageClientInfo pClientInfo, long fileSize,
                                            int crc32, char[] szFormattedExt, int timeStamp) {
        try {
            int fileNameLen;
            char[] buff = new char[SizeOfConstant.SIZE_OF_INT * 5];
            char[] encoded = new char[SizeOfConstant.SIZE_OF_INT * 8 + 1];
            long maskedFileSize = 0L;
            StorageUploadInfo storageUploadInfo = (StorageUploadInfo) pClientInfo.fileContext.extra_info;
            FdfsTrunkFullInfo pTrunkInfo = storageUploadInfo.getTrunkInfo();
            //@TODO 这里需要做 g_server_id_in_filename的取值 和 htonl的转换
            //int2buff(htonl(g_server_id_in_filename),buff);
            int2buff(0, buff);
            int2buff(timeStamp, buff, SizeOfConstant.SIZE_OF_INT);
            if ((fileSize >> 32) != 0) {
                maskedFileSize = fileSize;
            } else {
                maskedFileSize = combineRandFileSize(fileSize, maskedFileSize);
            }
            long2buff(maskedFileSize, buff, SizeOfConstant.SIZE_OF_INT * 2);
            int2buff(crc32, buff, SizeOfConstant.SIZE_OF_INT * 4);
            //需要定义一个全局的Base64Context
            fileNameLen = Base64.base64EncodeEx(TrunkShared.base64Context, buff, SizeOfConstant.SIZE_OF_INT * 5,
                    encoded, false);
            if (fileNameLen < 0) {
                return "";
            }
            if (!storageUploadInfo.isIfSubPathAlloced()) {
                storageGetStorePath(encoded, fileNameLen, pTrunkInfo.getPath());
                storageUploadInfo.setIfSubPathAlloced(true);
            }
            char[] fileNewName = (String.format("%02X", pTrunkInfo.getPath().getSubPathHigh()) + FILE_SEPARATOR + String.format("%02X", pTrunkInfo.getPath().getSubPathLow())
                    + FILE_SEPARATOR).toCharArray();
            int fileLen = fileNewName.length;
            int flag = 0;
            if (fileNameLen > encoded.length) {
                flag = fileNewName.length;
                fileNewName = Arrays.copyOf(fileNewName, flag + encoded.length);
                System.arraycopy(encoded, 0, fileNewName, flag, encoded.length);
            } else {
                flag = fileNewName.length;
                fileNewName = Arrays.copyOf(fileNewName, flag + fileNameLen);
                System.arraycopy(encoded, 0, fileNewName, flag, fileNameLen);
                int len = FdfsGlobal.FDFS_FILE_EXT_NAME_MAX_LEN + 1;
                if (szFormattedExt.length > len) {
                    fileNewName = Arrays.copyOf(fileNewName, flag + fileNameLen + len);
                    System.arraycopy(szFormattedExt, 0, fileNewName, flag + fileNameLen, len);
                } else {
                    fileNewName = Arrays.copyOf(fileNewName, flag + fileNameLen + szFormattedExt.length);
                    System.arraycopy(szFormattedExt, 0, fileNewName, flag + fileNameLen, szFormattedExt.length);
                }
            }
            fileNameLen += fileLen + FdfsGlobal.FDFS_FILE_EXT_NAME_MAX_LEN + 1;
            return new String(fileNewName);
        } catch (Exception e) {
            log.error(CommonConstant.LOG_FORMAT, "storageGenFilename",
                    "pFileContext:" + JSON.toJSONString(pClientInfo) + " fileSize "
                            + fileSize + " crc32 " + crc32 + " szFormattedExt " + szFormattedExt
                            + " timeStamp " + timeStamp, "e{}" + e);
            return "";
        }
    }

    private static long combineRandFileSize(long size, long maskedFileSize) {

        int r = (RandomUtil.randomFixedInt() & 0x007FFFFF) | 0x80000000;
        maskedFileSize = (((long) r) << 32) | size;
        return maskedFileSize;
    }

    /**
     * 获取文件路径
     *
     * @param filename
     * @param fileNameLen
     * @param trunkPathInfo
     */
    private static void storageGetStorePath(char[] filename, int fileNameLen, FdfsTrunkPathInfo trunkPathInfo) {
        int n;
        if (StorageGlobal.g_file_distribute_path_mode == FDFS_FILE_DIST_PATH_ROUND_ROBIN) {
            trunkPathInfo.setSubPathHigh(StorageGlobal.g_dist_path_index_high);
            trunkPathInfo.setSubPathLow(StorageGlobal.g_dist_path_index_low);
            if (++StorageGlobal.g_dist_write_file_count >= StorageGlobal.g_file_distribute_rotate_count) {
                StorageGlobal.g_dist_write_file_count = 0;
                lock.lock();
                try {
                    ++StorageGlobal.g_dist_path_index_low;
                    if (StorageGlobal.g_dist_path_index_low >= StorageGlobal.g_subdir_count_per_path) {  //rotate
                        StorageGlobal.g_dist_path_index_high++;
                        if (StorageGlobal.g_dist_path_index_high >= StorageGlobal.g_subdir_count_per_path)  //rotate
                        {
                            StorageGlobal.g_dist_path_index_high = 0;
                        }
                        StorageGlobal.g_dist_path_index_low = 0;
                    }
                    StorageGlobal.g_stat_change_count.increment();
                } finally {
                    lock.unlock();
                }
            }
        }  //random
        else {
            n = HashUtil.PJWHash(filename, fileNameLen) % (1 << 16);
            trunkPathInfo.setSubPathHigh(((n >> 8) & 0xFF) % StorageGlobal.g_subdir_count_per_path);
            trunkPathInfo.setSubPathLow((n & 0xFF) % StorageGlobal.g_subdir_count_per_path);
        }
    }

    public static void main(String[] args) {
        int c = 200;
        System.out.println(String.format("%02X", c));
    }

    int storageServiceInit() {
        return 0;
    }

    void storageServiceDestroy() {

    }

    int fdfsStatFileSyncFunc(Object args) {
        return 0;
    }

    int storageDealTask(StorageClientInfo pTask) {
        return 0;
    }

    int storageTerminateThreads() {
        return 0;
    }

    int storageGetStoragePathIndex(int[] store_path_index) {
        return 0;
    }

    void storageGetStorePath(final Path filename, final int filename_len, int[] sub_path_high, int[] sub_path_low) {

    }

}
