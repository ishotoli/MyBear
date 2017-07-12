package io.mybear.tracker.command;

import io.mybear.tracker.TrackerProto;

public class TrackerCommandFactory {
    public static TrackerCommand getHandler(byte cmd){
        switch(cmd){
            case TrackerProto.TRACKER_PROTO_CMD_STORAGE_BEAT:
                return new StorageBeatCommand();
            default:
                throw new RuntimeException("command[" + cmd + "] not supported.");
        }
    }
}
