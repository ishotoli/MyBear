package io.mybear.tracker.command;

import io.mybear.tracker.TrackerProto;

public class TrackerCommandFactory {
    public static final StorageBeatCommand STORAGE_BEAT_COMMAND_HANDLER = new StorageBeatCommand();

    public static final StorageBeatCommand STORAGE_JOIN_COMMAND_HANDLER = new StorageBeatCommand();

    public static TrackerCommand getHandler(byte cmd){
        switch(cmd){
            case TrackerProto.TRACKER_PROTO_CMD_STORAGE_BEAT:
                return STORAGE_BEAT_COMMAND_HANDLER;

            case TrackerProto.TRACKER_PROTO_CMD_STORAGE_JOIN:
                return  STORAGE_JOIN_COMMAND_HANDLER;

            default:
                throw new RuntimeException("command[" + cmd + "] not supported.");
        }
    }
}
