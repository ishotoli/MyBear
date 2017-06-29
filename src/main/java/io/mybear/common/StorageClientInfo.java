package io.mybear.common;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by jamie on 2017/6/22.
 */
public class StorageClientInfo {
    final static int FDFS_STORAGE_ID_MAX_SIZE = 16;
    public int nio_thread_index;  //nio thread index
    public boolean canceled;
    public int stage;  //nio stage, send or recv
    public byte[] storage_server_id = new byte[FDFS_STORAGE_ID_MAX_SIZE];

    public StorageFileContext file_context;

    public long total_length;   //pkg total length for req and request
    public long total_offset;   //pkg current offset for req and request

    public long request_length;   //request pkg length for access log

    public FDFSStorageServer pSrcStorage;
    public Function<Object, Integer> deal_func;  //function pointer to deal this task
    public Object extra_arg;   //store extra arg, such as (BinLogReader *)
    public Consumer<Object> clean_func;  //clean function pointer when finished
}
