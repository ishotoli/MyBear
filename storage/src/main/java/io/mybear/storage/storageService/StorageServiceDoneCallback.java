package io.mybear.storage.storageService;


import io.mybear.common.constants.SizeOfConstant;
import io.mybear.common.utils.ProtocolUtil;
import io.mybear.common.utils.SharedFunc;
import io.mybear.storage.StorageFileContext;
import io.mybear.storage.StorageUploadInfo;
import io.mybear.storage.TrunkClient;
import io.mybear.storage.storageNio.StorageClientInfo;
import io.mybear.storage.storageSync.StorageSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static io.mybear.common.constants.CommonConstant.*;
import static io.mybear.common.constants.ErrorNo.EEXIST;
import static io.mybear.common.constants.ErrorNo.ENOENT;
import static io.mybear.common.constants.config.StorageGlobal.g_group_name;
import static io.mybear.common.constants.config.StorageGlobal.g_storage_stat;
import static io.mybear.storage.FdfsStoraged.g_current_time;
import static io.mybear.storage.StorageDio.*;
import static io.mybear.storage.storageService.StorageService.*;
import static io.mybear.storage.storageService.StorageServiceHelper.*;
import static io.mybear.storage.storageSync.StorageSync.*;

/**
 * Created by jamie on 2017/8/1.
 **/
public class StorageServiceDoneCallback {
    public static final Logger LOGGER = LoggerFactory.getLogger(StorageServiceDoneCallback.class);

    /**
     * storage_sync_delete_file_done_callback
     *
     * @param pClientInfo
     * @param err_no
     * @throws IOException
     */
    public static void storage_sync_delete_file_done_callback(StorageClientInfo pClientInfo, final byte err_no) throws IOException {
        StorageFileContext pFileContext = pClientInfo.fileContext;
        int result = 0;
        if (err_no == 0 && pFileContext.syncFlag != '0') {
            result = storage_binlog_write(pFileContext.timestamp2log, pFileContext.syncFlag, pFileContext.fname2log);
        } else {
            result = err_no;
        }
        if (result == 0) {
            CHECK_AND_WRITE_TO_STAT_FILE1(pClientInfo);
        }
        pClientInfo.write(StorageServiceHelper.getHeadSetStateFromBufferPool(pClientInfo, result));
    }

    /**
     * storage_sync_truncate_file_done_callback
     *
     * @param pClientInfo
     * @param err_no
     */
    public static void storage_sync_truncate_file_done_callback(StorageClientInfo pClientInfo, final int err_no) {
        StorageFileContext pFileContext = pClientInfo.fileContext;
        int result = 0;
        if (err_no == 0 && pFileContext.syncFlag != '0') {
            StorageServiceHelper.set_file_utimes(pFileContext.filename, pFileContext.timestamp2log);
            result = storage_binlog_write(pFileContext.timestamp2log, pFileContext.syncFlag, pFileContext.fname2log);
        } else {
            result = err_no;
        }
        if (result == 0) {
            CHECK_AND_WRITE_TO_STAT_FILE1(pClientInfo);
        }
        pClientInfo.write(StorageServiceHelper.getHeadSetStateFromBufferPool(pClientInfo, result));
    }

    /**
     * storage_sync_copy_file_done_callback
     *
     * @param pClientInfo
     * @param err_no
     */
    public static void storage_sync_copy_file_done_callback(StorageClientInfo pClientInfo, final int err_no) {
        StorageFileContext pFileContext = pClientInfo.fileContext;
        int result = 0;
        result = err_no;
        if (result == 0) {
            if (pFileContext.op == FDFS_STORAGE_FILE_OP_WRITE) {
                if (!((((StorageUploadInfo) pFileContext.extra_info).getFileType() &
                        _FILE_TYPE_TRUNK) == _FILE_TYPE_TRUNK)) {
                    set_file_utimes(pFileContext.filename, pFileContext.timestamp2log);
                    result = storage_sync_copy_file_rename_filename(pFileContext);
                }
                if (result == 0) {
                    storage_binlog_write(pFileContext.timestamp2log, pFileContext.syncFlag, pFileContext.fname2log);
                }
            } else { //FDFS_STORAGE_FILE_OP_DISCARD
                storage_binlog_write(pFileContext.timestamp2log, pFileContext.syncFlag, pFileContext.fname2log);
            }
        }
        if (pFileContext.op == FDFS_STORAGE_FILE_OP_WRITE) {
            if (result == 0) {
                CHECK_AND_WRITE_TO_STAT_FILE1_WITH_BYTES(g_storage_stat.total_sync_in_bytes, g_storage_stat.success_sync_in_bytes, pFileContext.end - pFileContext.start);
            }
        } else { //FDFS_STORAGE_FILE_OP_DISCARD
            if (result == 0) {
                CHECK_AND_WRITE_TO_STAT_FILE1(pClientInfo);
            }
            result = EEXIST;
        }
        if (result != 0) {
            g_storage_stat.total_sync_in_bytes.add(pClientInfo.totalOffset);
        }
        pClientInfo.write(StorageServiceHelper.getHeadSetStateFromBufferPool(pClientInfo, result));
    }

    /**
     * storage_sync_modify_file_done_callback
     *
     * @param pClientInfo
     * @param err_no
     */
    public static void storage_sync_modify_file_done_callback(StorageClientInfo pClientInfo, final int err_no) {
        StorageFileContext pFileContext = pClientInfo.fileContext;
        int result = 0;
        result = err_no;
        if (pFileContext.op != FDFS_STORAGE_FILE_OP_DISCARD) {
            if (result == 0) {
                set_file_utimes(pFileContext.filename, pFileContext.timestamp2log);
                storage_binlog_write(pFileContext.timestamp2log, pFileContext.syncFlag, pFileContext.fname2log);
                CHECK_AND_WRITE_TO_STAT_FILE1_WITH_BYTES(g_storage_stat.total_sync_in_bytes, g_storage_stat.success_sync_in_bytes, pFileContext.end - pFileContext.start);
            }
        } else { //FDFS_STORAGE_FILE_OP_DISCARD
            if (result == 0) {
                File file = new File(pFileContext.filename);
                if (!file.exists()) {
                    if (result == 0) {
                        result = ENOENT;
                    }
                    STORAGE_STAT_FILE_FAIL_LOG(result, pClientInfo.getHost(), "regular", pFileContext.filename);
                } else if (!file.isFile()) {
                    result = EEXIST;
                } else if (file.length() < pFileContext.end) {
                    result = ENOENT;  //need to resync
                } else {
                    result = EEXIST;
                }
                CHECK_AND_WRITE_TO_STAT_FILE1(pClientInfo);
            }
        }
        if (result != 0) {
            g_storage_stat.total_sync_in_bytes.add(pClientInfo.totalOffset);
        }
        pClientInfo.write(StorageServiceHelper.getHeadSetStateFromBufferPool(pClientInfo, result));
    }


