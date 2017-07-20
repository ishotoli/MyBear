package io.mybear.storage.trunkMgr;

/**
 * Created by jamie on 2017/7/8.
 */
public class FDFSTrunkHeader {
    public byte fileType;
    public byte[]/*6+2*/formattedExtName;
    public int allocSize;
    public int fileSize;
    public int crc32;
    public int mtime;
}
