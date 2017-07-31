package io.mybear.common.utils;

import org.junit.Test;

/**
 * Created by zkn on 2017/7/22.
 */
public class BasicTypeConversionUtilTest {

    @Test
    public void buff2long() {
        System.out.println((int) 'r');
        System.out.println((int) ((long) ('r')));
        System.out.println(BasicTypeConversionUtil.buff2long("sqw1dafdretgdfdf".toCharArray()));
    }

    @Test
    public void buff2int() {
        System.out.println((int) 'r');
        System.out.println((int) ((long) ('r')));
        System.out.println(BasicTypeConversionUtil.buff2int("192aqsqw1dafdretgdfdf".toCharArray()));
    }

}