    /**
     * storage_get_metadata_done_callback
     *
     * @param pClientInfo
     * @param err_no
     */
    public static void storage_get_metadata_done_callback(StorageClientInfo pClientInfo, final int err_no) {
        STORAGE_ACCESS_LOG(pClientInfo, ACCESS_LOG_ACTION_GET_METADATA, err_no);
        if (err_no != 0) {
            g_storage_stat.total_get_meta_count.increment();
            //todo 这里的判断的意义是?
            if (pClientInfo.getLength() == SizeOfConstant.SIZE_OF_TRACKER_HEADER) //never response
            {
                pClientInfo.write(StorageServiceHelper.getHeadSetStateFromBufferPool(pClientInfo, err_no));
            } else {
                STORAGE_NIO_NOTIFY_CLOSE(pClientInfo);
            }
        } else {
            CHECK_AND_WRITE_TO_STAT_FILE2(g_storage_stat.total_get_meta_count, g_storage_stat.success_get_meta_count);
            // storage_nio_notify(pClientInfo);
        }
    }

    /**
     * storage_download_file_done_callback
     *
     * @param pClientInfo
     * @param err_no
     */
    public static void storage_download_file_done_callback(StorageClientInfo pClientInfo, final int err_no) {
        STORAGE_ACCESS_LOG(pClientInfo, ACCESS_LOG_ACTION_DOWNLOAD_FILE, err_no);
        StorageFileContext pFileContext = pClientInfo.fileContext;
        int result = 0;
        if (err_no != 0) {
            g_storage_stat.total_download_count.increment();
            g_storage_stat.total_download_bytes.add(pFileContext.offset - pFileContext.start);
            if (pClientInfo.getLength() == SizeOfConstant.SIZE_OF_TRACKER_HEADER) //never response
            {
                pClientInfo.write(StorageServiceHelper.getHeadSetStateFromBufferPool(pClientInfo, err_no));
            } else {
                STORAGE_NIO_NOTIFY_CLOSE(pClientInfo);
            }
        } else {
            CHECK_AND_WRITE_TO_STAT_FILE2_WITH_BYTES(
                    g_storage_stat.total_download_count,
                    g_storage_stat.success_download_count,
                    g_storage_stat.total_download_bytes,
                    g_storage_stat.success_download_bytes,
                    pFileContext.end - pFileContext.start);
//
//            storage_nio_notify(pClientInfo);
        }
    }

