package io.mybear.storage.parserHandler;

import java.nio.ByteBuffer;

import static org.csource.fastdfs.ProtoCommon.*;

/**
 * Created by jamie on 2017/7/12.
 */
public interface ParserHandler<T, U> {
    DeleteFileParserHandler DELETE_FILE_PARSER_HANDLER = new DeleteFileParserHandler();
    DownloadFileParserHandler DOWNLOAD_FILE_PARSER_HANDLER = new DownloadFileParserHandler();
    SetMetaDataParserHandler SET_META_DATA_PARSER_HANDLER = new SetMetaDataParserHandler();
    UploadFileParserHandler UPLOAD_FILE_PARSER_HANDLER = new UploadFileParserHandler();
    UploadSlaveFileParserHandler UPLOAD_SLAVE_FILE_PARSER_HANDLER = new UploadSlaveFileParserHandler();

    static ParserHandler getParserHandler(int cmd) {
        switch (cmd) {
            case STORAGE_PROTO_CMD_DOWNLOAD_FILE:
                return DOWNLOAD_FILE_PARSER_HANDLER;
            case STORAGE_PROTO_CMD_UPLOAD_FILE:
                return UPLOAD_FILE_PARSER_HANDLER;
            case STORAGE_PROTO_CMD_DELETE_FILE:
                return DELETE_FILE_PARSER_HANDLER;
            case STORAGE_PROTO_CMD_SET_METADATA:
                return SET_META_DATA_PARSER_HANDLER;
            case STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE:
                return UPLOAD_SLAVE_FILE_PARSER_HANDLER;
            default:
                return null;
        }
    }

    static long getMetaDataLength(int cmd, long totalLen) {
        switch (cmd) {
            case FDFS_PROTO_CMD_QUIT:
                return 0;
            case TRACKER_PROTO_CMD_SERVER_LIST_GROUP:
                return 0;
//            case TRACKER_PROTO_CMD_SERVER_LIST_STORAGE:
//                return totalLen;
//            case TRACKER_PROTO_CMD_SERVER_DELETE_STORAGE:
//                return totalLen;
//            case TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE:
//                return totalLen;
//            case TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ONE:
//                return totalLen;
//            case TRACKER_PROTO_CMD_SERVICE_QUERY_UPDATE:
//                return totalLen;
//            case TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ONE:
//                return totalLen;
//            case TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ALL:
//                return totalLen;
//            case TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ALL:
//                return totalLen;
//            case TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ALL:
//                return totalLen;
            case TRACKER_PROTO_CMD_RESP:
                return 0;
            case FDFS_PROTO_CMD_ACTIVE_TEST:
                return 0;
            case STORAGE_PROTO_CMD_UPLOAD_FILE:
                return UploadFileParserHandler.SIZE;
            case STORAGE_PROTO_CMD_DELETE_FILE:
                return DeleteFileParserHandler.SIZE;
            case STORAGE_PROTO_CMD_SET_METADATA:
                return totalLen;
            case STORAGE_PROTO_CMD_DOWNLOAD_FILE:
                return DownloadFileParserHandler.SIZE;
            case STORAGE_PROTO_CMD_GET_METADATA:
                return SetMetaDataParserHandler.SIZE;
            case STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE:
                return UploadSlaveFileParserHandler.SIZE;
//            case STORAGE_PROTO_CMD_QUERY_FILE_INFO:
//                return totalLen;
            case STORAGE_PROTO_CMD_UPLOAD_APPENDER_FILE:
                return 15;//待修改
            case STORAGE_PROTO_CMD_APPEND_FILE:
                return 15;
            case STORAGE_PROTO_CMD_MODIFY_FILE:
                return 15;
            case STORAGE_PROTO_CMD_TRUNCATE_FILE:
                return 16;
            default:
                return -1;
        }
    }

    public static void debug(ByteBuffer byteBuffer) {
        ByteBuffer v = byteBuffer.duplicate();
        v.flip();
        byte[] bytes = new byte[byteBuffer.position()];
        v.get(bytes);
        System.out.println(new String(bytes));

    }

    long handleMetaData(T con, U nioData);

    void handle(T con, U nioData);

    boolean handleEnd(T con, U nioData);

    int getSize();

}
