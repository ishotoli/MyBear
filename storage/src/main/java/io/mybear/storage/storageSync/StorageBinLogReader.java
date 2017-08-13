package io.mybear.storage.storageSync;


import io.mybear.common.context.Base64Context;
import io.mybear.common.tracker.BinLogBuffer;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static io.mybear.common.constants.CommonConstant.FDFS_FILENAME_BASE64_LENGTH;

/**
 * Created by jamie on 2017/7/22.
 */
public class StorageBinLogReader {
    public String storageId; //char storage_id[FDFS_STORAGE_ID_MAX_SIZE];
    public boolean need_sync_old;
    public boolean sync_old_done;
    public boolean last_file_exist;   //if the last file exist on the dest server
    public BinLogBuffer binlog_buff;
    public long until_timestamp;
    public int mark_fd;
    public int binlog_index;
    public FileChannel fileChannel;
    public long binlog_offset;
    public long scan_row_count;
    public long sync_row_count;

    public long last_scan_rows;  //for write to mark file
    public long last_sync_rows;  //for write to mark file


    public static void main(String[] args) throws Exception {
        Stream<String> iter = Files.lines(Paths.get("D:\\MyBear2\\storage\\src\\main\\resources\\binlog.000"));
        Base64Context base64Context = new Base64Context();
        io.mybear.common.utils.Base64.base64InitEx(base64Context, 0, '-', '_', '.');
        iter.forEach((i) -> {
            String[] strings = i.split(" ");
            if (strings.length == 3) {
                String time = strings[0].trim();
                String op = strings[1].trim();
                String fileName = strings[2].trim();
                if (!"".equals(time) && !"".equals(op) && !"".equals(fileName)) {
                    int extNameIndex = fileName.lastIndexOf(".");
                    if (extNameIndex == -1) return;
                    String extName = fileName.substring(extNameIndex);
                    if ("".equals(extName)) return;
                    fileName = fileName.substring(0, extNameIndex);
                    if ("".equals(fileName)) return;
                    int filenameIndex = fileName.lastIndexOf("/");
                    if (filenameIndex == -1) return;
                    ++filenameIndex;
                    String path = fileName.substring(0, filenameIndex);
                    fileName = fileName.substring(filenameIndex, fileName.length());
                    //   byte[] bytes = Base64.getDecoder().decode(fileName);
                    byte[] chars = new byte[128];
                    io.mybear.common.utils.Base64.base64_decode_auto(base64Context, fileName.getBytes(StandardCharsets.US_ASCII), FDFS_FILENAME_BASE64_LENGTH, chars);
                    ByteBuffer buffer = ByteBuffer.wrap(chars);
                    int storageId = buffer.getInt(0);
                    int timeStamp = buffer.getInt(4);
//                    byte[] n = new byte[8];
//                    buffer.position(8);
//                    buffer.get(n);
//                    ByteBuffer c= ByteBuffer.wrap(n);
//                    c.position(8);
//                    System.out.println(dumpAsHex(c));
//                    c.position(8);
                    System.out.println(Integer.MAX_VALUE);

                    System.out.println(Integer.toUnsignedLong(Integer.MAX_VALUE));
                    long fileSize = buffer.getInt(12);
                    /*
                    （若原始值为32位则前面加入一个随机值填充，最终为64位）
                     */
                    if (fileSize == ((long) Integer.MAX_VALUE)) {
                        fileSize = buffer.getLong(8);
                    }
                    System.out.println(Integer.toBinaryString(0));
                    int crc32 = buffer.getInt(16);
                    System.out.println(fileName);
                }
            }
        });
    }

    public static long toLong(byte[] b)


    {

        long l = 0;


        l = b[0];


        l |= ((long) b[1] << 8);


        l |= ((long) b[2] << 16);


        l |= ((long) b[3] << 24);


        l |= ((long) b[4] << 32);


        l |= ((long) b[5] << 40);


        l |= ((long) b[6] << 48);


        l |= ((long) b[7] << 56);


        return l;

    }
}