    /**
     * storage_do_delete_meta_file
     *
     * @param pClientInfo
     * @param err_no
     * @return
     */
//    public static int storage_do_delete_meta_file(StorageClientInfo pClientInfo, final int err_no) {
//        ;
//        StorageFileContext pFileContext;
//        GroupArray pGroupArray;
//        char meta_filename[ MAX_PATH_SIZE + 256];
//        char true_filename[ 128];
//        char value[ 128];
//        FDHTKeyInfo key_info_fid;
//        FDHTKeyInfo key_info_ref;
//        FDHTKeyInfo key_info_sig;
//        char pValue;
//        int value_len;
//        int src_file_nlink;
//        int result;
//        int store_path_index;
//
//        pClientInfo = (StorageClientInfo) pClientInfo.arg;
//        pFileContext =  &(pClientInfo.file_context);
//
//        if (upload.getFileType() &
//                _FILE_TYPE_TRUNK) {
//            int filename_len = strlen(pFileContext.fname2log);
//            if ((result = storage_split_filename_ex(pFileContext.fname2log,
//                    & filename_len,true_filename, &store_path_index)) !=0)
//            {
//                return result;
//            }
//
//            sprintf(meta_filename, "%s/data/%s"FDFS_STORAGE_META_FILE_EXT,
//                    g_fdfs_store_paths.paths[store_path_index], true_filename);
//        } else {
//            sprintf(meta_filename, "%s"FDFS_STORAGE_META_FILE_EXT,
//                    pFileContext.filename);
//        }
//        if (fileExists(meta_filename)) {
//            if (unlink(meta_filename) != 0) {
//                if (errno != ENOENT) {
//                    result = errno != 0 ? errno : EACCES;
//                    logError("file: "__FILE__", line: %d, "
//                            "client ip: %s, delete file %s fail,"
//                            "errno: %d, error info: %s", __LINE__,
//                            pClientInfo.client_ip, meta_filename,
//                            result, STRERROR(result));
//                    return result;
//                }
//            } else {
//                sprintf(meta_filename, "%s"FDFS_STORAGE_META_FILE_EXT,
//                        pFileContext.fname2log);
//                result = storage_binlog_write(g_current_time,
//                        STORAGE_OP_TYPE_SOURCE_DELETE_FILE, meta_filename);
//                if (result != 0) {
//                    return result;
//                }
//            }
//        }
//
//        src_file_nlink = -1;
//        if (g_check_file_duplicate) {
//            pGroupArray =&((g_nio_thread_data + pClientInfo.nio_thread_index)
//               .group_array);
//            memset( & key_info_sig, 0, sizeof(key_info_sig));
//            key_info_sig.namespace_len = g_namespace_len;
//            memcpy(key_info_sig.szNameSpace, g_key_namespace,
//                    g_namespace_len);
//            key_info_sig.obj_id_len = snprintf(
//                    key_info_sig.szObjectId,
//                    sizeof(key_info_sig.szObjectId), "%s/%s",
//                    g_group_name, pFileContext.fname2log);
//
//            key_info_sig.key_len = sizeof(FDHT_KEY_NAME_FILE_SIG) - 1;
//            memcpy(key_info_sig.szKey, FDHT_KEY_NAME_FILE_SIG,
//                    key_info_sig.key_len);
//            pValue = value;
//            value_len = sizeof(value) - 1;
//            result = fdht_get_ex1(pGroupArray, g_keep_alive,
//                    & key_info_sig, FDHT_EXPIRES_NONE,
//                &pValue, &value_len, malloc);
//            if (result == 0) {
//                memcpy( & key_info_fid, &key_info_sig,
//                        sizeof(FDHTKeyInfo));
//                key_info_fid.obj_id_len = value_len;
//                memcpy(key_info_fid.szObjectId, pValue,
//                        value_len);
//
//                key_info_fid.key_len = sizeof(FDHT_KEY_NAME_FILE_ID) - 1;
//                memcpy(key_info_fid.szKey, FDHT_KEY_NAME_FILE_ID,
//                        key_info_fid.key_len);
//                value_len = sizeof(value) - 1;
//                result = fdht_get_ex1(pGroupArray,
//                        g_keep_alive, & key_info_fid,
//                        FDHT_EXPIRES_NONE, &pValue,
//                    &value_len, malloc);
//                if (result == 0) {
//                    memcpy( & key_info_ref, &key_info_sig,
//                            sizeof(FDHTKeyInfo));
//                    key_info_ref.obj_id_len = value_len;
//                    memcpy(key_info_ref.szObjectId, pValue,
//                            value_len);
//                    key_info_ref.key_len =
//                            sizeof(FDHT_KEY_NAME_REF_COUNT) - 1;
//                    memcpy(key_info_ref.szKey,
//                            FDHT_KEY_NAME_REF_COUNT,
//                            key_info_ref.key_len);
//                    value_len = sizeof(value) - 1;
//
//                    result = fdht_get_ex1(pGroupArray,
//                            g_keep_alive, & key_info_ref,
//                            FDHT_EXPIRES_NONE, &pValue,
//                        &value_len, malloc);
//                    if (result == 0) {
//                        (pValue + value_len) = '0';
//                        src_file_nlink = atoi(pValue);
//                    } else if (result != ENOENT) {
//                        logError("file: "__FILE__", line: %d, "
//                                "client ip: %s, fdht_get fail,"
//                                "errno: %d, error info: %s",
//                                __LINE__, pClientInfo.client_ip,
//                                result, STRERROR(result));
//                        return result;
//                    }
//                } else if (result != ENOENT) {
//                    logError("file: "__FILE__", line: %d, "
//                            "client ip: %s, fdht_get fail,"
//                            "errno: %d, error info: %s",
//                            __LINE__, pClientInfo.client_ip,
//                            result, STRERROR(result));
//                    return result;
//                }
//            } else if (result != ENOENT) {
//                logError("file: "__FILE__", line: %d, "
//                        "client ip: %s, fdht_get fail,"
//                        "errno: %d, error info: %s",
//                        __LINE__, pClientInfo.client_ip,
//                        result, STRERROR(result));
//                return result;
//            }
//        }
//
//        if (src_file_nlink < 0) {
//            return 0;
//        }
//
//        if (g_check_file_duplicate) {
//            char pSeperator;
//            struct stat stat_buf;
//            FDFSTrunkHeader trunkHeader;
//
//            pGroupArray =&((g_nio_thread_data + pClientInfo.nio_thread_index)
//               .group_array);
//            if ((result = fdht_delete_ex(pGroupArray, g_keep_alive,
//                    & key_info_sig)) !=0)
//            {
//                logWarning("file: "__FILE__", line: %d, "
//                        "client ip: %s, fdht_delete fail,"
//                        "errno: %d, error info: %s",
//                        __LINE__, pClientInfo.client_ip,
//                        result, STRERROR(result));
//            }
//
//            value_len = sizeof(value) - 1;
//            result = fdht_inc_ex(pGroupArray, g_keep_alive,
//                    & key_info_ref, FDHT_EXPIRES_NEVER, -1,
//                    value, &value_len);
//            if (result != 0) {
//                logWarning("file: "__FILE__", line: %d, "
//                        "client ip: %s, fdht_inc fail,"
//                        "errno: %d, error info: %s",
//                        __LINE__, pClientInfo.client_ip,
//                        result, STRERROR(result));
//                return result;
//            }
//
//            if (!(value_len == 1 && value == '0')) //value == 0
//            {
//                return 0;
//            }
//
//            if ((result = fdht_delete_ex(pGroupArray, g_keep_alive,
//                    & key_info_fid)) !=0)
//            {
//                logWarning("file: "__FILE__", line: %d, "
//                        "client ip: %s, fdht_delete fail,"
//                        "errno: %d, error info: %s",
//                        __LINE__, pClientInfo.client_ip,
//                        result, STRERROR(result));
//            }
//            if ((result = fdht_delete_ex(pGroupArray, g_keep_alive,
//                    & key_info_ref)) !=0)
//            {
//                logWarning("file: "__FILE__", line: %d, "
//                        "client ip: %s, fdht_delete fail,"
//                        "errno: %d, error info: %s",
//                        __LINE__, pClientInfo.client_ip,
//                        result, STRERROR(result));
//            }
//
//            (key_info_ref.szObjectId + key_info_ref.obj_id_len) = '0';
//            pSeperator = strchr(key_info_ref.szObjectId, '/');
//            if (pSeperator == NULL) {
//                logWarning("file: "__FILE__", line: %d, "
//                        "invalid file_id: %s", __LINE__,
//                        key_info_ref.szObjectId);
//                return 0;
//            }
//
//            pSeperator++;
//            value_len = key_info_ref.obj_id_len - (pSeperator -
//                    key_info_ref.szObjectId);
//            memcpy(value, pSeperator, value_len + 1);
//            if ((result = storage_split_filename_ex(value, & value_len,
//            true_filename, &store_path_index)) !=0)
//            {
//                return result;
//            }
//            if ((result = fdfs_check_data_filename(true_filename,
//                    value_len)) != 0) {
//                return result;
//            }
//
//            if ((result = trunk_file_lstat(store_path_index, true_filename,
//                    value_len, & stat_buf,
//            &(pFileContext.extra_info.upload.trunk_info),
//			&trunkHeader)) !=0)
//            {
//                STORAGE_STAT_FILE_FAIL_LOG(result, pClientInfo.client_ip,
//                        "logic", value)
//                return 0;
//            }
//
//            if (IS_TRUNK_FILE_BY_ID(pFileContext.extra_info.
//                    upload.trunk_info)) {
//                trunk_get_full_filename( & (pFileContext.extra_info.
//                        upload.trunk_info), pFileContext.filename,
//                        sizeof(pFileContext.filename));
//            } else {
//                sprintf(pFileContext.filename, "%s/data/%s",
//                        g_fdfs_store_paths.paths[store_path_index],
//                        true_filename);
//            }
//
//            if ((result = storage_delete_file_auto(pFileContext)) != 0) {
//                logWarning("file: "__FILE__", line: %d, "
//                        "client ip: %s, delete logic source file "
//                        "%s fail, errno: %d, error info: %s",
//                        __LINE__, pClientInfo.client_ip,
//                        value, errno, STRERROR(errno));
//                return 0;
//            }
//
//            storage_binlog_write(g_current_time,
//                    STORAGE_OP_TYPE_SOURCE_DELETE_FILE, value);
//            pFileContext.delete_flag |= STORAGE_DELETE_FLAG_FILE;
//        }
//
//        return 0;
//    }

