package io.mybear.tracker.types;

/**
 * @author Yangll
 * @create 2017-07-20 17:21
 */

public class FdfsGroups {
    private int allocSize;  //alloc group count

    private int count; //group count

    private FdfsGroupInfo groups;
    private FdfsGroupInfo sortedGroups; //groups order by group_name
    private FdfsGroupInfo pStroreGroup; //the group to store uploaded files

    private int currentWriteGroup; //current group index to upload file
    private byte storeLookup; //store to which group, from conf file
    private byte storeServer; //store to which storage server, from conf file
    private byte downloadServer; //download from which storage server, from conf file
    private byte storePath; //store to which path, from conf file
    private String storeGroup;

    public int getAllocSize() {
        return allocSize;
    }

    public void setAllocSize(int allocSize) {
        this.allocSize = allocSize;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public FdfsGroupInfo getGroups() {
        return groups;
    }

    public void setGroups(FdfsGroupInfo groups) {
        this.groups = groups;
    }

    public FdfsGroupInfo getSortedGroups() {
        return sortedGroups;
    }

    public void setSortedGroups(FdfsGroupInfo sortedGroups) {
        this.sortedGroups = sortedGroups;
    }

    public FdfsGroupInfo getpStroreGroup() {
        return pStroreGroup;
    }

    public void setpStroreGroup(FdfsGroupInfo pStroreGroup) {
        this.pStroreGroup = pStroreGroup;
    }

    public int getCurrentWriteGroup() {
        return currentWriteGroup;
    }

    public void setCurrentWriteGroup(int currentWriteGroup) {
        this.currentWriteGroup = currentWriteGroup;
    }

    public byte getStoreLookup() {
        return storeLookup;
    }

    public void setStoreLookup(byte storeLookup) {
        this.storeLookup = storeLookup;
    }

    public byte getStoreServer() {
        return storeServer;
    }

    public void setStoreServer(byte storeServer) {
        this.storeServer = storeServer;
    }

    public byte getDownloadServer() {
        return downloadServer;
    }

    public void setDownloadServer(byte downloadServer) {
        this.downloadServer = downloadServer;
    }

    public byte getStorePath() {
        return storePath;
    }

    public void setStorePath(byte storePath) {
        this.storePath = storePath;
    }

    public String getStoreGroup() {
        return storeGroup;
    }

    public void setStoreGroup(String storeGroup) {
        this.storeGroup = storeGroup;
    }
}
