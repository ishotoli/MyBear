package io.mybear;


import io.mybear.storage.Storage;
import io.mybear.tracker.Tracker;
import org.csource.fastdfs.*;

import java.net.InetSocketAddress;

/**
 * Created by jamie on 2017/6/20.
 */
public class TestStorageProtoCmdDownloadFile {

    public static void main(String[] args) throws Exception {
        Thread trackerServiceServer = new Thread(() -> {
            try {
                Tracker.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        trackerServiceServer.start();
        Thread.sleep(10);
        Thread storageServiceServer = new Thread(() -> {
            try {
                Storage.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        storageServiceServer.start();


        System.out.println("java.version=" + System.getProperty("java.version"));

        String group_name = "group_name";
        String fileId = "fileId";
        String remote_filename = "remote_filename";
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

            byte[] result = client.download_file(group_name, remote_filename, file_offset, download_bytes);
            System.out.println("download result is: " + result.length);

            trackerServer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            trackerServiceServer.stop();
            storageServiceServer.stop();
        }
    }

}


