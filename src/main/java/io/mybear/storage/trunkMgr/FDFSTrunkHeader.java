package io.mybear.storage.trunkMgr;

/**
 * Created by jamie on 2017/7/8.
 */
public class FDFSTrunkHeader {
    // 'F' 和  'L' 两个值 F 是普通文件，L是软连接
    public byte fileType;
    public char[]/*6+2*/formattedExtName;
    public int allocSize;
    public int fileSize;
    public int crc32;
    public int mtime;
}
