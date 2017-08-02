package io.mybear.storage;


import org.csource.fastdfs.StorageClient1;

/**
 * Created by jamie on 2017/6/20.
 */
public class StorageDownloadFileTest {

    public static void main(String[] args) throws Exception {
        TwoServerTest.startup();
        StorageClient1 client = TwoServerTest.getStorageClient();
        String group_name = "group1";
        String remote_filename = "group1/data/00/00/AAAAAKMb3nWAAAAfAAD5SAAB4kAexe";
        long file_offset = 0;
        long download_bytes = 256;
        for (int i = 0; i < 100; i++) {
            byte[] result = client.download_file(group_name, remote_filename, file_offset, download_bytes);
            System.out.println("download result is: " + result.length);
        }
    }

}


