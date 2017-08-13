package io.mybear.common.tracker;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by jamie on 2017/7/19.
 */
public class TrackerServerGroup {
    public int tracker_server_index;
    public List<InetSocketAddress> tracker_servers;
    protected Integer lock;

    /**
     * fdfs_get_tracker_leader_index_ex
     *
     * @param leaderPort
     * @return
     */
    public int getTrackerLeaderIndexEx(String leaderIp, int leaderPort) {
        int size = tracker_servers.size();
        if (size == 0) return -1;
        for (int i = 0; i < size; i++) {
            InetSocketAddress it = tracker_servers.get(i);
            if (it.getHostString().equals(leaderIp) && it.getPort() == leaderPort) {
                return i;
            }
        }
        return -1;
    }
}
