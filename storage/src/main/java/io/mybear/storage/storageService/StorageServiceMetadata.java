package io.mybear.storage.storageService;

import io.mybear.common.constants.config.StorageGlobal;
import io.mybear.common.trunk.TrunkShared;
import io.mybear.common.utils.*;
import io.mybear.storage.StorageDio;
import io.mybear.storage.StorageFileContext;
import io.mybear.storage.StorageSetMetaInfo;
import io.mybear.storage.StorageUploadInfo;
import io.mybear.storage.storageNio.StorageClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static io.mybear.common.constants.CommonConstant.FDFS_STORAGE_META_FILE_EXT;
import static io.mybear.common.constants.TrackerProto.STORAGE_PROTO_CMD_RESP;
import static io.mybear.common.constants.TrackerProto.STORAGE_SET_METADATA_FLAG_OVERWRITE;
import static io.mybear.common.constants.config.StorageGlobal.g_check_file_duplicate;
import static io.mybear.common.constants.config.StorageGlobal.g_storage_stat;
import static io.mybear.storage.FdfsStoraged.g_current_time;
import static io.mybear.storage.storageService.StorageService.ACCESS_LOG_ACTION_SET_METADATA;
import static io.mybear.storage.storageService.StorageServiceHelper.storage_log_access_log;
import static io.mybear.storage.storageSync.StorageSync.*;

/**
 * Created by jamie on 2017/8/2.
 */
