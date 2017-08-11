package io.mybear.storage.trackerClient;

import static io.mybear.common.constants.config.StorageGlobal.*;
import static java.lang.Thread.sleep;

/**
 * Created by jamie on 2017/6/21.
 */
public class TrackerClientThread implements Runnable {
    public static void tracker_report_thread_start(TrackerClientConnection con) throws Exception {
        int serverCount = 1;
//        for (int i = 0; i < serverCount; i++) {
//            new TrackerClientConnection(ByteBuffer.allocateDirect(256), "127.0.0.1", 22122);
//        }
        boolean sync_old_done = g_sync_old_done;
        while (g_continue_flag && g_tracker_reporter_count < serverCount) {
            sleep(1); //waiting for all thread started
        }

        int result = 0;
        int previousCode = 0;
        int nContinuousFail = 0;

        while (g_continue_flag) {
            if (!con.isClosed()) {

            } else {
                //clean
            }
        }

    }

    public static void main(String[] args) {

    }

    @Override
    public void run() {

    }

    public void tracker_report_join(TrackerClientConnection pTrackerServer, boolean sync_old_done) {

    }
}
