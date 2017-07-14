package io.mybear.tracker.types;

public class TrackerClientInfo {
    private FdfsGroupInfo group;

    private FdfsStorageDetail storage;

    // for notify storage servers
    private int trackerLeader;

    // for notify other tracker servers
    private int trunkServer;

    public FdfsGroupInfo getGroup() {
        return group;
    }

    public void setGroup(FdfsGroupInfo group) {
        this.group = group;
    }

    public FdfsStorageDetail getStorage() {
        return storage;
    }

    public void setStorage(FdfsStorageDetail storage) {
        this.storage = storage;
    }

    public int getTrackerLeader() {
        return trackerLeader;
    }

    public void setTrackerLeader(int trackerLeader) {
        this.trackerLeader = trackerLeader;
        this.trunkServer = trackerLeader;
    }

    public int getTrunkServer() {
        return trunkServer;
    }

    public void setTrunkServer(int trunkServer) {
        this.trunkServer = trunkServer;
        this.trackerLeader = trunkServer;
    }
}