public class StorageServiceMetadata {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageServiceMetadata.class);

    public static int storage_do_set_metadata(StorageClientInfo clientInfo) {
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
                        fileContext.syncFlag = STORAGE_OP_TYPE_SOURCE_DELETE_FILE;
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

    public static int storage_set_metadata_done_callback(StorageClientInfo con, int error) {
        StorageFileContext pFileContext = con.fileContext;
        int result = 0;
        if (error == 0) {
            if (pFileContext.syncFlag != '\0') {
                result = storage_binlog_write(pFileContext.timestamp2log, pFileContext.syncFlag, pFileContext.fname2log);
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


    public static int storage_do_delete_meta_file(StorageClientInfo con) {
        StorageFileContext pFileContext = con.fileContext;
        StorageUploadInfo upload = (StorageUploadInfo) pFileContext.extra_info;
//        GroupArray * pGroupArray;
//        char meta_filename[ MAX_PATH_SIZE + 256];

//        char value[ 128];
//        FDHTKeyInfo key_info_fid;
//        FDHTKeyInfo key_info_ref;
//        FDHTKeyInfo key_info_sig;
//    char *pValue;
        int value_len;
        int src_file_nlink;
        int result;
        int store_path_index;
        String true_filename;
        String meta_filename;


        if (upload.isTRUNK()) {
            //@todo 校验文件名长度
            // int filename_len = strlen(pFileContext -> fname2log);
            FilenameResultEx res = StringUtil.storage_split_filename_ex(pFileContext.fname2log);
            store_path_index = res.storePathIndex;
            true_filename = res.true_filename;
            res = null;
            meta_filename = String.format("%s/data/%s" + FDFS_STORAGE_META_FILE_EXT, TrunkShared.getFdfsStorePaths().getPaths()[store_path_index], true_filename);
        } else {
            meta_filename = String.format("%s" + FDFS_STORAGE_META_FILE_EXT, pFileContext.filename);
        }
        if (SharedFunc.fileExists(meta_filename)) {
            if (!SharedFunc.unlink(meta_filename)) {
                LOGGER.error("client ip: %s, delete file %s fail", con.getHost(), meta_filename);
//                if (errno != ENOENT) {
//                    result = errno != 0 ? errno : EACCES;
//                    logError("file: "__FILE__", line: %d, " \
//                            "client ip: %s, delete file %s fail," \
//                            "errno: %d, error info: %s", __LINE__,\
//                            pTask -> client_ip, meta_filename, \
//                            result, STRERROR(result));
                return -1;
            }
        } else {
            meta_filename = String.format("%s" + FDFS_STORAGE_META_FILE_EXT, pFileContext.fname2log);
            result = storage_binlog_write(g_current_time, STORAGE_OP_TYPE_SOURCE_DELETE_FILE, meta_filename);
            if (result != 0) {
                return result;
            }
        }


        src_file_nlink = -1;
        if (g_check_file_duplicate) {
//            pGroupArray=&((g_nio_thread_data+pClientInfo->nio_thread_index)\
//				->group_array);
//            memset(&key_info_sig, 0, sizeof(key_info_sig));
//            key_info_sig.namespace_len = g_namespace_len;
//            memcpy(key_info_sig.szNameSpace, g_key_namespace, \
//                    g_namespace_len);
//            key_info_sig.obj_id_len = snprintf(\
//                    key_info_sig.szObjectId, \
//                    sizeof(key_info_sig.szObjectId), "%s/%s", \
//                    g_group_name, pFileContext->fname2log);
//
//            key_info_sig.key_len = sizeof(FDHT_KEY_NAME_FILE_SIG)-1;
//            memcpy(key_info_sig.szKey, FDHT_KEY_NAME_FILE_SIG, \
//                    key_info_sig.key_len);
//            pValue = value;
//            value_len = sizeof(value) - 1;
//            result = fdht_get_ex1(pGroupArray, g_keep_alive, \
//                    &key_info_sig, FDHT_EXPIRES_NONE, \
//				&pValue, &value_len, malloc);
//            if (result == 0)
//            {
//                memcpy(&key_info_fid, &key_info_sig, \
//                sizeof(FDHTKeyInfo));
//                key_info_fid.obj_id_len = value_len;
//                memcpy(key_info_fid.szObjectId, pValue, \
//                        value_len);
//
//                key_info_fid.key_len = sizeof(FDHT_KEY_NAME_FILE_ID) - 1;
//                memcpy(key_info_fid.szKey, FDHT_KEY_NAME_FILE_ID, \
//                        key_info_fid.key_len);
//                value_len = sizeof(value) - 1;
//                result = fdht_get_ex1(pGroupArray, \
//                        g_keep_alive, &key_info_fid, \
//                FDHT_EXPIRES_NONE, &pValue, \
//					&value_len, malloc);
//                if (result == 0)
//                {
//                    memcpy(&key_info_ref, &key_info_sig, \
//                    sizeof(FDHTKeyInfo));
//                    key_info_ref.obj_id_len = value_len;
//                    memcpy(key_info_ref.szObjectId, pValue,
//                            value_len);
//                    key_info_ref.key_len = \
//                    sizeof(FDHT_KEY_NAME_REF_COUNT)-1;
//                    memcpy(key_info_ref.szKey, \
//                            FDHT_KEY_NAME_REF_COUNT, \
//                            key_info_ref.key_len);
//                    value_len = sizeof(value) - 1;
//
//                    result = fdht_get_ex1(pGroupArray, \
//                            g_keep_alive, &key_info_ref, \
//                    FDHT_EXPIRES_NONE, &pValue, \
//						&value_len, malloc);
//                    if (result == 0)
//                    {
//					*(pValue + value_len) = '\0';
//                        src_file_nlink = atoi(pValue);
//                    }
//                    else if (result != ENOENT)
//                    {
//                        logError("file: "__FILE__", line: %d, " \
//                                "client ip: %s, fdht_get fail," \
//                                "errno: %d, error info: %s", \
//                                __LINE__, pTask->client_ip, \
//                                result, STRERROR(result));
//                        return result;
//                    }
//                }
//                else if (result != ENOENT)
//                {
//                    logError("file: "__FILE__", line: %d, " \
//                            "client ip: %s, fdht_get fail," \
//                            "errno: %d, error info: %s", \
//                            __LINE__, pTask->client_ip, \
//                            result, STRERROR(result));
//                    return result;
//                }
//            }
//            else if (result != ENOENT)
//            {
//                logError("file: "__FILE__", line: %d, " \
//                        "client ip: %s, fdht_get fail," \
//                        "errno: %d, error info: %s", \
//                        __LINE__, pTask->client_ip, \
//                        result, STRERROR(result));
//                return result;
//            }
        }

        if (src_file_nlink < 0) {
            return 0;
        }

        if (g_check_file_duplicate) {
//		char *pSeperator;
//            struct stat stat_buf;
//            FDFSTrunkHeader trunkHeader;
//
//            pGroupArray=&((g_nio_thread_data+pClientInfo->nio_thread_index)\
//				->group_array);
//            if ((result=fdht_delete_ex(pGroupArray, g_keep_alive, \
//                    &key_info_sig)) != 0)
//            {
//                logWarning("file: "__FILE__", line: %d, " \
//                        "client ip: %s, fdht_delete fail," \
//                        "errno: %d, error info: %s", \
//                        __LINE__, pTask->client_ip, \
//                        result, STRERROR(result));
//            }
//
//            value_len = sizeof(value) - 1;
//            result = fdht_inc_ex(pGroupArray, g_keep_alive, \
//                    &key_info_ref, FDHT_EXPIRES_NEVER, -1, \
//            value, &value_len);
//            if (result != 0)
//            {
//                logWarning("file: "__FILE__", line: %d, " \
//                        "client ip: %s, fdht_inc fail," \
//                        "errno: %d, error info: %s", \
//                        __LINE__, pTask->client_ip, \
//                        result, STRERROR(result));
//                return result;
//            }
//
//            if (!(value_len == 1 && *value == '0')) //value == 0
//            {
//                return 0;
//            }
//
//            if ((result=fdht_delete_ex(pGroupArray, g_keep_alive, \
//                    &key_info_fid)) != 0)
//            {
//                logWarning("file: "__FILE__", line: %d, " \
//                        "client ip: %s, fdht_delete fail," \
//                        "errno: %d, error info: %s", \
//                        __LINE__, pTask->client_ip, \
//                        result, STRERROR(result));
//            }
//            if ((result=fdht_delete_ex(pGroupArray, g_keep_alive, \
//                    &key_info_ref)) != 0)
//            {
//                logWarning("file: "__FILE__", line: %d, " \
//                        "client ip: %s, fdht_delete fail," \
//                        "errno: %d, error info: %s", \
//                        __LINE__, pTask->client_ip, \
//                        result, STRERROR(result));
//            }
//
//		*(key_info_ref.szObjectId+key_info_ref.obj_id_len)='\0';
//            pSeperator = strchr(key_info_ref.szObjectId, '/');
//            if (pSeperator == NULL)
//            {
//                logWarning("file: "__FILE__", line: %d, " \
//                        "invalid file_id: %s", __LINE__, \
//                        key_info_ref.szObjectId);
//                return 0;
//            }
//
//            pSeperator++;
//            value_len = key_info_ref.obj_id_len - (pSeperator - \
//            key_info_ref.szObjectId);
//            memcpy(value, pSeperator, value_len + 1);
//            if ((result=storage_split_filename_ex(value, &value_len, \
//            true_filename, &store_path_index)) != 0)
//            {
//                return result;
//            }
//            if ((result=fdfs_check_data_filename(true_filename, \
//                    value_len)) != 0)
//            {
//                return result;
//            }
//
//            if ((result=trunk_file_lstat(store_path_index, true_filename, \
//                    value_len, &stat_buf, \
//			&(pFileContext->extra_info.upload.trunk_info), \
//			&trunkHeader)) != 0)
//            {
//                STORAGE_STAT_FILE_FAIL_LOG(result, pTask->client_ip,
//                        "logic", value)
//                return 0;
//            }
//
//            if (IS_TRUNK_FILE_BY_ID(pFileContext->extra_info. \
//                    upload.trunk_info))
//            {
//                trunk_get_full_filename(&(pFileContext->extra_info. \
//                upload.trunk_info), pFileContext->filename, \
//                sizeof(pFileContext->filename));
//            }
//            else
//            {
//                sprintf(pFileContext->filename, "%s/data/%s", \
//                        g_fdfs_store_paths.paths[store_path_index], \
//                        true_filename);
//            }
//
//            if ((result=storage_delete_file_auto(pFileContext)) != 0)
//            {
//                logWarning("file: "__FILE__", line: %d, " \
//                        "client ip: %s, delete logic source file " \
//                        "%s fail, errno: %d, error info: %s", \
//                        __LINE__, pTask->client_ip, \
//                        value, errno, STRERROR(errno));
//                return 0;
//            }
//
//            storage_binlog_write(g_current_time, \
//                    STORAGE_OP_TYPE_SOURCE_DELETE_FILE, value);
//            pFileContext->delete_flag |= STORAGE_DELETE_FLAG_FILE;
        }

        return 0;
    }



}
