package io.mybear.tracker.types;

import java.net.InetSocketAddress;

/**
 * Created by jamie on 2017/7/19.
 */
public class TrackerServerGroup {
    public int tracker_server_index;
    public InetSocketAddress[] tracker_servers;
    protected Integer lock;
}
