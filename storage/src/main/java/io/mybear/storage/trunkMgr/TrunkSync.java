package io.mybear.storage.trunkMgr;

/**
 * Created by jamie on 2017/6/21.
 */
public class TrunkSync {
    public static final int TRUNK_BINLOG_BUFFER_SIZE = (64 * 1024);


//    int trunk_binlog_write(int timestamp, const char op_type, FdfsTrunkFullInfo pTrunk)
//    {
//        int result;
//        int write_ret;
//
//        trunk_binlog_write_cache_len += sprintf(trunk_binlog_write_cache_buff + \
//                trunk_binlog_write_cache_len, \
//                "%d %c %d %d %d %d %d %d\n", \
//                timestamp, op_type, \
//                pTrunk->path.store_path_index, \
//                pTrunk->path.sub_path_high, \
//                pTrunk->path.sub_path_low, \
//                pTrunk->file.id, \
//                pTrunk->file.offset, \
//                pTrunk->file.size);
//
//        //check if buff full
//        if (TRUNK_BINLOG_BUFFER_SIZE - trunk_binlog_write_cache_len < 128)
//        {
//            write_ret = trunk_binlog_fsync(false);  //sync to disk
//        }
//        else
//        {
//            write_ret = 0;
//        }
//
//        if ((result=pthread_mutex_unlock(&trunk_sync_thread_lock)) != 0)
//        {
//            logError("file: "__FILE__", line: %d, " \
//                    "call pthread_mutex_unlock fail, " \
//                    "errno: %d, error info: %s", \
//                    __LINE__, result, STRERROR(result));
//        }
//
//        return write_ret;
//    }
}
