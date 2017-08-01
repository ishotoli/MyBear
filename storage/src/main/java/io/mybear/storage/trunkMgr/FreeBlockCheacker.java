package io.mybear.storage.trunkMgr;

import io.mybear.common.trunk.FdfsTrunkFullInfo;
import io.mybear.common.trunk.TrunkFileIdentifier;

import java.util.*;

/**
 * Created by jamie on 2017/7/28.
 * 用于检查空闲块,生成报告信息
 */
public class FreeBlockCheacker {
    public static final Map<TrunkFileIdentifier, List<FdfsTrunkFullInfo>> TREE_INFO_BY_ID = new HashMap<>();

    public static int freeBlockTreeNodeCount() {
        return TREE_INFO_BY_ID.size();
    }

    public static int freeBlockTotalCount() {
        Collection<List<FdfsTrunkFullInfo>> collection = TREE_INFO_BY_ID.values();
        int count = 0;
        for (List it : collection) {
            count += it.size();
        }
        return count;
    }

    public static boolean freeBlockCheckDuplicate(FdfsTrunkFullInfo pTrunkInfo) {
        TrunkFileIdentifier id = pTrunkInfo.genTrunkFileIdentifier();
        List list = TREE_INFO_BY_ID.get(id);
        if (list == null) {
            return true;
        }
        return list.contains(pTrunkInfo);
    }

    public static int freeBlockInsert(FdfsTrunkFullInfo pTrunkInfo) {
        TrunkFileIdentifier id = pTrunkInfo.genTrunkFileIdentifier();
        List list = TREE_INFO_BY_ID.get(id);
        if (list == null) {
            list = new ArrayList();
        }
        list.add(pTrunkInfo);
        TREE_INFO_BY_ID.put(id, list);
        return 0;
    }

    public static int freeBlockDelete(FdfsTrunkFullInfo pTrunkInfo) {
        TrunkFileIdentifier id = pTrunkInfo.genTrunkFileIdentifier();
        List list = TREE_INFO_BY_ID.get(id);
        list.remove(pTrunkInfo);
        if (list.size() == 0) {
            TREE_INFO_BY_ID.remove(id);
        }
        return 0;
    }

}
