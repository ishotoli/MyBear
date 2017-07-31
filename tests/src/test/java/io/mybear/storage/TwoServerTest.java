package io.mybear.storage;

import io.mybear.tracker.FdfsTrackerd;
import org.csource.fastdfs.*;

import java.net.InetSocketAddress;

/**
 * Created by jamie on 2017/7/31.
 */
public class TwoServerTest {
    public static void startup() throws Exception {
        trackerStartup();
        storageStartup();
    }

    public static void storageStartup() throws Exception {
        String[] args = new String[0];
        Thread storage = new Thread(() -> {
            try {
                FdfsStoraged.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        storage.start();
        Thread.sleep(1000);
    }

    public static void trackerStartup() throws Exception {
        String[] args = new String[0];
        Thread tracker = new Thread(() -> {
            try {
                FdfsTrackerd.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        tracker.start();
        Thread.sleep(1000);
    }

    public static StorageClient1 getStorageClient() throws Exception {
        TrackerGroup tg = new TrackerGroup(new InetSocketAddress[]{new InetSocketAddress("127.0.0.1", 22122)});
        TrackerClient tc = new TrackerClient(tg);
        TrackerServer ts = tc.getConnection();
        if (ts == null) {
            System.out.println("getConnection return null");
            System.exit(0);
        }
        StorageServer ss = tc.getStoreStorage(ts);
        if (ss == null) {
            System.out.println("getStoreStorage return null");
            System.exit(0);
        }
        StorageClient1 sc1 = new StorageClient1(ts, ss);
        return sc1;
    }
}