    /**
     * storage_delete_fdfs_file_done_callback
     *
     * @param pClientInfo
     * @param err_no
     */
    public static void storage_delete_fdfs_file_done_callback(StorageClientInfo pClientInfo, final int err_no) {
        StorageFileContext pFileContext = pClientInfo.fileContext;
        StorageUploadInfo upload = (StorageUploadInfo) pFileContext.extra_info;
        int result = 0;
        if (err_no == 0) {
            if (upload.isTRUNK()) {
                TrunkClient.freeSpace(upload.getTrunkInfo(), err_no);
            }
            result = storage_binlog_write(g_current_time, STORAGE_OP_TYPE_SOURCE_DELETE_FILE, pFileContext.fname2log);
        } else {
            result = err_no;
        }

        if (result == 0) {
            result = StorageServiceMetadata.storage_do_delete_meta_file(pClientInfo);
        }
        if (result != 0) {
            if (pFileContext.isSTORAGE_DELETE_FLAG_NONE() ||
                    (pFileContext.isSTORAGE_DELETE_FLAG_FILE())) {
                g_storage_stat.total_delete_count.increment();
            }
            if (pFileContext.isSTORAGE_DELETE_FLAG_LINK()) {
                g_storage_stat.total_delete_link_count.increment();
            }
        } else {
            if (pFileContext.isSTORAGE_CREATE_FLAG_FILE()) {
                CHECK_AND_WRITE_TO_STAT_FILE3(
                        g_storage_stat.total_delete_count,
                        g_storage_stat.success_delete_count,
                        g_storage_stat);
            }

            if ((pFileContext.deleteFlag & STORAGE_DELETE_FLAG_LINK) == STORAGE_DELETE_FLAG_LINK) {
                CHECK_AND_WRITE_TO_STAT_FILE3(
                        g_storage_stat.total_delete_link_count,
                        g_storage_stat.success_delete_link_count,
                        g_storage_stat);
            }

        }
        STORAGE_ACCESS_LOG(pClientInfo, ACCESS_LOG_ACTION_DELETE_FILE, result);
        pClientInfo.write(StorageServiceHelper.getHeadSetStateFromBufferPool(pClientInfo, result));
    }

    /**
     * storage_upload_file_done_callback
     *
     * @param pClientInfo
     * @param err_no
     */
    public static void storage_upload_file_done_callback(StorageClientInfo pClientInfo, final int err_no) {
        StorageFileContext pFileContext = pClientInfo.fileContext;
        StorageUploadInfo upload = (StorageUploadInfo) pClientInfo.extraArg;
        int result = 0;

//        if (upload.getFileType() & _FILE_TYPE_TRUNK) {
//            result = trunk_client_trunk_alloc_confirm( & (pFileContext.extra_info.upload.trunk_info), err_no);
//            if (err_no != 0) {
//                result = err_no;
//            }
//        } else {
//            result = err_no;
//        }
        if (result == 0) {
            result = storage_service_upload_file_done(pClientInfo);
            if (result == 0) {
                if (pFileContext.isSTORAGE_CREATE_FLAG_FILE()) {
                    result = storage_binlog_write(
                            pFileContext.timestamp2log,
                            STORAGE_OP_TYPE_SOURCE_CREATE_FILE,
                            pFileContext.fname2log);
                }
            }
        }

        if (result == 0) {
            int filename_len;
            char p;
            if (pFileContext.isSTORAGE_CREATE_FLAG_FILE()) {
                CHECK_AND_WRITE_TO_STAT_FILE3_WITH_BYTES(
                        g_storage_stat.total_upload_count,
                        g_storage_stat.success_upload_count,
                        g_storage_stat,
                        g_storage_stat.total_upload_bytes,
                        g_storage_stat.success_upload_bytes,
                        pFileContext.end - pFileContext.start);
            }

            filename_len = pFileContext.fname2log.length();
            pClientInfo.totalLength = SizeOfConstant.SIZE_OF_TRACKER_HEADER + FDFS_GROUP_NAME_MAX_LEN + filename_len;
        } else {
            if (pFileContext.isSTORAGE_CREATE_FLAG_FILE()) {
                g_storage_stat.total_upload_count.increment();
                g_storage_stat.total_upload_bytes.add(pClientInfo.totalOffset);
            }
            pClientInfo.totalLength = SizeOfConstant.SIZE_OF_TRACKER_HEADER;
        }

        STORAGE_ACCESS_LOG(pClientInfo, ACCESS_LOG_ACTION_UPLOAD_FILE, result);

        ByteBuffer res;
        res = pClientInfo.getMyBufferPool().allocateByteBuffer();
        res.clear();
        res.position(8);
        ProtocolUtil.setStorageCMDResp(res);
        res.position(9);
        res.put((byte) 0);
        ProtocolUtil.setGroupName(res, upload.groupName);
        res.put(pFileContext.fname2log.getBytes(StandardCharsets.US_ASCII));
        int limit = res.position();
        int pkgLen = limit - 10;
        res.position(0);
        res.putLong(0, pkgLen);
        res.position(limit);
        pClientInfo.write(res);
    }

