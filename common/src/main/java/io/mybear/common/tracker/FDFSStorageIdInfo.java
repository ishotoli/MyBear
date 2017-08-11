package io.mybear.common.tracker;

import java.net.InetSocketAddress;

/**
 * Created by jamie on 2017/8/10.
 */
public class FDFSStorageIdInfo {
    public String id;
    public String group_name;  //for 8 bytes alignment
    public InetSocketAddress address;
}
