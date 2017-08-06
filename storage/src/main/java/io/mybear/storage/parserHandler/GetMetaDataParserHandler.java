package io.mybear.storage.parserHandler;

import io.mybear.common.trunk.TrunkShared;
import io.mybear.common.utils.StringUtil;
import io.mybear.storage.StorageDio;
import io.mybear.storage.StorageFileContext;
import io.mybear.storage.storageNio.StorageClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static io.mybear.common.constants.CommonConstant.FDFS_STORAGE_META_FILE_EXT;
import static io.mybear.common.constants.config.StorageGlobal.g_group_name;

/**
 * Created by jamie on 2017/7/25.
 * storage_server_get_metadata
 */
public class GetMetaDataParserHandler implements ParserHandler<StorageClientInfo, ByteBuffer> {
    public static final int SIZE = 16;
    private static final Logger LOGGER = LoggerFactory.getLogger(SetMetaDataParserHandler.class);

    /**
     * pkg format:
     * Header
     * FDFS_GROUP_NAME_MAX_LEN bytes: group_name
     * filename
     **/
    @Override
    public long handleMetaData(StorageClientInfo con, ByteBuffer nioData) {
        nioData.flip();
        //byte flag = nioData.get();
        byte[] group_name = new byte[16];
        nioData.get(group_name);
        nioData.position(0);
        if (!Arrays.equals(group_name, g_group_name)) {
            con.close("group_name 不对应");
        }
        System.out.println("groupName:" + new String(group_name));
        StorageFileContext fileContext = con.makeStorageFileContext();
        con.makeMetaInfo((int) con.getLength(), (int) con.getLength());
        return 0;
    }

    @Override
    public void handle(StorageClientInfo con, ByteBuffer nioData) {
        con.appendMetaInfo(nioData);
    }


    @Override
    public void handleEnd(StorageClientInfo con, ByteBuffer nioData) {
        con.appendMetaInfo(nioData);
        char[] true_filename = new char[128];
        int store_path_index = StringUtil.storage_split_filename_ex(con.getMetaInfo().metaBuff.toString().toCharArray(), true_filename.length, true_filename);
        con.fileContext.filename = String.format("%s/data/%s%s", TrunkShared.getFdfsStorePaths().getPaths()[store_path_index], new String(true_filename), FDFS_STORAGE_META_FILE_EXT);
        con.dealFunc = StorageDio::dio_read_file;
    }

    @Override
    public int getSize() {
        return SIZE;
    }

}
