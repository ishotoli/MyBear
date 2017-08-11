package io.mybear.common.tracker;

import static io.mybear.common.constants.CommonConstant.*;
import static io.mybear.common.constants.TrackerProto.FDFS_PROTO_PKG_LEN_SIZE;

public class FdfsStorageJoinBody {

    //    ByteBuffer toByteBuffer(ByteBuffer buffer){
//
//    }
    public final static int SIZE =
            4 * (FDFS_GROUP_NAME_MAX_LEN + 1)//	char groupName[FDFS_GROUP_NAME_MAX_LEN+1];
                    + 4 * (FDFS_PROTO_PKG_LEN_SIZE)//char storage_port[FDFS_PROTO_PKG_LEN_SIZE];
                    + 4 * FDFS_PROTO_PKG_LEN_SIZE//storage_http_port[FDFS_PROTO_PKG_LEN_SIZE];
                    + 4 * FDFS_PROTO_PKG_LEN_SIZE//store_path_count[FDFS_PROTO_PKG_LEN_SIZE];
                    + 4 * FDFS_PROTO_PKG_LEN_SIZE//subdir_count_per_path[FDFS_PROTO_PKG_LEN_SIZE];
                    + 4 * FDFS_PROTO_PKG_LEN_SIZE//char upload_priority[FDFS_PROTO_PKG_LEN_SIZE];
                    + 4 * FDFS_PROTO_PKG_LEN_SIZE//join_time[FDFS_PROTO_PKG_LEN_SIZE];
                    + 4 * FDFS_PROTO_PKG_LEN_SIZE//up_time[FDFS_PROTO_PKG_LEN_SIZE];
                    + 4 * FDFS_VERSION_SIZE//version[FDFS_VERSION_SIZE];
                    + 4 * FDFS_DOMAIN_NAME_MAX_SIZE//domain_name
                    + 4//init_flag
                    + 4 //status
                    + 4 * FDFS_PROTO_PKG_LEN_SIZE;//tracker_count[FDFS_PROTO_PKG_LEN_SIZE];
    private int storagePort;
    private int storageHttpPort;
    private int storePathCount;
    private int subdirCountPerPath;
    private int uploadPriority;
    private int joinTime;
    private int upTime;

    private byte[] version = new byte[FDFS_VERSION_SIZE];
    private byte[] groupName = new byte[FDFS_GROUP_NAME_MAX_LEN + 1];
    private byte[] domainName = new byte[FDFS_DOMAIN_NAME_MAX_SIZE];

    private byte initFlag;
    private byte state;
    private long trackerCount;

    public int getStoragePort() {
        return storagePort;
    }

    public void setStoragePort(int storagePort) {
        this.storagePort = storagePort;
    }

    public int getStorageHttpPort() {
        return storageHttpPort;
    }

    public void setStorageHttpPort(int storageHttpPort) {
        this.storageHttpPort = storageHttpPort;
    }

    public int getStorePathCount() {
        return storePathCount;
    }

    public void setStorePathCount(int storePathCount) {
        this.storePathCount = storePathCount;
    }

    public int getSubdirCountPerPath() {
        return subdirCountPerPath;
    }

    public void setSubdirCountPerPath(int subdirCountPerPath) {
        this.subdirCountPerPath = subdirCountPerPath;
    }

    public int getUploadPriority() {
        return uploadPriority;
    }

    public void setUploadPriority(int uploadPriority) {
        this.uploadPriority = uploadPriority;
    }

    public int getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(int joinTime) {
        this.joinTime = joinTime;
    }

    public int getUpTime() {
        return upTime;
    }

    public void setUpTime(int upTime) {
        this.upTime = upTime;
    }

    public byte[] getVersion() {
        return version;
    }

    public byte[] getGroupName() {
        return groupName;
    }

    public byte[] getDomainName() {
        return domainName;
    }

    public byte getInitFlag() {
        return initFlag;
    }

    public void setInitFlag(byte initFlag) {
        this.initFlag = initFlag;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public long getTrackerCount() {
        return trackerCount;
    }

    public void setTrackerCount(long trackerCount) {
        this.trackerCount = trackerCount;
    }
}
