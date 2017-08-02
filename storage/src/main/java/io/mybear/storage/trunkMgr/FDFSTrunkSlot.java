package io.mybear.storage.trunkMgr;//package io.mybear.storage.trunkMgr;
//
//import io.mybear.common.FdfsTrunkFileInfo;
//import io.mybear.common.FdfsTrunkFullInfo;
//import io.mybear.common.FdfsTrunkPathInfo;
//import io.mybear.storage.StorageUploadInfo;
//import io.mybear.storage.storageNio.StorageClientInfo;
//
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.nio.ByteBuffer;
//import java.nio.MappedByteBuffer;
//import java.nio.channels.FileChannel;
//import java.nio.file.Paths;
//import java.nio.file.StandardOpenOption;
//import java.util.*;
//import java.util.concurrent.ThreadLocalRandom;
//
//import static io.mybear.common.constants.ErrorNo.EEXIST;
//import static io.mybear.storage.trunkMgr.TrunkMem.g_current_trunk_file_id;
//import static io.mybear.storage.trunkMgr.TrunkMem.g_slot_min_size;
//import static io.mybear.storage.trunkMgr.TrunkShared.FDFS_TRUNK_FILE_TYPE_NONE;
//import static io.mybear.storage.trunkMgr.TrunkShared.FDFS_TRUNK_STATUS_FREE;
//import static io.mybear.storage.trunkMgr.TrunkShared.FDFS_TRUNK_STATUS_HOLD;
//
///**
// * Created by jamie on 2017/7/25.
// */
//public class FDFSTrunkSlot implements Comparable<FDFSTrunkSlot> {
//    public static class FDFSTrunkNode implements Comparable<FDFSTrunkNode> {
//        public int size;
//        public int offset;
//        public FdfsTrunkFullInfo trunkFullInfo;
//
//        @Override
//        public int compareTo(FDFSTrunkNode o) {
//            return trunkFullInfo.getStatus() - o.trunkFullInfo.getStatus();
//        }
//
//        public FDFSTrunkNode split(int s) {
//            FDFSTrunkNode slot = new FDFSTrunkNode();
//            this.size -= s;
//            slot.offset = this.offset + this.size;
//            slot.size = s;
//            slot.trunkFullInfo = this.trunkFullInfo;
//            return slot;
//        }
//
//        public boolean isFull() {
//            return (size - offset) < g_slot_min_size;
//        }
//    }
//
//    public static class TrunkFileIdentifier {
//        public FdfsTrunkPathInfo pathInfo;
//        public int id;
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//
//            TrunkFileIdentifier that = (TrunkFileIdentifier) o;
//
//            if (id != that.id) return false;
//            return pathInfo != null ? pathInfo.equals(that.pathInfo) : that.pathInfo == null;
//        }
//
//        @Override
//        public int hashCode() {
//            int result = pathInfo != null ? pathInfo.hashCode() : 0;
//            result = 31 * result + id;
//            return result;
//        }
//    }
//
//    public int size;    //space size
//
//    static final NavigableMap<Integer, PriorityQueue<FdfsTrunkFullInfo>> TREE_INFO_BY_SIZES = new TreeMap<>();
//    static final Map<TrunkFileIdentifier, BlockArray> TREE_INFO_BY_ID = new HashMap<>();
//
//    //    public static int freeSpace(FdfsTrunkFullInfo pTrunkInfo) {
////        PriorityQueue<FDFSTrunkNode> list = TREE_INFO_BY_SIZES.get(size);
////        if (list!=null){
////            FDFSTrunkNode node=new FDFSTrunkNode();
////            node.offset=pTrunkInfo.offset;
////
////            list.offer()
////        }else {
////
////        }
////
////    }
//    static int trunkFreeBlockInsert(FdfsTrunkFullInfo pTrunkInfo) {
//        TrunkFileIdentifier id = pTrunkInfo.genTrunkFileIdentifier();
//        BlockArray list = TREE_INFO_BY_ID.get(id);
//        if (list != null) {
//            list = new BlockArray();
//            TREE_INFO_BY_ID.put(id, list);
//        }
//        return trunkFreeBlockDoInsert(pTrunkInfo, list);
//    }
//
//    static int trunkFreeBlockDelete(FdfsTrunkFullInfo pTrunkInfo) {
//        TrunkFileIdentifier id = pTrunkInfo.genTrunkFileIdentifier();
//        BlockArray list = TREE_INFO_BY_ID.get(id);
//        list.blocks.remove(pTrunkInfo);
//        if (list.blocks.size() == 0) {
//            TREE_INFO_BY_ID.remove(list);
//        }
//        list.blocks.sort(Comparator.comparingInt(x -> x.getFile().getOffset()));
//        return 0;
//    }
//
//    static int trunkFreeBlockDoInsert(FdfsTrunkFullInfo pTrunkInfo, BlockArray list) {
//        //把trunk插到对应的位置
//        list.blocks.add(pTrunkInfo);
//        list.blocks.sort(Comparator.comparingInt(x -> x.getFile().getOffset()));
//        return 0;
//    }
//
//    /**
//     * 检查有没有重复块
//     *
//     * @param pTrunkInfo
//     * @return
//     */
//    static boolean trunkFreeBlockCheckDuplicate(FdfsTrunkFullInfo pTrunkInfo) {
//        TrunkFileIdentifier id = pTrunkInfo.genTrunkFileIdentifier();
//        BlockArray list = TREE_INFO_BY_ID.get(id);
//        if (list == null) {
//            return true;
//        }
//        if (list.blocks.size() == 0) {
//            return true;
//        }
//        return list.blocks.contains(pTrunkInfo);
//    }
//
//    static int trunkDeleteSpace(FdfsTrunkFullInfo pTrunkInfo, boolean bWriteBinLog) {
//        int size = pTrunkInfo.getFile().getSize();
//        PriorityQueue<FdfsTrunkFullInfo> chain = TREE_INFO_BY_SIZES.get(size);
//        chain.remove(pTrunkInfo);
//        if (chain.size() == 0) {
//            TREE_INFO_BY_SIZES.remove(size);
//        }
//        trunkFreeBlockDelete(pTrunkInfo);
//    }
//
//    int trunkAllocConfirm(FdfsTrunkFullInfo pTrunkInfo, int status) {
//        if (status == 0) {
//            return trunkDeleteSpace(pTrunkInfo, true);
//        } else if (EEXIST == status) {
//            return trunkDeleteSpace(pTrunkInfo, true);
//        }
//       return trunkRestoreNode(pTrunkInfo);
//
//    }
//
//    static int trunkRestoreNode(FdfsTrunkFullInfo pNode) {
//        pNode.setStatus(FDFS_TRUNK_STATUS_FREE);
//        return 0;
//    }
//
//    static int trunkAddFreeBlock(FdfsTrunkFullInfo pNode, boolean bWriteBinLog) {
//        //检查有没有重复块
//        if (!trunkFreeBlockCheckDuplicate(pNode)) {
//            return 0;
//        }
//        PriorityQueue<FdfsTrunkFullInfo> chain = TREE_INFO_BY_SIZES.get(pNode.getFile().getSize());
//        if (chain == null) {
//            chain = new PriorityQueue<>();
//            chain.offer(pNode);
//            TREE_INFO_BY_SIZES.put(pNode.getFile().getSize(), chain);
//        } else {
//            chain.offer(pNode);
//        }
//        trunkFreeBlockInsert(pNode);
//        return 0;
//    }
//
//    public static FdfsTrunkFullInfo allocSpace(int size) {
//        FdfsTrunkFullInfo node1 = null;
//        PriorityQueue<FdfsTrunkFullInfo> queue = null;
//        Map.Entry<Integer, PriorityQueue<FdfsTrunkFullInfo>> listEntry = TREE_INFO_BY_SIZES.floorEntry(size);
//        if (listEntry != null) {
//            queue = listEntry.getValue();
//            Iterator<FdfsTrunkFullInfo> iterator = queue.iterator();
//            while (iterator.hasNext()) {
//                FdfsTrunkFullInfo node = iterator.next();
//                if (node.getStatus() == FDFS_TRUNK_STATUS_FREE) {
//                    iterator.remove();
//                    if (queue.size() == 0) {
//                        TREE_INFO_BY_SIZES.remove(listEntry.getKey());
//                    }
//                    //找到合适的了
//                    node1 = node;
//                    break;
//                }
//            }
//        }
//        if (node1 == null) {
//
//            //新建文件
//            FdfsTrunkFullInfo trunkFullInfo = new FdfsTrunkFullInfo();
//            FdfsTrunkPathInfo path = new FdfsTrunkPathInfo();
//            FdfsTrunkFileInfo file = new FdfsTrunkFileInfo();
//            trunkFullInfo.setPath(path);
//            trunkFullInfo.setFile(file);
//            trunkFullInfo.setStatus(FDFS_TRUNK_STATUS_FREE);
//            trunkFullInfo.getFile().setSize(64 * 1024);
//            trunkFullInfo.getFile().setOffset(0);
//            //文件名
//            trunkFullInfo.getPath().setStorePathIndex(0);
//            trunkFullInfo.getPath().setSubPathHigh(0);
//            trunkFullInfo.getPath().setSubPathLow(0);
//
//            node1 = trunkFullInfo;
//        }
//        //split
//        FdfsTrunkFullInfo result = node1.split(size);
//        final FdfsTrunkFullInfo old = node1;
//
//        if (result != null) {
//            TREE_INFO_BY_ID.compute(old.genTrunkFileIdentifier(), (k, v) -> {
//                List<FdfsTrunkFullInfo> list;
//                if (k == null) {
//                    list = new ArrayList<>();
//                    list.add(old);
//                    return list;
//                } else {
//                    v.add(old);
//                    return v;
//                }
//            });
//        }
//        result.setStatus(FDFS_TRUNK_STATUS_HOLD);
//        return result;
//    }
//
//    public static void main(String[] args) throws Exception {
//        int size = 5 * 1024;
////        FdfsTrunkFullInfo trunkFullInfo = allocSpace(size);
//        FileChannel fileChannel = FileChannel.open(Paths.get("d:/d.conf"), StandardOpenOption.APPEND);
//        fileChannel.position(20000000);
//        fileChannel.write(ByteBuffer.wrap(new byte[]{1, 1, 1, 1, 11, 1}), 200000);
//
//        FileChannel fileChannel2 = FileChannel.open(Paths.get("d:/d.conf"), StandardOpenOption.APPEND);
//        fileChannel2.position(100000);
//        fileChannel2.write(ByteBuffer.wrap(new byte[]{22, 1, 1, 1, 11, 1}), 20);
//    }
//
//    static ByteBuffer gputTrunkHeader(int offset, ByteBuffer byteBuffer, byte fileType, int alloc_size, int filesize, int crc32, int mtime, byte[] formatted_ext_name) {
//        byteBuffer.put(offset + 0, fileType);//1 byte
//        byteBuffer.putInt(offset + 1, alloc_size);
//        byteBuffer.putInt(offset + 6, filesize);
//        byteBuffer.putInt(offset + 10, crc32);
//        byteBuffer.putInt(offset + 14, mtime);
//        byteBuffer.put(formatted_ext_name, 0, 7);
//        return byteBuffer;
//    }
//
//    static ByteBuffer dofield(int offset, ByteBuffer byteBuffer) {
//        byte fileType = byteBuffer.get(offset + 0);//1 byte
//        int alloc_size = byteBuffer.getInt(offset + 1);
//        int filesize = byteBuffer.getInt(offset + 6);
//        int crc32 = byteBuffer.getInt(offset + 10);
//        int mtime = byteBuffer.getInt(offset + 14);
//        byte[] formatted_ext_name = new byte[7];
//        byteBuffer.get(formatted_ext_name, 0, 7);
//        return byteBuffer;
//    }
//
//    @Override
//    public int compareTo(FDFSTrunkSlot o) {
//        return Integer.compare(this.size, o.size);
//    }
//
//    MappedByteBuffer open(String trunk_filename) throws IOException {
//        RandomAccessFile randomAccessFile = new RandomAccessFile("d:/test", "rw");
//        MappedByteBuffer byteBuffer = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 64 * 1024);
//        return byteBuffer;
//    }
//
//
//    static ByteBuffer alloc() {
//        return ByteBuffer.allocate(128 * 1024);
//    }
//
////
////    int delete(String trunk_filename) throws IOException {
////        int remain_bytes;
////        try (RandomAccessFile randomAccessFile = new RandomAccessFile("d:/test", "rw")) {
////            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(64 * 1024);
////            byteBuffer.put(offset + 0, FDFS_TRUNK_FILE_TYPE_NONE);//1 byte
////            byteBuffer.putInt(offset + 1, size);
////            byteBuffer.limit(24);
////            FileChannel fileChannel = randomAccessFile.getChannel();
////            fileChannel.position(offset);
////            fileChannel.write(byteBuffer);
////
////            //clear
////            byteBuffer.put(offset + 0, (byte) 0);//1 byte
////            byteBuffer.putInt(offset + 1, (byte) 0);
////            byteBuffer.position(0).limit(byteBuffer.capacity());
////
////
////            int rest = size % byteBuffer.capacity();
////            remain_bytes = size - rest;
////            byteBuffer.limit(rest);
////            while (fileChannel.write(byteBuffer) > 0 && (remain_bytes > 0)) ;
////            while (fileChannel.write(byteBuffer) > 0 && (remain_bytes > 0)) {
////                byteBuffer.position(0).limit(byteBuffer.capacity());
////                remain_bytes -= byteBuffer.capacity();
////            }
////        }
////        return 0;
////    }
////
////
////    public void write(StorageClientInfo clientInfo) throws IOException {
////        if (clientInfo.fileContext.offset == 0) {
////            int size = (int) clientInfo.fileContext.end;
////            StorageUploadInfo uploadInfo = (StorageUploadInfo) clientInfo.extraArg;
////            Map.Entry<Integer, FDFSTrunkSlot> kv = SET.floorEntry(size);
////            if (kv != null) {
////                SET.remove(kv.getKey());
////                FDFSTrunkSlot slot = kv.getValue();
////                uploadInfo.getTrunkInfo().getFile().
////            } else {
////                //next
////            }
////        } else {
////            clientInfo.fileContext.offset += slot.channel.write(clientInfo.readBuffer);
////            if (clientInfo.fileContext.offset >= clientInfo.fileContext.end) {
////
////            }
////        }
////
////
////    }
//
//
//}
