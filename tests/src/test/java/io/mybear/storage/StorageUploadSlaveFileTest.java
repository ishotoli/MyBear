package io.mybear.storage;


import org.csource.common.NameValuePair;
import org.csource.fastdfs.StorageClient1;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by jamie on 2017/6/20.
 */
public class StorageUploadSlaveFileTest {
    public static void main(String[] args) throws Exception {
        TwoServerTest.startup();
        StorageClient1 client = TwoServerTest.getStorageClient();
        NameValuePair[] meta_list = null;  //new NameValuePair[0];
        String item;
        String fileid;
        String name = System.getProperty("os.name");
        Path path = Paths.get(System.getProperty("user.dir") + "/lib/fastdfs-client-java-1.27-SNAPSHOT.jar");
        meta_list = new NameValuePair[2];
        meta_list[0] = new NameValuePair("width", "800");
        meta_list[1] = new NameValuePair("heigth", "600");
        String[] res = client.upload_file("hello", "test", "777777777777777jar", Files.readAllBytes(path), "ext666777777", meta_list);
        System.out.println(Arrays.toString(res));
        //  upload_file(String group_name, String master_filename, String prefix_name, byte[] file_buff, String file_ext_name, NameValuePair[] meta_list)
        meta_list = new NameValuePair[2];
        meta_list[0] = new NameValuePair("width", "800");
        meta_list[1] = new NameValuePair("heigth", "600");
        String[] res1 = client.upload_file("hello", "test", "jar", Files.readAllBytes(path), "file_ext_name", meta_list);
        System.out.println(Arrays.toString(res1));
        //      System.out.println(sc1.set_metadata1(fileid, meta_list, (byte) 0));

    }
}


