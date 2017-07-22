package io.mybear.storage;


import io.mybear.tracker.FdfsTrackerd;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by jamie on 2017/6/20.
 */
public class StorageUploadSlaveFileTest {
    public static void main(String[] args) throws Exception {
        Thread tracker = new Thread(() -> {
            try {
                FdfsTrackerd.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        tracker.start();
        Thread.sleep(10);
        Thread storage = new Thread(() -> {
            try {
                FdfsStoraged.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        storage.start();
        Thread.sleep(2000);
        try {
            TrackerGroup tg = new TrackerGroup(new InetSocketAddress[]{new InetSocketAddress("127.0.0.1", 22122)});
            TrackerClient tc = new TrackerClient(tg);
            TrackerServer ts = tc.getConnection();
            if (ts == null) {
                System.out.println("getConnection return null");
                return;
            }
            StorageServer ss = tc.getStoreStorage(ts);
            if (ss == null) {
                System.out.println("getStoreStorage return null");
            }
            StorageClient1 sc1 = new StorageClient1(ts, ss);
            NameValuePair[] meta_list = null;  //new NameValuePair[0];
            String item;
            String fileid;
            String name = System.getProperty("os.name");
            Path path = Paths.get(System.getProperty("user.dir") + "/lib/fastdfs-client-java-1.27-SNAPSHOT.jar");
            meta_list = new NameValuePair[2];
            meta_list[0] = new NameValuePair("width", "800");
            meta_list[1] = new NameValuePair("heigth", "600");
            String[] res = sc1.upload_file("hello", "test", "777777777777777jar", Files.readAllBytes(path), "ext666777777", meta_list);
            System.out.println(Arrays.toString(res));
            //  upload_file(String group_name, String master_filename, String prefix_name, byte[] file_buff, String file_ext_name, NameValuePair[] meta_list)
            meta_list = new NameValuePair[2];
            meta_list[0] = new NameValuePair("width", "800");
            meta_list[1] = new NameValuePair("heigth", "600");
            String[] res1 = sc1.upload_file("hello", "test", "jar", Files.readAllBytes(path), "file_ext_name", meta_list);
            System.out.println(Arrays.toString(res1));
            //      System.out.println(sc1.set_metadata1(fileid, meta_list, (byte) 0));
        } finally {
            tracker.stop();
            storage.stop();
        }

    }
}


