package io.mybear.common;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by jamie on 2017/6/22.
 */
public class StorageClientInfo {

    final static int FDFS_STORAGE_ID_MAX_SIZE = 16;
    /**
     * nio线程的索引
     */
    private int nioThreadIndex;  //nio thread index
    private boolean canceled;
    private int stage;  //nio stage, send or recv
    private byte[] storageServerId = new byte[FDFS_STORAGE_ID_MAX_SIZE];

    private StorageFileContext fileContext;

    private long totalLength;   //pkg total length for req and request
    private long totalOffset;   //pkg current offset for req and request

    private long requestLength;   //request pkg length for access log

    private FDFSStorageServer pSrcStorage;
    private Function<Object, Integer> dealFunc;  //function pointer to deal this task
    private Object extraArg;   //store extra arg, such as (BinLogReader *)
    private Consumer<Object> cleanFunc;  //clean function pointer when finished

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public byte[] getStorageServerId() {
        return storageServerId;
    }

    public void setStorageServerId(byte[] storageServerId) {
        this.storageServerId = storageServerId;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public long getTotalOffset() {
        return totalOffset;
    }

    public void setTotalOffset(long totalOffset) {
        this.totalOffset = totalOffset;
    }

    public long getRequestLength() {
        return requestLength;
    }

    public void setRequestLength(long requestLength) {
        this.requestLength = requestLength;
    }

    public FDFSStorageServer getpSrcStorage() {
        return pSrcStorage;
    }

    public void setpSrcStorage(FDFSStorageServer pSrcStorage) {
        this.pSrcStorage = pSrcStorage;
    }

    public Object getExtraArg() {
        return extraArg;
    }

    public void setExtraArg(Object extraArg) {
        this.extraArg = extraArg;
    }

    public int getNioThreadIndex() {
        return nioThreadIndex;
    }

    public void setNioThreadIndex(int nioThreadIndex) {
        this.nioThreadIndex = nioThreadIndex;
    }

    public StorageFileContext getFileContext() {
        return fileContext;
    }

    public void setFileContext(StorageFileContext fileContext) {
        this.fileContext = fileContext;
    }

    public Function<Object, Integer> getDealFunc() {
        return dealFunc;
    }

    public void setDealFunc(Function<Object, Integer> dealFunc) {
        this.dealFunc = dealFunc;
    }

    public Consumer<Object> getCleanFunc() {
        return cleanFunc;
    }

    public void setCleanFunc(Consumer<Object> cleanFunc) {
        this.cleanFunc = cleanFunc;
    }

    @Override
    public String toString() {
        return "StorageClientInfo{" +
                "nioThreadIndex=" + nioThreadIndex +
                ", canceled=" + canceled +
                ", stage=" + stage +
                ", storageServerId=" + Arrays.toString(storageServerId) +
                ", fileContext=" + fileContext +
                ", totalLength=" + totalLength +
                ", totalOffset=" + totalOffset +
                ", requestLength=" + requestLength +
                ", pSrcStorage=" + pSrcStorage +
                ", dealFunc=" + dealFunc +
                ", extraArg=" + extraArg +
                ", cleanFunc=" + cleanFunc +
                '}';
    }
}
