package io.mybear.tracker;

import static io.mybear.common.CommonDefine.IP_ADDRESS_SIZE;

/**
 * Created by jamie on 2017/6/21.
 */
public class TrackerProto {
    public static final int FDFS_PROTO_PKG_LEN_SIZE = 8;

    // 解析fdfs消息头部
    public static final byte FDFS_PROTO_CMD_HEAD = 0;

    // storage heart beat
    public static final byte TRACKER_PROTO_CMD_STORAGE_BEAT = 83;


    public static final byte TRACKER_PROTO_CMD_STORAGE_JOIN = 81;
    public final static int FDFS_PROTO_CMD_QUIT = 82;
    public final static int TRACKER_PROTO_CMD_STORAGE_REPORT_DISK_USAGE = 84; //report disk usage
    public final static int TRACKER_PROTO_CMD_STORAGE_REPLICA_CHG = 85; //repl new storage servers
    public final static int TRACKER_PROTO_CMD_STORAGE_SYNC_SRC_REQ = 86;  //src storage require sync
    public final static int TRACKER_PROTO_CMD_STORAGE_SYNC_DEST_REQ = 87; //dest storage require sync
    public final static int TRACKER_PROTO_CMD_STORAGE_SYNC_NOTIFY = 88; //sync done notify
    public final static int TRACKER_PROTO_CMD_STORAGE_SYNC_REPORT = 89;//report src last synced time as dest server
    public final static int TRACKER_PROTO_CMD_STORAGE_SYNC_DEST_QUERY = 79;//dest storage query sync src storage server
    public final static int TRACKER_PROTO_CMD_STORAGE_REPORT_IP_CHANGED = 78; //storage server report it's ip changed
    public final static int TRACKER_PROTO_CMD_STORAGE_CHANGELOG_REQ = 77; //storage server request storage server's changelog
    public final static int TRACKER_PROTO_CMD_STORAGE_REPORT_STATUS = 76;  //report specified storage server status
    public final static int TRACKER_PROTO_CMD_STORAGE_PARAMETER_REQ = 75; //storage server request parameters
    public final static int TRACKER_PROTO_CMD_STORAGE_REPORT_TRUNK_FREE = 74; //storage report trunk free space
    public final static int TRACKER_PROTO_CMD_STORAGE_REPORT_TRUNK_FID = 73; //storage report current trunk file id
    public final static int TRACKER_PROTO_CMD_STORAGE_FETCH_TRUNK_FID = 72;  //storage get current trunk file id
    public final static int TRACKER_PROTO_CMD_STORAGE_GET_STATUS = 71;  //get storage status from tracker
    public final static int TRACKER_PROTO_CMD_STORAGE_GET_SERVER_ID = 70; //get storage server id from tracker
    public final static int TRACKER_PROTO_CMD_STORAGE_FETCH_STORAGE_IDS = 69;//get all storage ids from tracker
    public final static int TRACKER_PROTO_CMD_STORAGE_GET_GROUP_NAME = 109; //get storage group name from tracker

    public final static int TRACKER_PROTO_CMD_TRACKER_GET_SYS_FILES_START = 61; //start of tracker get system data files
    public final static int TRACKER_PROTO_CMD_TRACKER_GET_SYS_FILES_END = 62; //end of tracker get system data files
    public final static int TRACKER_PROTO_CMD_TRACKER_GET_ONE_SYS_FILE = 63; //tracker get a system data file
    public final static int TRACKER_PROTO_CMD_TRACKER_GET_STATUS = 64; //tracker get status of other tracker
    public final static int TRACKER_PROTO_CMD_TRACKER_PING_LEADER = 65; //tracker ping leader
    public final static int TRACKER_PROTO_CMD_TRACKER_NOTIFY_NEXT_LEADER = 66;//notify next leader to other trackers
    public final static int TRACKER_PROTO_CMD_TRACKER_COMMIT_NEXT_LEADER = 67; //commit next leader to other trackers
    public final static int TRACKER_PROTO_CMD_TRACKER_NOTIFY_RESELECT_LEADER = 68; //storage notify reselect leader when split-brain

    public final static int TRACKER_PROTO_CMD_SERVER_LIST_ONE_GROUP = 90;
    public final static int TRACKER_PROTO_CMD_SERVER_LIST_ALL_GROUPS = 91;
    public final static int TRACKER_PROTO_CMD_SERVER_LIST_STORAGE = 92;
    public final static int TRACKER_PROTO_CMD_SERVER_DELETE_STORAGE = 93;
    public final static int TRACKER_PROTO_CMD_SERVER_SET_TRUNK_SERVER = 94;
    public final static int TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE = 101;
    public final static int TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ONE = 102;
    public final static int TRACKER_PROTO_CMD_SERVICE_QUERY_UPDATE = 103;
    public final static int TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ONE = 104;
    public final static int TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ALL = 105;
    public final static int TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ALL = 106;
    public final static int TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ALL = 107;
    public final static int TRACKER_PROTO_CMD_SERVER_DELETE_GROUP = 108;

