package io.mybear.storage.parserHandler;

import java.nio.ByteBuffer;

import static io.mybear.common.constants.TrackerProto.*;


/**
 * Created by jamie on 2017/7/12.
 */
public interface ParserHandler<T, U> {
    DeleteFileParserHandler DELETE_FILE_PARSER_HANDLER = new DeleteFileParserHandler();
    DownloadFileParserHandler DOWNLOAD_FILE_PARSER_HANDLER = new DownloadFileParserHandler();
    SetMetaDataParserHandler SET_META_DATA_PARSER_HANDLER = new SetMetaDataParserHandler();
    UploadFileParserHandler UPLOAD_FILE_PARSER_HANDLER = new UploadFileParserHandler();
    UploadSlaveFileParserHandler UPLOAD_SLAVE_FILE_PARSER_HANDLER = new UploadSlaveFileParserHandler();
    GetMetaDataParserHandler GET_META_DATA_PARSER_HANDLER = new GetMetaDataParserHandler();
    ActiveTestParserHandler ACTIVE_TEST = new ActiveTestParserHandler();
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
            case STORAGE_PROTO_CMD_GET_METADATA:
                return GET_META_DATA_PARSER_HANDLER;
            case STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE:
                return UPLOAD_SLAVE_FILE_PARSER_HANDLER;
            case FDFS_PROTO_CMD_ACTIVE_TEST:
                return ACTIVE_TEST;
            default:
                return null;
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

    void handleEnd(T con, U nioData);

    int getSize();

}