    /**
     * storage_trunk_create_link_file_done_callback
     *
     * @param pClientInfo
     * @param err_no
     */
//    public static void storage_trunk_create_link_file_done_callback(StorageClientInfo pClientInfo, final int err_no) {
//        ;
//        StorageFileContext pFileContext;
//        TrackerHeader pHeader;
//        TrunkCreateLinkArg pCreateLinkArg;
//        SourceFileInfo pSourceFileInfo;
//        int result;
//
//        pClientInfo = (StorageClientInfo) pClientInfo.arg;
//        pFileContext =  &(pClientInfo.file_context);
//        pCreateLinkArg = (TrunkCreateLinkArg) pClientInfo.extra_arg;
//        pSourceFileInfo = &(pCreateLinkArg.src_file_info);
//
//        result = trunk_client_trunk_alloc_confirm(
//                & (pFileContext.extra_info.upload.trunk_info), err_no);
//        if (err_no != 0) {
//            result = err_no;
//        }
//
//        if (result == 0) {
//            result = storage_service_upload_file_done(pClientInfo);
//            if (result == 0) {
//                char src_filename[ 128];
//                char binlog_msg[ 256];
//
//                sprintf(src_filename,
//                        "%c"FDFS_STORAGE_DATA_DIR_FORMAT"/%s",
//                        FDFS_STORAGE_STORE_PATH_PREFIX_CHAR,
//                        pFileContext.extra_info.upload.trunk_info.
//                                path.store_path_index,
//                        pSourceFileInfo.src_true_filename);
//
//                sprintf(binlog_msg, "%s %s",
//                        pFileContext.fname2log, src_filename);
//                result = storage_binlog_write(
//                        pFileContext.timestamp2log,
//                        STORAGE_OP_TYPE_SOURCE_CREATE_LINK,
//                        binlog_msg);
//            }
//        }
//
//        if (result == 0) {
//            int filename_len;
//            char p;
//
//            CHECK_AND_WRITE_TO_STAT_FILE3(
//                    g_storage_stat.total_create_link_count,
//                    g_storage_stat.success_create_link_count,
//                    g_storage_stat.last_source_update)
//
//            filename_len = strlen(pFileContext.fname2log);
//            pClientInfo.total_length = sizeof(TrackerHeader) +
//                    FDFS_GROUP_NAME_MAX_LEN + filename_len;
//            p = pClientInfo.data + sizeof(TrackerHeader);
//            memcpy(p, pFileContext.extra_info.upload.group_name,
//                    FDFS_GROUP_NAME_MAX_LEN);
//            p += FDFS_GROUP_NAME_MAX_LEN;
//            memcpy(p, pFileContext.fname2log, filename_len);
//        } else {
//            pthread_mutex_lock( & stat_count_thread_lock);
//            g_storage_stat.total_create_link_count++;
//            pthread_mutex_unlock( & stat_count_thread_lock);
//            pClientInfo.total_length = sizeof(TrackerHeader);
//        }
//
//        storage_set_link_file_meta(pClientInfo, pSourceFileInfo,
//                pFileContext.fname2log);
//
//        if (pCreateLinkArg.need_response) {
//            pClientInfo.total_offset = 0;
//            pClientInfo.length = pClientInfo.total_length;
//
//            pHeader = (TrackerHeader) pClientInfo.data;
//            pHeader.status = result;
//            pHeader.cmd = STORAGE_PROTO_CMD_RESP;
//            long2buff(pClientInfo.total_length - sizeof(TrackerHeader),
//                    pHeader.pkg_len);
//
//            storage_nio_notify(pClientInfo);
//        }
//    }

    /**
     * storage_append_file_done_callback
     *
     * @param pClientInfo
     * @param err_no
     */
    public static void storage_append_file_done_callback(StorageClientInfo pClientInfo, final int err_no) {
        StorageFileContext pFileContext = pClientInfo.fileContext;
        int result = 0;
        if (err_no == 0) {
            try {
                File file = new File(pFileContext.filename);
                pFileContext.timestamp2log = file.lastModified();
            } catch (Exception e) {
                if (result == 0) {
                    result = ENOENT;
                }
                STORAGE_STAT_FILE_FAIL_LOG(result, pClientInfo.getHost(), "regular", pFileContext.filename);
            }
            String extra = String.format("%d %d", pFileContext.start, pFileContext.end - pFileContext.start);
            result = StorageSync.storage_binlog_write_ex(pFileContext.timestamp2log,
                    pFileContext.syncFlag,
                    pFileContext.fname2log, extra);
        } else {
            result = err_no;
        }

        if (result == 0) {
            CHECK_AND_WRITE_TO_STAT_FILE3_WITH_BYTES(
                    g_storage_stat.total_append_count,
                    g_storage_stat.success_append_count,
                    g_storage_stat,
                    g_storage_stat.total_append_bytes,
                    g_storage_stat.success_append_bytes,
                    pFileContext.end - pFileContext.start);
        } else {
            g_storage_stat.total_append_count.increment();
            g_storage_stat.total_append_bytes.add(pClientInfo.totalOffset);
        }

        STORAGE_ACCESS_LOG(pClientInfo, ACCESS_LOG_ACTION_APPEND_FILE, result);
        pClientInfo.write(StorageServiceHelper.getHeadSetStateFromBufferPool(pClientInfo, result));
    }

