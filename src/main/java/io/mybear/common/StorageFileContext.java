package io.mybear.common;

import java.util.Arrays;

/**
 * Created by jamie on 2017/6/22.
 */
public class StorageFileContext {
   private String fileName;    //full fileName char fileName[MAX_PATH_SIZE + 128];
    /* FDFS logic fileName to log not including group name */
    private String fname2log;//char fname2log[128+sizeof(FDFS_STORAGE_META_FILE_EXT)];
    private byte op;            //w for writing, r for reading, d for deleting etc.
    private byte syncFlag;     //sync flag log to binlog
    private boolean calcCrc32;    //if calculate file content hash code
    private boolean calcFileHash;      //if calculate file content hash code
    private int openFlags;           //open file flags
    private int[] fileHashCodes;   //file hash code int fileHashCodes[4]
    private int crc32;   //file content crc32 signature
    private String MD5_CTX;//MD5_CTX md5_context;
    private Object extraInfo;//StorageUploadInfo or StorageSetMetaInfo
    private int dioThreadIndex;        //dio thread index
    private int timestamp2Log;        //timestamp to log
    private int deleteFlag;     //delete file flag
    private int createFlag;    //create file flag
    private int buffOffset;    //buffer offset after recv to write to file
    private int fd;         //file description no
    private long start;  //the start offset of file
    private long end;    //the end offset of file
    private long offset; //the current offset of file
    private FileDealDoneCallback doneCallback;
    private DeleteFileLogCallback logCallback;
    private long tvDealStart; //task deal start tv for access log

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFname2log() {
        return fname2log;
    }

    public void setFname2log(String fname2log) {
        this.fname2log = fname2log;
    }

    public byte getOp() {
        return op;
    }

    public void setOp(byte op) {
        this.op = op;
    }

    public byte getSyncFlag() {
        return syncFlag;
    }

    public void setSyncFlag(byte syncFlag) {
        this.syncFlag = syncFlag;
    }

    public boolean isCalcCrc32() {
        return calcCrc32;
    }

    public void setCalcCrc32(boolean calcCrc32) {
        this.calcCrc32 = calcCrc32;
    }

    public boolean isCalcFileHash() {
        return calcFileHash;
    }

    public void setCalcFileHash(boolean calcFileHash) {
        this.calcFileHash = calcFileHash;
    }

    public int getOpenFlags() {
        return openFlags;
    }

    public void setOpenFlags(int openFlags) {
        this.openFlags = openFlags;
    }

    public int[] getFileHashCodes() {
        return fileHashCodes;
    }

    public void setFileHashCodes(int[] fileHashCodes) {
        this.fileHashCodes = fileHashCodes;
    }

    public int getCrc32() {
        return crc32;
    }

    public void setCrc32(int crc32) {
        this.crc32 = crc32;
    }

    public String getMD5_CTX() {
        return MD5_CTX;
    }

    public void setMD5_CTX(String MD5_CTX) {
        this.MD5_CTX = MD5_CTX;
    }

    public Object getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(Object extraInfo) {
        this.extraInfo = extraInfo;
    }

    public int getDioThreadIndex() {
        return dioThreadIndex;
    }

    public void setDioThreadIndex(int dioThreadIndex) {
        this.dioThreadIndex = dioThreadIndex;
    }

    public int getTimestamp2Log() {
        return timestamp2Log;
    }

    public void setTimestamp2Log(int timestamp2Log) {
        this.timestamp2Log = timestamp2Log;
    }

    public int getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(int deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public int getCreateFlag() {
        return createFlag;
    }

    public void setCreateFlag(int createFlag) {
        this.createFlag = createFlag;
    }

    public int getBuffOffset() {
        return buffOffset;
    }

    public void setBuffOffset(int buffOffset) {
        this.buffOffset = buffOffset;
    }

    public int getFd() {
        return fd;
    }

    public void setFd(int fd) {
        this.fd = fd;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public FileDealDoneCallback getDoneCallback() {
        return doneCallback;
    }

    public void setDoneCallback(FileDealDoneCallback doneCallback) {
        this.doneCallback = doneCallback;
    }

    public DeleteFileLogCallback getLogCallback() {
        return logCallback;
    }

    public void setLogCallback(DeleteFileLogCallback logCallback) {
        this.logCallback = logCallback;
    }

    public long getTvDealStart() {
        return tvDealStart;
    }

    public void setTvDealStart(long tvDealStart) {
        this.tvDealStart = tvDealStart;
    }

    @Override
    public String toString() {
        return "StorageFileContext{" +
                "fileName='" + fileName + '\'' +
                ", fname2log='" + fname2log + '\'' +
                ", op=" + op +
                ", syncFlag=" + syncFlag +
                ", calcCrc32=" + calcCrc32 +
                ", calcFileHash=" + calcFileHash +
                ", openFlags=" + openFlags +
                ", fileHashCodes=" + Arrays.toString(fileHashCodes) +
                ", crc32=" + crc32 +
                ", MD5_CTX='" + MD5_CTX + '\'' +
                ", extraInfo=" + extraInfo +
                ", dioThreadIndex=" + dioThreadIndex +
                ", timestamp2Log=" + timestamp2Log +
                ", deleteFlag=" + deleteFlag +
                ", createFlag=" + createFlag +
                ", buffOffset=" + buffOffset +
                ", fd=" + fd +
                ", start=" + start +
                ", end=" + end +
                ", offset=" + offset +
                ", doneCallback=" + doneCallback +
                ", logCallback=" + logCallback +
                ", tvDealStart=" + tvDealStart +
                '}';
    }
}
