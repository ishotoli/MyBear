package io.mybear.storage.trunkMgr;

import io.mybear.common.FdfsStorePaths;

/**
 * Created by jamie on 2017/6/21.
 */
public class TrunkShared {

    public static final FdfsStorePaths fdfsStorePaths = new FdfsStorePaths();

    public static void trunkSharedInit() {

    }

    public FdfsStorePaths getFdfsStorePaths() {
        return fdfsStorePaths;
    }

}
