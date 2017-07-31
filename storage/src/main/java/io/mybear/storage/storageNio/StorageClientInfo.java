package io.mybear.storage.storageNio;

import io.mybear.common.FDFSStorageServer;
import io.mybear.storage.StorageFileContext;

import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;
import java.util.function.Function;

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
    public Function<StorageClientInfo, Integer> dealFunc;  //function pointer to deal this task
    public Object extraArg;   //store extra arg, such as (BinLogReader *)
    public Consumer<Object> cleanFunc;  //clean function pointer when finished

    public StorageClientInfo(SocketChannel channel) {
        super(channel);
    }

    @Override
    public String getCharset() {
        return "ASCII";
    }

    @Override
    public void run() {
        if (dealFunc != null)
            dealFunc.apply(this);
    }
}
