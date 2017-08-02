package io.mybear.storage.storageNio;

import io.mybear.common.FDFSStorageServer;
import io.mybear.common.ThrowingConsumer;
import io.mybear.common.constants.config.StorageGlobal;
import io.mybear.storage.StorageFileContext;
import io.mybear.storage.StorageSetMetaInfo;
import io.mybear.storage.StorageUploadInfo;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static io.mybear.common.constants.config.StorageGlobal.g_storage_stat;

/**
 * Created by jamie on 2017/6/21.
 */
public class StorageClientInfo extends Connection implements Runnable, Serializable {
    final static int FDFS_STORAGE_ID_MAX_SIZE = 16;
    private static final long serialVersionUID = -832774976898639721L;
    /**
     * nio线程的索引
     */
    public int nioThreadIndex;  //nio thread index
    public boolean canceled;
    public int stage;  //nio stage, send or recv
    public byte[] storageServerId = new byte[FDFS_STORAGE_ID_MAX_SIZE];

    public StorageFileContext fileContext;

    public long totalLength;   //pkg total length for req and request
    public long totalOffset;   //pkg current offset for req and request

    public long requestLength;   //request pkg length for access log

    public FDFSStorageServer pSrcStorage;
    public ThrowingConsumer<StorageClientInfo> dealFunc;  //function pointer to deal this task
    public Object extraArg;   //store extra arg, such as (BinLogReader *)
    public ThrowingConsumer<StorageClientInfo> cleanFunc;  //clean function pointer when finished

    public StorageClientInfo(SocketChannel channel) {
        super(channel);
    }

    @Override
    public String getCharset() {
        return "ASCII";
    }

    public StorageSetMetaInfo makeMetaInfo(int filenameLength, int len) {
        StorageSetMetaInfo metaInfo = new StorageSetMetaInfo();
        metaInfo.filenameLength = filenameLength;
        metaInfo.metaBuff = new StringBuilder(len);
        this.fileContext.extra_info = metaInfo;
        return metaInfo;
    }

    public void appendMetaInfo(ByteBuffer nioData) {
        byte[] bytes = new byte[nioData.position()];
        nioData.flip();
        nioData.get(bytes);
        String s = new String(bytes);
        ((StorageSetMetaInfo) this.fileContext.extra_info).metaBuff.append(s);
    }

    public StorageSetMetaInfo getMetaInfo() {
        return ((StorageSetMetaInfo) this.fileContext.extra_info);
    }

    public void recycleMetaInfo() {
        this.fileContext.extra_info = null;
    }

    public StorageFileContext makeStorageFileContext() {
        StorageFileContext context = new StorageFileContext();
        this.fileContext = context;
        return context;
    }

    public void recycleStorageFileContext() {
        this.fileContext = null;
    }

    public StorageUploadInfo makStorageUploadInfo() {
        StorageUploadInfo uploadInfo = new StorageUploadInfo();
        this.fileContext.extra_info = uploadInfo;
        return uploadInfo;
    }

    public void recycleStorageUploadInfo() {
        this.fileContext.extra_info = null;
    }

    /**
     * void task_finish_clean_up(struct fast_task_info *pTask)
     *
     * @param
     */
    public void finishCleanUp() {
        if (this.cleanFunc != null) {
            this.cleanFunc.accept(this);
        }
        g_storage_stat.current_count.decrement();
        StorageGlobal.g_stat_change_count.increment();
    }

    @Override
    public void close(String reason) {
        finishCleanUp();
        super.close(reason);
    }

    @Override
    public void run() {
        if (dealFunc != null)
            dealFunc.accept(this);
    }
}
