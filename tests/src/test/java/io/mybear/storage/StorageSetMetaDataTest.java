package io.mybear.storage;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.StorageClient1;

/**
 * Created by jamie on 2017/7/20.
 */
public class StorageSetMetaDataTest {
    public static void main(String[] args) throws Exception {
        TwoServerTest.startup();
        StorageClient1 client = TwoServerTest.getStorageClient();
        String group_name = "group1";
        String fileId = "fileId";
        String remote_filename = "remote_filename";
        long file_offset = 0;
        long download_bytes = 256;
        NameValuePair[] metaList = new NameValuePair[3];
        metaList[0] = new NameValuePair("fileName", "myname");
        metaList[1] = new NameValuePair("fileExtName", "myextn");
        metaList[2] = new NameValuePair("fileLength", String.valueOf(120));
        client.set_metadata(group_name, remote_filename, metaList, (byte) 0);
        client.set_metadata1("/data/0C/0D/AAAAAF6ihAmAAAAIAAD5SAAB4kAexe", metaList, (byte) 0);

    }
}
