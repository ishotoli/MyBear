package io.mybear.storage;

import io.mybear.common.ThrowingConsumer;
import io.mybear.common.trunk.FdfsTrunkFullInfo;
import io.mybear.storage.storageNio.StorageClientInfo;

import java.io.Serializable;
import java.util.Arrays;

import static io.mybear.common.constants.CommonConstant.FDFS_FILE_PREFIX_MAX_LEN;
import static io.mybear.common.constants.CommonConstant.FDFS_GROUP_NAME_MAX_LEN;
import static io.mybear.common.constants.config.FdfsGlobal.FDFS_FILE_EXT_NAME_MAX_LEN;


/**
 * Created by jamie on 2017/6/22.
 */
public class StorageUploadInfo implements Serializable {
    private static final long serialVersionUID = 3125803375110901181L;
    public ThrowingConsumer<StorageClientInfo> beforeOpenCallback;
    public ThrowingConsumer<StorageClientInfo> beforeCloseCallback;
    /**
     * if upload generate filename
     */
    private boolean ifGenFilename;
    /**
     * regular or link file
     */
    private char fileType;
    /**
     * if sub path alloced since V3.0
     */
    private boolean ifSubPathAlloced;
    private char[] masterFileName = new char[128];
    private char[] fileExtName = new char[FDFS_FILE_EXT_NAME_MAX_LEN + 1];
    private char[] formattedExtName = new char[FDFS_FILE_EXT_NAME_MAX_LEN + 2];
    private char[] prefixName = new char[FDFS_FILE_PREFIX_MAX_LEN + 1];
    /**
     * //the upload group name
     */
    private char[] groupName = new char[FDFS_GROUP_NAME_MAX_LEN + 1];
    /**
     * //upload start timestamp
     */
    private int startTime;
    private FdfsTrunkFullInfo trunkInfo;

    public boolean isIfGenFilename() {
        return ifGenFilename;
    }

    public void setIfGenFilename(boolean ifGenFilename) {
        this.ifGenFilename = ifGenFilename;
    }

    public char getFileType() {
        return fileType;
    }

    public void setFileType(char fileType) {
        this.fileType = fileType;
    }

    public boolean isIfSubPathAlloced() {
        return ifSubPathAlloced;
    }

    public void setIfSubPathAlloced(boolean ifSubPathAlloced) {
        this.ifSubPathAlloced = ifSubPathAlloced;
    }

    public char[] getMasterFileName() {
        return masterFileName;
    }

    public char[] getFileExtName() {
        return fileExtName;
    }

    public char[] getFormattedExtName() {
        return formattedExtName;
    }

    public char[] getPrefixName() {
        return prefixName;
    }

    public char[] getGroupName() {
        return groupName;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public FdfsTrunkFullInfo getTrunkInfo() {
        return trunkInfo;
    }

    public void setTrunkInfo(FdfsTrunkFullInfo trunkInfo) {
        this.trunkInfo = trunkInfo;
    }

    @Override
    public String toString() {
        return "StorageUploadInfo{" +
                "ifGenFilename=" + ifGenFilename +
                ", fileType=" + fileType +
                ", ifSubPathAlloced=" + ifSubPathAlloced +
                ", masterFileName=" + Arrays.toString(masterFileName) +
                ", fileExtName=" + Arrays.toString(fileExtName) +
                ", formattedExtName=" + Arrays.toString(formattedExtName) +
                ", prefixName=" + Arrays.toString(prefixName) +
                ", groupName=" + Arrays.toString(groupName) +
                ", startTime=" + startTime +
                ", trunkInfo=" + trunkInfo +
                ", beforeOpenCallback=" + beforeOpenCallback +
                ", beforeCloseCallback=" + beforeCloseCallback +
                '}';
    }
}
