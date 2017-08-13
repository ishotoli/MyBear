package io.mybear.common.tracker;

import java.util.List;
import java.util.TreeMap;

/**
 * @author Yangll
 * @create 2017-07-20 17:21
 */

public class FdfsGroups {
    private int allocSize;  //alloc group count

    private int count; //group count

    private List<FdfsGroupInfo> groups;
    private TreeMap<String, FdfsGroupInfo> sortedGroups = new TreeMap<>(); //groups order by groupName
    private List<FdfsGroupInfo> pStroreGroup; //the group to store uploaded files

    private int currentWriteGroup; //current group index to upload file
    private byte storeLookup; //store to which group, from conf file
    private byte storeServer; //store to which storage server, from conf file
    private byte downloadServer; //download from which storage server, from conf file
    private byte storePath; //store to which path, from conf file
    private String storeGroup;

    /**
     * tracker_mem_get_group_ex
     *
     * @param group_name
     * @return
     */
    public FdfsGroupInfo getGroup(String group_name) {
        return sortedGroups.get(group_name);
    }

    /**
     * static void tracker_mem_insert_into_sorted_groups(FDFSGroups *pGroups, \
     *
     * @param
     * @return
     */
    public void insertIntoSortedGroups(FdfsGroupInfo pTargetGroup) {
        sortedGroups.put(pTargetGroup.getGroupName(), pTargetGroup);
    }


    /**
     * tracker_mem_insert_into_sorted_servers
     */
    public void insertIntoSortedServers(List<FdfsStorageDetail> list, FdfsStorageDetail pTargetServer) {
        list.add(pTargetServer);
        list.sort(FdfsStorageDetail::cmpByStorageId);
    }

    /**
     * tracker_mem_destroy_groups
     *
     * @param saveFiles
     * @return
     */
    public void destroy(boolean saveFiles) {

    }

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

    public List<FdfsGroupInfo> getGroups() {
        return groups;
    }

    public void setGroups(List<FdfsGroupInfo> groups) {
        this.groups = groups;
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
