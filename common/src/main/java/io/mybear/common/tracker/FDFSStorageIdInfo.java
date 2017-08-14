package io.mybear.common.tracker;

import java.net.InetSocketAddress;

/**
 * Created by jamie on 2017/8/10.
 */
public class FDFSStorageIdInfo {
    public String id;
    public String group_name;  //for 8 bytes alignment
    public InetSocketAddress address;
    public String ipAddr;
    public int port;

    public FDFSStorageIdInfo() {

    }

    public FDFSStorageIdInfo(String id, String group_name, String ipAddr, int port) {
        this.id = id;
        this.group_name = group_name;
        this.address = new InetSocketAddress(ipAddr, port);
        this.ipAddr = ipAddr;
        this.port = port;
    }
}
