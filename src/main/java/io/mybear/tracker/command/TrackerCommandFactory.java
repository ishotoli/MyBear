package io.mybear.tracker.command;

import io.mybear.tracker.TrackerProto;

public class TrackerCommandFactory {
    public static final StorageBeatCommand STORAGE_BEAT_COMMAND_HANDLER = new StorageBeatCommand();

    public static final StorageBeatCommand STORAGE_JOIN_COMMAND_HANDLER = new StorageBeatCommand();
    public static final ServiceQueryStoreWithoutGroupOneCommand SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE = new ServiceQueryStoreWithoutGroupOneCommand();
    public static final ServiceQueryStoreWithGroupOneCommand SERVICE_QUERY_STORE_WITH_GROUP_ONE = new ServiceQueryStoreWithGroupOneCommand();
    public static final ServiceQueryFetchOneCommand SERVICE_QUERY_FETCH_ONE = new ServiceQueryFetchOneCommand();
    public static final ServiceQueryUpdateCommand SERVICE_QUERY_UPDATE = new ServiceQueryUpdateCommand();

    public static TrackerCommand getHandler(byte cmd) {
        switch (cmd) {
            case TrackerProto.TRACKER_PROTO_CMD_STORAGE_BEAT:
                return STORAGE_BEAT_COMMAND_HANDLER;
            case TrackerProto.TRACKER_PROTO_CMD_STORAGE_JOIN:
                return STORAGE_JOIN_COMMAND_HANDLER;
            case TrackerProto.TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE:
                return SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE;
            case TrackerProto.TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITH_GROUP_ONE:
                return SERVICE_QUERY_STORE_WITH_GROUP_ONE;
            case TrackerProto.TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ONE:
                return SERVICE_QUERY_FETCH_ONE;
            case TrackerProto.TRACKER_PROTO_CMD_SERVICE_QUERY_UPDATE:
                return SERVICE_QUERY_UPDATE;
            default:
                throw new RuntimeException("command[" + cmd + "] not supported.");
        }
    }
}



