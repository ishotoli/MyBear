package io.mybear.storage;

import org.csource.fastdfs.StorageClient1;

/**
 * Created by jamie on 2017/7/20.
 */
public class StorageGetMetaDataTest {
    public static void main(String[] args) throws Exception {
        TwoServerTest.startup();
        StorageClient1 client = TwoServerTest.getStorageClient();
        String group_name = "group1";
        String fileId = "fileId";
        String remote_filename = "/data/0C/0D/AAAAAF6ihAmAAAAIAAD5SAAB4kAexe";
        long file_offset = 0;
        long download_bytes = 256;
        for (int i = 0; i < 100; i++) {
            client.get_metadata(group_name, remote_filename);
            client.get_metadata1("/data/0C/0D/AAAAAF6ihAmAAAAIAAD5SAAB4kAexe");
        }

    }
}
