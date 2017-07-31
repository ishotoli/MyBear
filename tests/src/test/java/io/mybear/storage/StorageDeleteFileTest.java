package io.mybear.storage;

import org.csource.fastdfs.StorageClient1;

/**
 * Created by jamie on 2017/7/20.
 */
public class StorageDeleteFileTest {
    public static void main(String[] args) throws Exception {
        TwoServerTest.startup();
        StorageClient1 sc1 = TwoServerTest.getStorageClient();
        String group_name = "group1";
        String remote_filename = "remote_filename";
        sc1.delete_file1("group1/data/0C/0D/AAAAAF8oMI-AAAAIAAD5SAAB4kAexe");
        sc1.delete_file(group_name, remote_filename);
    }
}
