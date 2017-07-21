package io.mybear.common;

import io.mybear.storage.storageNio.StorageClientInfo;

import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.zip.CRC32;

/**
 * Created by jamie on 2017/6/22.
 */
public class StorageFileContext implements Serializable{
    private static final long serialVersionUID = -655643703448865782L;

    public String filename = "";    //full filename char filename[MAX_PATH_SIZE + 128];

    /* FDFS logic filename to log not including group name */
    public String fname2log;//char fname2log[128+sizeof(FDFS_STORAGE_META_FILE_EXT)];

    public byte op;            //w for writing, r for reading, d for deleting etc.
    public byte syncFlag;     //sync flag log to binlog
    public boolean calcCrc32;    //if calculate file content hash code
    public boolean calcFileHash;      //if calculate file content hash code
    public StandardOpenOption openFlags;           //open file flags
    public int[] fileHashCodes;   //file hash code int fileHashCodes[4]
    public CRC32 crc32;   //file content crc32 signature
    public MessageDigest MD5CTX;//MD5CTX md5_context;

    public Object extra_info;//StorageUploadInfo or StorageSetMetaInfo


    public ExecutorService dioExecutorService;        //dio thread
    public int timestamp2log;        //timestamp to log
    public int deleteFlag;     //delete file flag
    public int createFlag;    //create file flag
    public int buffOffset;    //buffer offset after recv to write to file
    public FileChannel fileChannel;         //file description no
    public long start;  //the start offset of file
    public long end;    //the end offset of file
    public long offset; //the current offset of file
    public FileDealDoneCallback<StorageClientInfo> done_callback;
    public DeleteFileLogCallback<StorageClientInfo> log_callback;
    public RandomAccessFile randomAccessFile;


    public long tvDealStart; //task deal start tv for access log

}