    /**
     * storage_modify_file_done_callback
     *
     * @param pClientInfo
     * @param err_no
     */
    public static void storage_modify_file_done_callback(StorageClientInfo pClientInfo, final int err_no) {
        StorageFileContext pFileContext = pClientInfo.fileContext;
        int result = 0;
        if (err_no == 0) {
            try {
                File file = new File(pFileContext.filename);
                pFileContext.timestamp2log = file.lastModified();
            } catch (Exception e) {
                if (result == 0) {
                    result = ENOENT;
                }
                STORAGE_STAT_FILE_FAIL_LOG(result, pClientInfo.getHost(), "regular", pFileContext.filename);
            }
            String extra = String.format("%d %d", pFileContext.start, pFileContext.end - pFileContext.start);
            result = StorageSync.storage_binlog_write_ex(pFileContext.timestamp2log,
                    pFileContext.syncFlag,
                    pFileContext.fname2log, extra);
        } else {
            result = err_no;
        }

        if (result == 0) {
            CHECK_AND_WRITE_TO_STAT_FILE3_WITH_BYTES(
                    g_storage_stat.total_modify_count,
                    g_storage_stat.success_modify_count,
                    g_storage_stat,
                    g_storage_stat.total_modify_bytes,
                    g_storage_stat.success_modify_bytes,
                    pFileContext.end - pFileContext.start);
        } else {
            g_storage_stat.total_modify_count.increment();
            g_storage_stat.total_modify_bytes.add(pClientInfo.totalOffset);
        }
        STORAGE_ACCESS_LOG(pClientInfo, ACCESS_LOG_ACTION_MODIFY_FILE, result);
        pClientInfo.write(StorageServiceHelper.getHeadSetStateFromBufferPool(pClientInfo, result));
    }

    /**
     * storage_do_truncate_file_done_callback
     *
     * @param pClientInfo
     * @param err_no
     */
    public static void storage_do_truncate_file_done_callback(StorageClientInfo pClientInfo, final int err_no) {
        StorageFileContext pFileContext = pClientInfo.fileContext;
        int result = 0;
        if (err_no == 0) {
            try {
                File file = new File(pFileContext.filename);
                pFileContext.timestamp2log = file.lastModified();
            } catch (Exception e) {
                if (result == 0) {
                    result = ENOENT;
                }
                STORAGE_STAT_FILE_FAIL_LOG(result, pClientInfo.getHost(), "regular", pFileContext.filename);
            }
            String extra = String.format("%d %d", pFileContext.start, pFileContext.end - pFileContext.start);
            result = StorageSync.storage_binlog_write_ex(pFileContext.timestamp2log,
                    pFileContext.syncFlag,
                    pFileContext.fname2log, extra);
        } else {
            result = err_no;
        }

        if (result == 0) {
            CHECK_AND_WRITE_TO_STAT_FILE3(g_storage_stat.total_truncate_count, g_storage_stat.success_truncate_count, g_storage_stat);
        } else {
            g_storage_stat.total_truncate_count.increment();
        }
        STORAGE_ACCESS_LOG(pClientInfo, ACCESS_LOG_ACTION_TRUNCATE_FILE, result);
        pClientInfo.write(StorageServiceHelper.getHeadSetStateFromBufferPool(pClientInfo, result));
    }

    /**
     * storage_set_metadata_done_callback
     *
     * @param pClientInfo
     * @param err_no
     */
    public static void storage_set_metadata_done_callback(StorageClientInfo pClientInfo, final int err_no) {
        StorageFileContext pFileContext = pClientInfo.fileContext;
        int result = 0;
        if (err_no == 0) {
            if (pFileContext.syncFlag != '0') {
                result = storage_binlog_write(pFileContext.timestamp2log,
                        pFileContext.syncFlag, pFileContext.fname2log);
            } else {
                result = err_no;
            }
        } else {
            result = err_no;
        }
        if (result != 0) {
            g_storage_stat.total_set_meta_count.increment();
        } else {
            CHECK_AND_WRITE_TO_STAT_FILE3(g_storage_stat.total_set_meta_count, g_storage_stat.success_set_meta_count, g_storage_stat);
        }
        STORAGE_ACCESS_LOG(pClientInfo, ACCESS_LOG_ACTION_SET_METADATA, result);
        pClientInfo.write(StorageServiceHelper.getHeadSetStateFromBufferPool(pClientInfo, result));
    }

