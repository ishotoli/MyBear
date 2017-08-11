package io.mybear.storage.parserHandler;

import io.mybear.common.trunk.TrunkShared;
import io.mybear.common.utils.FilenameResultEx;
import io.mybear.common.utils.StringUtil;
import io.mybear.common.utils.TimeUtil;
import io.mybear.storage.StorageDio;
import io.mybear.storage.StorageFileContext;
import io.mybear.storage.StorageSetMetaInfo;
import io.mybear.storage.storageNio.StorageClientInfo;
import io.mybear.storage.storageService.StorageServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static io.mybear.common.constants.CommonConstant.FDFS_STORAGE_META_FILE_EXT;
import static io.mybear.storage.StorageDio.FDFS_STORAGE_FILE_OP_WRITE;

/**
 * Created by jamie on 2017/7/12.
 */
public class SetMetaDataParserHandler implements ParserHandler<StorageClientInfo, ByteBuffer> {
    public static final int SIZE = 8 + 8 + 1 + 16;
    private static final Logger LOGGER = LoggerFactory.getLogger(SetMetaDataParserHandler.class);

    @Override
    public long handleMetaData(StorageClientInfo con, ByteBuffer nioData) {
        nioData.flip();
        int filenameLen = (int) nioData.getLong();
        int meta_buff_length = (int) nioData.getLong();
        byte flag = nioData.get();
        byte[] group_name = new byte[16];
        nioData.get(group_name);
        nioData.position(0);
        con.fileContext = new StorageFileContext();
        con.makeMetaInfo((int) filenameLen, (int) (filenameLen + meta_buff_length));
        System.out.println("groupName:" + new String(group_name));
        return 0;
    }

    @Override
    public void handle(StorageClientInfo con, ByteBuffer nioData) {
        con.appendMetaInfo(nioData);
    }

    @Override
    public void handleEnd(StorageClientInfo con, ByteBuffer nioData) {
        con.appendMetaInfo(nioData);
        StorageSetMetaInfo metaInfo = con.getMetaInfo();
        String filename = metaInfo.metaBuff.substring(0, metaInfo.filenameLength);
        metaInfo.metaBuff = new StringBuilder(metaInfo.metaBuff.substring(metaInfo.filenameLength));
        FilenameResultEx resultEx = StringUtil.storage_split_filename_ex(filename);
        int store_path_index = resultEx.storePathIndex;
        String true_filename = resultEx.true_filename;
        resultEx = null;
        con.fileContext.timestamp2log = TimeUtil.currentTimeMillis();
        con.fileContext.filename = String.format(
                "%s/data/%s%s",
                TrunkShared.getFdfsStorePaths().getPaths()[store_path_index],
                true_filename,
                FDFS_STORAGE_META_FILE_EXT);
        con.fileContext.fname2log = String.format("%s%s", filename, FDFS_STORAGE_META_FILE_EXT);
        con.fileContext.dioExecutorService = StorageDio.getThreadIndex(con, store_path_index, FDFS_STORAGE_FILE_OP_WRITE);
        con.dealFunc = StorageServiceMetadata::storage_do_set_metadata;
        StorageDio.queuePush(con);
    }

    @Override
    public int getSize() {
        return SIZE;
    }

}
