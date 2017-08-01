package io.mybear.common;

import io.mybear.common.constants.config.FdfsGlobal;
import org.junit.Test;

/**
 * Created by zkn on 2017/7/22.
 */
public class FdfsGlobalTest {

    @Test
    public void test_fdfs_check_data_filename() {
        char[] ch = "CD/00//".toCharArray();
        System.out.println(FdfsGlobal.fdfs_check_data_filename(ch, ch.length));
    }
}
