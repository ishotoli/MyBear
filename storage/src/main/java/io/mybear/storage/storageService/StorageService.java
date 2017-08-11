package io.mybear.storage.storageService;

import com.alibaba.fastjson.JSON;
import io.mybear.common.Stat;
import io.mybear.common.constants.CommonConstant;
import io.mybear.common.constants.SizeOfConstant;
import io.mybear.common.constants.config.FdfsGlobal;
import io.mybear.common.constants.config.StorageGlobal;
import io.mybear.common.trunk.FDFSTrunkHeader;
import io.mybear.common.trunk.TrunkShared;
import io.mybear.common.utils.*;
import io.mybear.storage.StorageDio;
import io.mybear.storage.StorageFileContext;
import io.mybear.storage.StorageUploadInfo;
import io.mybear.storage.storageNio.StorageClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

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
    private final static Logger log = LoggerFactory.getLogger(StorageService.class);

    private static final int STORAGE_STATUE_DEAL_FILE = 123456;


    /**
     * 删除文件
     */
    public static void storageServerDeleteFile(StorageClientInfo pTask) {
        StorageClientInfo pClientInfo;
        StorageFileContext pFileContext;
        char[] p;
        FDFSTrunkHeader trunkHeader = null;
        byte[] group_name = new byte[CommonConstant.FDFS_GROUP_NAME_MAX_LEN];
        byte[] true_filename = new byte[128];
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
                    String.format("client ip:%s, groupName: %s,not correct, should be: %s", pTask.getHost(), groupName, StorageGlobal.g_group_name));
            return;
        }
        filename = new char[p.length - CommonConstant.FDFS_GROUP_NAME_MAX_LEN];
        System.arraycopy(p, CommonConstant.FDFS_GROUP_NAME_MAX_LEN, filename, CommonConstant.FDFS_GROUP_NAME_MAX_LEN, p.length);
        filename_len = (int) (nInPackLen - CommonConstant.FDFS_GROUP_NAME_MAX_LEN);
        STORAGE_ACCESS_STRCPY_FNAME2LOG(new String(filename), pClientInfo);
        true_filename_len = filename_len;
        if ((store_path_index = StringUtil.storage_split_filename_ex(filename, true_filename_len, StringUtil.byte2char(true_filename))) < 0) {
            log.error(CommonConstant.LOG_FORMAT, "storageServerDeleteFile", JSON.toJSONString(pTask), "获取文件名错误!");
            return;
        }
        true_filename_len -= 4;
        if ((result = FdfsGlobal.fdfs_check_data_filename(StringUtil.byte2char(true_filename), true_filename_len)) != 0) {
            return;
        }
        char[] true_filename_bak = new char[true_filename_len];
        System.arraycopy(true_filename, 0, true_filename_bak, 0, true_filename_len);
        // 像这样的数据 CD/00/wKi0hVjqXGeAcyFfAAGSt-FxG-0872.jpg
        true_filename = StringUtil.char2byte(true_filename_bak);
        StorageUploadInfo upload = (StorageUploadInfo) pFileContext.extra_info;
        if ((result = TrunkShared.trunk_file_lstat(store_path_index, true_filename, true_filename_len, stat_buf,
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
        if (TrunkShared.IS_TRUNK_FILE_BY_ID(upload.getTrunkInfo())) {
            upload.setFileType((char) (upload.getFileType() | StorageDio._FILE_TYPE_TRUNK));
            pClientInfo.dealFunc = StorageDio::dio_delete_trunk_file;
            char[] file_name = pFileContext.filename.toCharArray();
            file_name = TrunkShared.trunk_get_full_filename(upload.getTrunkInfo(), file_name,
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


    public static void main(String[] args) {
        int c = 200;
        System.out.println(String.format("%02X", c));
    }

    static void STORAGE_ACCESS_STRCPY_FNAME2LOG(String filename, StorageClientInfo pClientInfo) {

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
