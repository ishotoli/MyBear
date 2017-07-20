package io.mybear.storage;

import io.mybear.tracker.Tracker;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by jamie on 2017/7/20.
 */
public class StorageUploadFileTest {
    public static void main(String[] args) throws Exception {
        Thread tracker = new Thread(() -> {
            try {
                Tracker.main(args);
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
            if (name.toLowerCase().contains("windows")) {
                item = path.toString();
                for (int i = 0; i < 100; i++) {
                    fileid = sc1.upload_file1(item, "exe", meta_list);
                    System.out.println("Upload local file " + item + " ok, fileid=" + fileid);
                    fileid = sc1.upload_file1(item, "exe", meta_list);
                    System.out.println("Upload local file " + item + " ok, fileid=" + fileid);
                }
            } else {
                item = "/etc/hosts";
                fileid = sc1.upload_file1(item, "", meta_list);
            }

        } finally {
            tracker.stop();
            storage.stop();
        }

    }
}
