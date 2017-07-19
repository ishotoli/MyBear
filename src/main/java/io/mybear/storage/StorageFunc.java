package io.mybear.storage;

import com.alibaba.fastjson.JSON;
import io.mybear.common.FdfsGlobal;
import io.mybear.common.FdfsStorePathInfo;
import io.mybear.common.IniFileReader;
import io.mybear.common.constants.CommonConstant;
import io.mybear.storage.trunkMgr.TrunkShared;
import io.mybear.tracker.SharedFunc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jamie on 2017/6/21.
 */
public class StorageFunc {

    public static final Logger log = LoggerFactory.getLogger(StorageFunc.class);

    public static int storageLoadPaths(IniFileReader pItemContext) {
        int result;
        result = storageLoadPathsFromConfFile(pItemContext);
        if (result != 0) {
            return result;
        }
        StorageGlobal.fdfsStorePathInfo = new FdfsStorePathInfo[TrunkShared.fdfsStorePaths.getCount()];
        if (StorageGlobal.fdfsStorePathInfo.length == 0) {
            log.error(CommonConstant.LOG_FORMAT, "storageLoadPaths",
                JSON.toJSON(pItemContext) + " count " + TrunkShared.fdfsStorePaths.getCount(), "count值为0");
            return -1;
        }
        return 0;
    }

    public static int storageLoadPathsFromConfFile(IniFileReader pItemContext) {
        char[] pPath = pItemContext.getStrValue("base_path") == null ? null : pItemContext.getStrValue("base_path")
            .toCharArray();
        if (pPath == null) {
            log.error(CommonConstant.LOG_FORMAT, "storageLoadPaths",
                JSON.toJSON(pItemContext), "base_path值为null");
            return -1;
        }
        pPath = SharedFunc.chopPath(pPath);
        FdfsGlobal.g_fdfs_base_path = new String(pPath);
        if (!SharedFunc.fileExists(FdfsGlobal.g_fdfs_base_path)) {
            log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFile",
                JSON.toJSON(pItemContext), FdfsGlobal.g_fdfs_base_path + "路径不存在");
            return -1;
        }
        if (!SharedFunc.isDir(FdfsGlobal.g_fdfs_base_path)) {
            log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFile",
                JSON.toJSON(pItemContext), FdfsGlobal.g_fdfs_base_path + " is not a directory!");
            return -1;
        }
        return storageLoadPathsFromConfFileEx(pItemContext, null, true);
    }

    public static int storageLoadPathsFromConfFileEx(IniFileReader pItemContext,
                                                     final char[] szSectionName, final boolean bUseBasePath) {
        String item_name = null;
        String[] store_paths;
        String pPath;
        TrunkShared.fdfsStorePaths.setCount(pItemContext.getIntValue("store_path_count", 1));
        if (TrunkShared.fdfsStorePaths.getCount() <= 0) {
            log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFileEx",
                JSON.toJSON(pItemContext),
                String.format("store_path_count: %d is invalid", TrunkShared.fdfsStorePaths.getCount()));
            return -1;
        }
        store_paths = new String[TrunkShared.fdfsStorePaths.getCount()];
        //取store_path0的路径
        pPath = pItemContext.getStrValue("store_path0");
        if (pPath == null || "".equals(pPath.trim())) {
            if (!bUseBasePath) {
                log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFileEx",
                    JSON.toJSON(pItemContext),
                    "conf file must have item store_path0");
                return -1;
            }
            pPath = FdfsGlobal.g_fdfs_base_path;
        }
        if (pPath == null || "".equals(pPath.trim())) {
            log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFileEx",
                JSON.toJSON(pItemContext),
                "conf file must have item pPath");
            return -1;
        }
        store_paths[0] = pPath;
        int err_no = 0;
        for (int i = 1; i < TrunkShared.fdfsStorePaths.getCount(); i++) {
            item_name = String.format("store_path%d", i);
            pPath = pItemContext.getStrValue(item_name);
            if (pPath == null || "".equals(pPath.trim())) {
                log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFileEx",
                    JSON.toJSON(pItemContext),
                    String.format("conf file must have item %s", item_name));
                err_no = -1;
                break;
            }
            pPath = new String(SharedFunc.chopPath(pPath.toCharArray()));
            if (!SharedFunc.fileExists(pPath)) {
                log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFileEx",
                    JSON.toJSON(pItemContext),
                    String.format("can't be accessed %s", pPath));
                err_no = -1;
                break;
            }
            if (!SharedFunc.isDir(pPath)) {
                log.error(CommonConstant.LOG_FORMAT, "storageLoadPathsFromConfFileEx",
                    JSON.toJSON(pItemContext),
                    String.format("conf file must have item %s", pPath));
                err_no = -1;
                break;
            }
            store_paths[i] = pPath;
        }
        if (err_no == 0) {
            for (int i = 0; i < TrunkShared.fdfsStorePaths.getCount(); i++) {
                TrunkShared.fdfsStorePaths.getPaths()[i] = store_paths[i];
            }
            return 0;
        }
        return err_no;
    }
}
