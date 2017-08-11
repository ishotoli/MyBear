package io.mybear.storage.trunkMgr;

import io.mybear.common.trunk.TrunkShared;
import org.junit.Test;

/**
 * Created by zkn on 2017/7/23.
 */
public class TrunkSharedTest {

    @Test
    public void testTRUNK_GET_FILENAME() {

        System.out.println(TrunkShared.TRUNK_GET_FILENAME(12));
        System.out.println(String.format("%02X", 12));
        String fullFileName = String.format("%s/data/%02X/%02X/%s",
                "QA", 12,
                56,
                TrunkShared.TRUNK_GET_FILENAME(12));
        System.out.println(fullFileName);
    }

}
