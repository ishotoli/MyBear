package io.mybear.storage;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.StorageClient1;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by jamie on 2017/7/20.
 */
public class StorageUploadFileTest {
    public static void main(String[] args) throws Exception {
        TwoServerTest.startup();
        StorageClient1 sc1 = TwoServerTest.getStorageClient();
        NameValuePair[] meta_list = null;  //new NameValuePair[0];
        String item;
        String fileid;
        String name = System.getProperty("os.name");
        Path path = Paths.get(System.getProperty("user.dir") + "/tests/lib/fastdfs-client-java-1.27-SNAPSHOT.jar");
        if (name.toLowerCase().contains("windows")) {
            item = path.toString();
            for (int i = 0; i < 9; i++) {
                fileid = sc1.upload_file1(item, "exe", meta_list);
                System.out.println(fileid);
            }
        } else {
            item = "/etc/hosts";
            fileid = sc1.upload_file1(item, "", meta_list);
        }
    }
}
