package io.mybear.common.tracker;

import java.nio.ByteBuffer;

/**
 * Created by jamie on 2017/7/22.
 */
public class BinLogBuffer {
    //    char *buffer;  //the buffer pointer
//    char *current; //pointer to current position
//    int length;    //the content length
    public ByteBuffer buffer;
    public int version;   //for binlog pre-read, compare with binlog_write_version
}
