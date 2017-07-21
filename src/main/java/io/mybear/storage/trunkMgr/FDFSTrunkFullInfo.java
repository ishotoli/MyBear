//package io.mybear.storage.trunkMgr;
//
//
///**
// * Created by jamie on 2017/7/8.
// */
//public class FDFSTrunkFullInfo {
//
//
//    byte status;
//
//    int storePathIndex;   //store which path as Mxx
//    int subPathHigh;      //high sub dir index, front part of HH/HH
//    int subPathLow;       //low sub dir index, tail part of HH/HH
//
//
//    int id;      //trunk file id
//    int offset;  //file offset
//    int size;    //space size
//
//    public static int storageTrunkNodeCompareOffset(FDFSTrunkFullInfo f, FDFSTrunkFullInfo s) {
//        boolean pathBoolean =
//                (f.getSubPathHigh() == s.getSubPathHigh())
//                        || (f.getSubPathLow() == s.getSubPathLow())
//                        || (f.getStorePathIndex() == s.getSubPathHigh() || f.getId() == s.getId());
//        return pathBoolean ? f.getOffset() - s.getOffset() : 0;
//    }
//
//    public static int storageTrunkNodeCompareSize(FDFSTrunkFullInfo f, FDFSTrunkFullInfo s) {
//        return f.getSize() - s.getSize();
//    }
//
//    public byte getStatus() {
//        return status;
//    }
//
//    public void setStatus(byte status) {
//        this.status = status;
//    }
//
//    public int getStorePathIndex() {
//        return storePathIndex;
//    }
//
//    public void setStorePathIndex(int storePathIndex) {
//        this.storePathIndex = storePathIndex;
//    }
//
//    public int getSubPathHigh() {
//        return subPathHigh;
//    }
//
//    public void setSubPathHigh(int subPathHigh) {
//        this.subPathHigh = subPathHigh;
//    }
//
//    public int getSubPathLow() {
//        return subPathLow;
//    }
//
//    public void setSubPathLow(int subPathLow) {
//        this.subPathLow = subPathLow;
//    }
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public int getOffset() {
//        return offset;
//    }
//
//    public void setOffset(int offset) {
//        this.offset = offset;
//    }
//
//    public int getSize() {
//        return size;
//    }
//
//    public void setSize(int size) {
//        this.size = size;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        FDFSTrunkFullInfo that = (FDFSTrunkFullInfo) o;
//
//        if (status != that.status) return false;
//        if (storePathIndex != that.storePathIndex) return false;
//        if (subPathHigh != that.subPathHigh) return false;
//        if (subPathLow != that.subPathLow) return false;
//        if (id != that.id) return false;
//        if (offset != that.offset) return false;
//        return size == that.size;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = (int) status;
//        result = 31 * result + storePathIndex;
//        result = 31 * result + subPathHigh;
//        result = 31 * result + subPathLow;
//        result = 31 * result + id;
//        result = 31 * result + offset;
//        result = 31 * result + size;
//        return result;
//    }
////    public static Path storageTrunkGetDataFilename(Path full_filename) {
////        return Paths.get(g_fdfs_base_path,"/data/",STORAGE_TRUNK_DATA_FILENAME);
////    }
//
////    public static int trunkMemBinlogWrite(final int timestamp, final int op_type, final FDFSTrunkFullInfo pTrunk) {
////        synchronized (TrunkMem.trunkFileLock) {
////            if (op_type == TrunkSync.TRUNK_OP_TYPE_ADD_SPACE) {
////                TrunkMem.g_trunk_total_free_space += pTrunk.getSize();
////            } else if (op_type == TRUNK_OP_TYPE_DEL_SPACE) {
////                TrunkMem.g_trunk_total_free_space -= pTrunk.getSize();
////            }
////        }
////
////        return trunk_binlog_write(timestamp, op_type, pTrunk);
////    }
//////
////    int trunk_binlog_write(final int timestamp, final char op_type, final FDFSTrunkFullInfo pTrunk) {
////        int result;
////        int write_ret;
////
////        synchronized (trunk_sync_thread_lock){
////            //        if ((result=pthread_mutex_lock(&trunk_sync_thread_lock)) != 0)
//////        {
//////            logError("file: "__FILE__", line: %d, " \
//////                    "call pthread_mutex_lock fail, " \
//////                    "errno: %d, error info: %s", \
//////                    __LINE__, result, STRERROR(result));
//////        }
////        }
////
////
////        trunk_binlog_write_cache_len += sprintf(trunk_binlog_write_cache_buff + \
////                trunk_binlog_write_cache_len, \
////                "%d %c %d %d %d %d %d %d\n", \
////                timestamp, op_type, \
////                pTrunk->path.store_path_index, \
////                pTrunk->path.sub_path_high, \
////                pTrunk->path.sub_path_low, \
////                pTrunk->file.id, \
////                pTrunk->file.offset, \
////                pTrunk->file.size);
////
////        //check if buff full
////        if (TRUNK_BINLOG_BUFFER_SIZE - trunk_binlog_write_cache_len < 128)
////        {
////            write_ret = trunk_binlog_fsync(false);  //sync to disk
////        }
////        else
////        {
////            write_ret = 0;
////        }
////
////        if ((result=pthread_mutex_unlock(&trunk_sync_thread_lock)) != 0)
////        {
////            logError("file: "__FILE__", line: %d, " \
////                    "call pthread_mutex_unlock fail, " \
////                    "errno: %d, error info: %s", \
////                    __LINE__, result, STRERROR(result));
////        }
////
////        return write_ret;
////    }
//
//}
