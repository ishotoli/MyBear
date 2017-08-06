package io.mybear.storage;

import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.StorageClient1;

/**
 * Created by jamie on 2017/8/2.
 */
public class StorageQueryFileinfoTest {
    public static void main(String[] args) throws Exception {
        TwoServerTest.startup();
        StorageClient1 client = TwoServerTest.getStorageClient();
        String group_name = "group1";
        String remote_filename = "group1/data/00/00/AAAAAKMb3nWAAAAfAAD5SAAB4kAexe";
        FileInfo fileInfo = client.query_file_info(group_name, remote_filename);
        System.out.println(fileInfo.toString());
    }
}