    /**
     * @param pClientInfo
     * @return
     */
    static int storage_service_upload_file_done(StorageClientInfo pClientInfo) {
        int result = 0;
        StorageFileContext pFileContext = pClientInfo.fileContext;
        long file_size_in_name;
        long file_size;
        long end_time;
        String new_fname2log;
        String new_full_filename;
        char[] new_filename = new char[128];
        int new_filename_len;
        file_size = pFileContext.end - pFileContext.start;
        StorageUploadInfo upload = (StorageUploadInfo) (pFileContext.extra_info);
        new_filename_len = 0;
        if ((upload.getFileType() & _FILE_TYPE_TRUNK) == _FILE_TYPE_TRUNK) {
            end_time = upload.getStartTime();
            file_size_in_name = COMBINE_RAND_FILE_SIZE(file_size);
            file_size_in_name |= FDFS_TRUNK_FILE_MARK_SIZE;
        } else {
            try {
                File file = new File(pFileContext.filename);
                end_time = file.lastModified();
            } catch (Exception e) {
                if (result == 0) {
                    result = ENOENT;
                }
                STORAGE_STAT_FILE_FAIL_LOG(result, pClientInfo.getHost(), "regular", pFileContext.filename);
                end_time = g_current_time;
            }

            if (upload.isAPPENDER()) {
                file_size_in_name = COMBINE_RAND_FILE_SIZE(0);
                file_size_in_name |= FDFS_APPENDER_FILE_SIZE;
            } else {
                file_size_in_name = file_size;
            }
        }
        //todo end_time是int 还是long crc32的类型是?
        try {
            new_full_filename = GenFilenameUtil.storageGetFilename(pClientInfo, (int) end_time,
                    file_size_in_name, (int) (pFileContext.crc32.getValue()), upload.getFormattedExtName());
        } catch (Exception e) {
            storage_delete_file_auto(pFileContext);
            return result;
        }
        upload.groupName = g_group_name;

        //生成新的文件名
        new_fname2log = String.format("%c" + FDFS_STORAGE_DATA_DIR_FORMAT + "/%s",
                FDFS_STORAGE_STORE_PATH_PREFIX_CHAR,
                upload.getTrunkInfo().getPath().getStorePathIndex(), new_filename);

        if (upload.isTRUNK()) {
//            char trunk_buff[ FDFS_TRUNK_FILE_INFO_LEN + 1];
//            trunk_file_info_encode( & (pFileContext.extra_info.upload.
//            trunk_info.file),trunk_buff);
//
//            sprintf(new_fname2log + FDFS_LOGIC_FILE_PATH_LEN
//                    + FDFS_FILENAME_BASE64_LENGTH, "%s%s", trunk_buff,
//            new_filename + FDFS_TRUE_FILE_PATH_LEN +
//            FDFS_FILENAME_BASE64_LENGTH);
        } else if (SharedFunc.rename(pFileContext.filename, new_full_filename)) {
//            result = errno != 0 ? errno : EPERM;
            LOGGER.error("rename %s to %s fail, ", pFileContext.filename, new_full_filename);
            SharedFunc.delete(pFileContext.filename);
            return result;
        }

        pFileContext.timestamp2log = end_time;
        if ((upload.isAPPENDER())) {
            pFileContext.fname2log = new_fname2log;
            pFileContext.createFlag = STORAGE_CREATE_FLAG_FILE;
            return 0;
        }

        if (upload.isSLAVE()) {
//            char true_filename[ 128];
//            char filename[ 128];
//            int master_store_path_index;
//            int master_filename_len = strlen(pFileContext.extra_info.
//                    upload.master_filename);
//            if ((result = storage_split_filename_ex(pFileContext.extra_info.
//                    upload.master_filename, & master_filename_len,
//            true_filename, &master_store_path_index)) !=0)
//            {
//                unlink(new_full_filename);
//                return result;
//            }
//            if ((result = fdfs_gen_slave_filename(true_filename,
//                    pFileContext.extra_info.upload.prefix_name,
//                    pFileContext.extra_info.upload.file_ext_name,
//                    filename, & filename_len)) !=0)
//            {
//                unlink(new_full_filename);
//                return result;
//            }
//
//            snprintf(pFileContext.filename, sizeof(pFileContext.filename),
//                    "%s/data/%s", g_fdfs_store_paths.paths[master_store_path_index],
//                    filename);
//            sprintf(pFileContext.fname2log,
//                    "%c"FDFS_STORAGE_DATA_DIR_FORMAT"/%s",
//                    FDFS_STORAGE_STORE_PATH_PREFIX_CHAR,
//                    master_store_path_index, filename);
//
//            if (g_store_slave_file_use_link) {
//                if (symlink(new_full_filename, pFileContext.filename) != 0) {
//                    result = errno != 0 ? errno : ENOENT;
//                    logError("file: "__FILE__", line: %d, "
//                            "link file %s to %s fail, "
//                            "errno: %d, error info: %s",
//                            __LINE__, new_full_filename,
//                            pFileContext.filename,
//                            result, STRERROR(result));
//
//                    unlink(new_full_filename);
//                    return result;
//                }
//
//                result = storage_binlog_write(
//                        pFileContext.timestamp2log,
//                        STORAGE_OP_TYPE_SOURCE_CREATE_FILE,
//                        new_fname2log);
//                if (result == 0) {
//                    char binlog_buff[ 256];
//                    snprintf(binlog_buff, sizeof(binlog_buff),
//                            "%s %s", pFileContext.fname2log,
//                            new_fname2log);
//                    result = storage_binlog_write(
//                            pFileContext.timestamp2log,
//                            STORAGE_OP_TYPE_SOURCE_CREATE_LINK,
//                            binlog_buff);
//                }
//                if (result != 0) {
//                    unlink(new_full_filename);
//                    unlink(pFileContext.filename);
//                    return result;
//                }
//
//                pFileContext.create_flag = STORAGE_CREATE_FLAG_LINK;
//            } else {
//                if (rename(new_full_filename, pFileContext.filename) != 0) {
//                    result = errno != 0 ? errno : ENOENT;
//                    logError("file: "__FILE__", line: %d, "
//                            "rename file %s to %s fail, "
//                            "errno: %d, error info: %s",
//                            __LINE__, new_full_filename,
//                            pFileContext.filename,
//                            result, STRERROR(result));
//
//                    unlink(new_full_filename);
//                    return result;
//                }
//
//                pFileContext.createFlag = STORAGE_CREATE_FLAG_FILE;
//            }
//
//            return 0;
        }

        pFileContext.fname2log = new_fname2log;
        if (!upload.isTRUNK()) {
            pFileContext.filename = new_full_filename;
        }

//        if (g_check_file_duplicate && !(upload.getFileType() &
//        _FILE_TYPE_LINK))
//        {
//            GroupArray * pGroupArray;
//            char value[ 128];
//            FDHTKeyInfo key_info;
//		char *pValue;
//            int value_len;
//            int nSigLen;
//            char szFileSig[ FILE_SIGNATURE_SIZE];
//            //char buff[64];
//
//            memset( & key_info, 0, sizeof(key_info));
//            key_info.namespace_len = g_namespace_len;
//            memcpy(key_info.szNameSpace, g_key_namespace, g_namespace_len);
//
//            pGroupArray =&((g_nio_thread_data + pClientInfo.nio_thread_index)
//				->group_array);
//
//            STORAGE_GEN_FILE_SIGNATURE(file_size,
//                    pFileContext.file_hash_codes, szFileSig)
//		/*
//		bin2hex(szFileSig, FILE_SIGNATURE_SIZE, buff);
//		logInfo("file: "__FILE__", line: %d, "
//			"file sig: %s", __LINE__, buff);
//		*/
//
//            nSigLen = FILE_SIGNATURE_SIZE;
//            key_info.obj_id_len = nSigLen;
//            memcpy(key_info.szObjectId, szFileSig, nSigLen);
//            key_info.key_len = sizeof(FDHT_KEY_NAME_FILE_ID) - 1;
//            memcpy(key_info.szKey, FDHT_KEY_NAME_FILE_ID,
//                    sizeof(FDHT_KEY_NAME_FILE_ID) - 1);
//
//            pValue = value;
//            value_len = sizeof(value) - 1;
//            result = fdht_get_ex1(pGroupArray, g_keep_alive,
//                    & key_info, FDHT_EXPIRES_NONE,
//				&pValue, &value_len, malloc);
//            if (result == 0) {   //exists
//			char *pGroupName;
//			char *pSrcFilename;
//			char *pSeperator;
//
//			*(value + value_len) = '0';
//                pSeperator = strchr(value, '/');
//                if (pSeperator == NULL) {
//                    logError("file: "__FILE__", line: %d, "
//                            "value %s is invalid",
//                            __LINE__, value);
//
//                    return EINVAL;
//                }
//
//			*pSeperator = '0';
//                pGroupName = value;
//                pSrcFilename = pSeperator + 1;
//
//                if ((result = storage_delete_file_auto(pFileContext)) != 0) {
//                    logError("file: "__FILE__", line: %d, "
//                            "unlink %s fail, errno: %d, "
//                            "error info: %s", __LINE__,
//                    ((pFileContext.extra_info.upload.
//                    file_type & _FILE_TYPE_TRUNK) ?
//                    pFileContext.fname2log
//					:pFileContext.filename),
//                    result, STRERROR(result));
//
//                    return result;
//                }
//
//                memset(pFileContext.extra_info.upload.group_name,
//                        0, FDFS_GROUP_NAME_MAX_LEN + 1);
//                snprintf(pFileContext.extra_info.upload.group_name,
//                        FDFS_GROUP_NAME_MAX_LEN + 1, "%s", pGroupName);
//                result = storage_client_create_link_wrapper(pTask,
//                        pFileContext.extra_info.upload.master_filename,
//                        pSrcFilename, value_len - (pSrcFilename - value),
//                        key_info.szObjectId, key_info.obj_id_len,
//                        pGroupName,
//                        pFileContext.extra_info.upload.prefix_name,
//                        pFileContext.extra_info.upload.file_ext_name,
//                        pFileContext.fname2log, & filename_len);
//
//                pFileContext.create_flag = STORAGE_CREATE_FLAG_LINK;
//                return result;
//            } else if (result == ENOENT) {
//                char src_filename[ 128];
//                FDHTKeyInfo ref_count_key;
//
//                filename_len = sprintf(src_filename, "%s", new_fname2log);
//                value_len = sprintf(value, "%s/%s",
//                        g_group_name, new_fname2log);
//                if ((result = fdht_set_ex(pGroupArray, g_keep_alive,
//                        & key_info,FDHT_EXPIRES_NEVER,
//                value, value_len)) !=0)
//                {
//                    logError("file: "__FILE__", line: %d, "
//                            "client ip: %s, fdht_set fail,"
//                            "errno: %d, error info: %s",
//                            __LINE__, pTask.client_ip,
//                            result, STRERROR(result));
//
//                    storage_delete_file_auto(pFileContext);
//                    return result;
//                }
//
//                memcpy( & ref_count_key, &key_info, sizeof(FDHTKeyInfo));
//                ref_count_key.obj_id_len = value_len;
//                memcpy(ref_count_key.szObjectId, value, value_len);
//                ref_count_key.key_len = sizeof(FDHT_KEY_NAME_REF_COUNT) - 1;
//                memcpy(ref_count_key.szKey, FDHT_KEY_NAME_REF_COUNT,
//                        ref_count_key.key_len);
//                if ((result = fdht_set_ex(pGroupArray, g_keep_alive,
//                        & ref_count_key,FDHT_EXPIRES_NEVER, "0", 1)) !=0)
//                {
//                    logError("file: "__FILE__", line: %d, "
//                            "client ip: %s, fdht_set fail,"
//                            "errno: %d, error info: %s",
//                            __LINE__, pTask.client_ip,
//                            result, STRERROR(result));
//
//                    storage_delete_file_auto(pFileContext);
//                    return result;
//                }
//
//
//                result = storage_binlog_write(pFileContext.timestamp2log,
//                        STORAGE_OP_TYPE_SOURCE_CREATE_FILE,
//                        src_filename);
//                if (result != 0) {
//                    storage_delete_file_auto(pFileContext);
//                    return result;
//                }
//
//                result = storage_client_create_link_wrapper(pTask,
//                        pFileContext.extra_info.upload.master_filename,
//                        src_filename, filename_len, szFileSig, nSigLen,
//                        g_group_name, pFileContext.extra_info.upload.prefix_name,
//                        pFileContext.extra_info.upload.file_ext_name,
//                        pFileContext.fname2log, & filename_len);
//
//                if (result != 0) {
//                    fdht_delete_ex(pGroupArray, g_keep_alive, & key_info);
//                    fdht_delete_ex(pGroupArray, g_keep_alive, & ref_count_key);
//
//                    storage_delete_file_auto(pFileContext);
//                }
//
//                pFileContext.create_flag = STORAGE_CREATE_FLAG_LINK;
//                return result;
//            } else //error
//            {
//                logError("file: "__FILE__", line: %d, "
//                        "fdht_get fail, "
//                        "errno: %d, error info: %s",
//                        __LINE__, result, STRERROR(errno));
//
//                storage_delete_file_auto(pFileContext);
//                return result;
//            }
//        }

        if ((upload.getFileType() & _FILE_TYPE_LINK) == _FILE_TYPE_LINK) {
            pFileContext.createFlag = STORAGE_CREATE_FLAG_LINK;
        } else {
            pFileContext.createFlag = STORAGE_CREATE_FLAG_FILE;
        }

        return 0;
    }


}
