package io.mybear.storage;

import io.mybear.tracker.FdfsTrackerd;
import org.csource.fastdfs.*;

import java.net.InetSocketAddress;

/**
 * Created by jamie on 2017/7/20.
 */
public class StorageDeleteFileTest {
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
            String group_name = "group1";
            String remote_filename = "remote_filename";
            StorageClient1 sc1 = new StorageClient1(ts, ss);
            sc1.delete_file1("group1/data/0C/0D/AAAAAF8oMI-AAAAIAAD5SAAB4kAexe");
            sc1.delete_file(group_name, remote_filename);
        } finally {
            tracker.stop();
            storage.stop();
        }

    }
}
