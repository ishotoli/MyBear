package io.mybear.storage.parserHandler;

import io.mybear.common.utils.FilenameResultEx;
import io.mybear.common.utils.ProtocolUtil;
import io.mybear.common.utils.StringUtil;
import io.mybear.storage.storageNio.StorageClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by jamie on 2017/7/25.
 * storage_server_query_file_info
 * STORAGE_PROTO_CMD_QUERY_FILE_INFO
 * * FDFS logic filename to log not including group name
 * char fname2log[128+sizeof(FDFS_STORAGE_META_FILE_EXT)];
 */
public class QueryFileInfoHandler implements ParserHandler<StorageClientInfo, ByteBuffer> {
    public static final Logger LOGGER = LoggerFactory.getLogger(QueryFileInfoHandler.class);

    @Override
    public long handleMetaData(StorageClientInfo con, ByteBuffer nioData) {
        nioData.flip();
        LOGGER.debug(ProtocolUtil.getGroupName(nioData));
        con.context = new StringBuilder((int) (con.getLength() - 10));
        return 0;
    }

    @Override
    public void handle(StorageClientInfo con, ByteBuffer nioData) {
        nioData.flip();
        con.context.append(nioData.asCharBuffer());
    }

    @Override
    public void handleEnd(StorageClientInfo con, ByteBuffer nioData) {
        nioData.flip();
        con.context.append(nioData.asCharBuffer());
        String true_filename = con.context.toString();
        System.out.println(true_filename);
        FilenameResultEx resultEx = StringUtil.storage_split_filename_ex(true_filename);
        //todo trunk

        //todo S_ISLNK
//        StringUtil.trunk_file_lstat()
    }

    @Override
    public int getSize() {
        return 16;
    }
}
