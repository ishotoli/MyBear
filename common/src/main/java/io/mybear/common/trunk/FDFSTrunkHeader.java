package io.mybear.common.trunk;

import java.nio.ByteBuffer;

/**
 * Created by jamie on 2017/7/8.
 */
public class FDFSTrunkHeader {
    public byte fileType;
    public char[]/*6+2*/formattedExtName;
    public int allocSize;
    public int fileSize;
    public int crc32;
    public int mtime;

    /*
|||——————————————————— 24bytes——————-—————————|||
|—1byte   —|—4bytes    —|—4bytes —|—4bytes—|—4bytes —|—7bytes                      —|
|—filetype—|—alloc_size—|—filesize—|—crc32  —|—mtime —|—formatted_ext_name—|
|||——————file_data filesize bytes——————————————————————|||
|———————file_data————————————————————————————|

     */
    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(24);
        int offset = 0;
        byte fileType = byteBuffer.get(offset + 0);//1 byte
        int alloc_size = byteBuffer.getInt(offset + 1);
        int filesize = byteBuffer.getInt(offset + 6);
        int crc32 = byteBuffer.getInt(offset + 10);
        int mtime = byteBuffer.getInt(offset + 14);
        byte[] formatted_ext_name = new byte[7];
        byteBuffer.get(formatted_ext_name, 0, 7);

    }
}
