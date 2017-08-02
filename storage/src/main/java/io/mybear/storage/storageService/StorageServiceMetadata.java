package io.mybear.storage.storageService;

import io.mybear.common.constants.config.StorageGlobal;
import io.mybear.common.utils.MetadataUtil;
import io.mybear.common.utils.ProtocolUtil;
import io.mybear.common.utils.SharedFunc;
import io.mybear.storage.StorageDio;
import io.mybear.storage.StorageFileContext;
import io.mybear.storage.StorageSetMetaInfo;
import io.mybear.storage.storageNio.StorageClientInfo;
import io.mybear.storage.storageSync.StorageSync;
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

import static io.mybear.common.constants.TrackerProto.STORAGE_PROTO_CMD_RESP;
import static io.mybear.common.constants.TrackerProto.STORAGE_SET_METADATA_FLAG_OVERWRITE;
import static io.mybear.common.constants.config.StorageGlobal.g_storage_stat;
import static io.mybear.storage.FdfsStoraged.g_current_time;
import static io.mybear.storage.storageService.StorageService.ACCESS_LOG_ACTION_SET_METADATA;
import static io.mybear.storage.storageService.StorageServiceHelper.storage_log_access_log;
import static io.mybear.storage.storageSync.StorageSync.STORAGE_OP_TYPE_SOURCE_CREATE_FILE;
import static io.mybear.storage.storageSync.StorageSync.STORAGE_OP_TYPE_SOURCE_UPDATE_FILE;

/**
 * Created by jamie on 2017/8/2.
 */
public class StorageServiceMetadata {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageServiceMetadata.class);

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

}
