package io.mybear.common;

/**
 * Created by jamie on 2017/6/22.
 */
public class StorageFileContext {
    String filename;    //full filename char filename[MAX_PATH_SIZE + 128];

    /* FDFS logic filename to log not including group name */
    String fname2log;//char fname2log[128+sizeof(FDFS_STORAGE_META_FILE_EXT)];

    byte op;            //w for writing, r for reading, d for deleting etc.
    byte sync_flag;     //sync flag log to binlog
    boolean calc_crc32;    //if calculate file content hash code
    boolean calc_file_hash;      //if calculate file content hash code
    int open_flags;           //open file flags
    int[] file_hash_codes;   //file hash code int file_hash_codes[4]
    int crc32;   //file content crc32 signature
    String MD5_CTX;//MD5_CTX md5_context;

    Object extra_info;//StorageUploadInfo or StorageSetMetaInfo


    int dio_thread_index;        //dio thread index
    int timestamp2log;        //timestamp to log
    int delete_flag;     //delete file flag
    int create_flag;    //create file flag
    int buff_offset;    //buffer offset after recv to write to file
    int fd;         //file description no
    long start;  //the start offset of file
    long end;    //the end offset of file
    long offset; //the current offset of file
    FileDealDoneCallback done_callback;
    DeleteFileLogCallback log_callback;

    long tv_deal_start; //task deal start tv for access log
}
