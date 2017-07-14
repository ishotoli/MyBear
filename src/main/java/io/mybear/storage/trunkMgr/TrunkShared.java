package io.mybear.storage.trunkMgr;

import io.mybear.common.Base64Context;
import io.mybear.common.FdfsStorePaths;
import io.mybear.common.utils.Base64;

/**
 * Created by jamie on 2017/6/21.
 */
public class TrunkShared {

    public static final FdfsStorePaths fdfsStorePaths = new FdfsStorePaths();
    //定义Base64Context
    public static final Base64Context base64Context = new Base64Context();

    public static void trunkSharedInit() {
        //初始化Base64
        Base64.base64InitEx(base64Context,0, '-', '_', '.');
    }

    public FdfsStorePaths getFdfsStorePaths() {
        return fdfsStorePaths;
    }

}
