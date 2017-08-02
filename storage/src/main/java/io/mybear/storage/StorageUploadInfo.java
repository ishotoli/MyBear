package io.mybear.storage;

import io.mybear.common.ThrowingConsumer;
import io.mybear.common.trunk.FdfsTrunkFullInfo;
import io.mybear.storage.storageNio.StorageClientInfo;

import java.io.Serializable;
import java.util.Arrays;

import static io.mybear.common.constants.CommonConstant.FDFS_FILE_PREFIX_MAX_LEN;
import static io.mybear.common.constants.config.FdfsGlobal.FDFS_FILE_EXT_NAME_MAX_LEN;
import static io.mybear.storage.StorageDio.*;


/**
 * Created by jamie on 2017/6/22.
 */
public class StorageUploadInfo implements Serializable {
    private static final long serialVersionUID = 3125803375110901181L;
    public ThrowingConsumer<StorageClientInfo> beforeOpenCallback;
    public ThrowingConsumer<StorageClientInfo> beforeCloseCallback;
    /**
     * //the upload group name
     */
    public byte[] groupName;
    /**
     * if upload generate filename
     */
    private boolean ifGenFilename;
    /**
     * regular or link file
     */
    private int fileType;
    /**
     * if sub path alloced since V3.0
     */
    private boolean ifSubPathAlloced;
    private char[] masterFileName = new char[128];
    private char[] fileExtName = new char[FDFS_FILE_EXT_NAME_MAX_LEN + 1];
    private char[] formattedExtName = new char[FDFS_FILE_EXT_NAME_MAX_LEN + 2];
    private char[] prefixName = new char[FDFS_FILE_PREFIX_MAX_LEN + 1];
    /**
     * //upload start timestamp
     */
    private int startTime;
    private FdfsTrunkFullInfo trunkInfo;

    /**
     * #define _FILE_TYPE_APPENDER  1
     * #define _FILE_TYPE_TRUNK     2   //if trunk file, since V3.0
     * #define _FILE_TYPE_SLAVE     4
     * #define _FILE_TYPE_REGULAR   8
     * #define _FILE_TYPE_LINK     16
     */
    public boolean isTRUNK() {
        return (fileType & _FILE_TYPE_TRUNK) == _FILE_TYPE_TRUNK;
    }

    public boolean isAPPENDER() {
        return (fileType & _FILE_TYPE_APPENDER) == _FILE_TYPE_APPENDER;
    }

    public boolean isSLAVE() {
        return (fileType & _FILE_TYPE_SLAVE) == _FILE_TYPE_SLAVE;
    }

    public boolean isREGULAR() {
        return (fileType & _FILE_TYPE_REGULAR) == _FILE_TYPE_REGULAR;
    }

    public boolean isLINK() {
        return (fileType & _FILE_TYPE_LINK) == _FILE_TYPE_LINK;
    }

    public void setIfGenFilename(boolean ifGenFilename) {
        this.ifGenFilename = ifGenFilename;
    }

    public int getFileType() {
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
