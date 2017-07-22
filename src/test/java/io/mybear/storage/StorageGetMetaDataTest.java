package io.mybear.storage;

import io.mybear.tracker.FdfsTrackerd;
import org.csource.fastdfs.*;

import java.net.InetSocketAddress;

/**
 * Created by jamie on 2017/7/20.
 */
public class StorageGetMetaDataTest {
    public static void main(String[] args) throws Exception {
        Thread trackerServiceServer = new Thread(() -> {
            try {
                FdfsTrackerd.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        trackerServiceServer.start();
        Thread storageServiceServer = new Thread(() -> {
            try {
                FdfsStoraged.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        storageServiceServer.start();
        Thread.sleep(2000);

        System.out.println("java.version=" + System.getProperty("java.version"));

        String group_name = "group1";
        String fileId = "fileId";
        String remote_filename = "/data/0C/0D/AAAAAF6ihAmAAAAIAAD5SAAB4kAexe";
        long file_offset = 0;
        long download_bytes = 256;

        try {

            System.out.println("network_timeout=" + ClientGlobal.g_network_timeout + "ms");
            System.out.println("charset=" + ClientGlobal.g_charset);
            TrackerGroup tg = new TrackerGroup(new InetSocketAddress[]{new InetSocketAddress("127.0.0.1", 22122)});
            TrackerClient tracker = new TrackerClient(tg);
            TrackerServer trackerServer = tracker.getConnection();
            StorageServer storageServer = null;
            StorageClient1 client = new StorageClient1(trackerServer, storageServer);

            for (int i = 0; i < 100; i++) {
                client.get_metadata(group_name, remote_filename);
                client.get_metadata1("/data/0C/0D/AAAAAF6ihAmAAAAIAAD5SAAB4kAexe");
            }

//            int res = client.delete_file1("hello/filedId");
//            System.out.println("delete res:" + res);

            trackerServer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            trackerServiceServer.stop();
            storageServiceServer.stop();
        }
    }
}