    public final static int TRACKER_PROTO_CMD_RESP = 100;
    public final static int FDFS_PROTO_CMD_ACTIVE_TEST = 111; //active test, tracker and storage both support since V1.28

    public final static int STORAGE_PROTO_CMD_REPORT_SERVER_ID = 9;
    public final static int STORAGE_PROTO_CMD_UPLOAD_FILE = 11;
    public final static int STORAGE_PROTO_CMD_DELETE_FILE = 12;
    public final static int STORAGE_PROTO_CMD_SET_METADATA = 13;
    public final static int STORAGE_PROTO_CMD_DOWNLOAD_FILE = 14;
    public final static int STORAGE_PROTO_CMD_GET_METADATA = 15;
    public final static int STORAGE_PROTO_CMD_SYNC_CREATE_FILE = 16;
    public final static int STORAGE_PROTO_CMD_SYNC_DELETE_FILE = 17;
    public final static int STORAGE_PROTO_CMD_SYNC_UPDATE_FILE = 18;
    public final static int STORAGE_PROTO_CMD_SYNC_CREATE_LINK = 19;
    public final static int STORAGE_PROTO_CMD_CREATE_LINK = 20;
    public final static int STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE = 21;
    public final static int STORAGE_PROTO_CMD_QUERY_FILE_INFO = 22;
    public final static int STORAGE_PROTO_CMD_UPLOAD_APPENDER_FILE = 23; //create appender file
    public final static int STORAGE_PROTO_CMD_APPEND_FILE = 24;  //append file
    public final static int STORAGE_PROTO_CMD_SYNC_APPEND_FILE = 25;
    public final static int STORAGE_PROTO_CMD_FETCH_ONE_PATH_BINLOG = 26;
    ;  //fetch binlog of one store path
    public final static int STORAGE_PROTO_CMD_RESP = TRACKER_PROTO_CMD_RESP;
    public final static int STORAGE_PROTO_CMD_UPLOAD_MASTER_FILE = STORAGE_PROTO_CMD_UPLOAD_FILE;

    public final static int STORAGE_PROTO_CMD_TRUNK_ALLOC_SPACE = 27;//since V3.00, storage to trunk server
    public final static int STORAGE_PROTO_CMD_TRUNK_ALLOC_CONFIRM = 28; //since V3.00, storage to trunk server
    public final static int STORAGE_PROTO_CMD_TRUNK_FREE_SPACE = 29;//since V3.00, storage to trunk server
    public final static int STORAGE_PROTO_CMD_TRUNK_SYNC_BINLOG = 30; //since V3.00, trunk storage to storage
    public final static int STORAGE_PROTO_CMD_TRUNK_GET_BINLOG_SIZE = 31;  //since V3.07, tracker to storage
    public final static int STORAGE_PROTO_CMD_TRUNK_DELETE_BINLOG_MARKS = 32;//since V3.07, tracker to storage
    public final static int STORAGE_PROTO_CMD_TRUNK_TRUNCATE_BINLOG_FILE = 33; //since V3.07, trunk storage to storage

    public final static int STORAGE_PROTO_CMD_MODIFY_FILE = 34; //since V3.08
    public final static int STORAGE_PROTO_CMD_SYNC_MODIFY_FILE = 35; //since V3.08
    public final static int STORAGE_PROTO_CMD_TRUNCATE_FILE = 36;//since V3.08
    public final static int STORAGE_PROTO_CMD_SYNC_TRUNCATE_FILE = 37;//since V3.08
    public final static int FDFS_PROTO_CMD_SIZE = 1;
    public final static int FDFS_PROTO_IP_PORT_SIZE = (IP_ADDRESS_SIZE + 6);
    //for overwrite all old metadata
    public static char STORAGE_SET_METADATA_FLAG_OVERWRITE = 'O';
    public static String STORAGE_SET_METADATA_FLAG_OVERWRITE_STR = "O";
    //for replace, insert when the meta item not exist, otherwise update ==it
    public static char STORAGE_SET_METADATA_FLAG_MERGE = 'M';
    public static String STORAGE_SET_METADATA_FLAG_MERGE_STR = "M";
}
