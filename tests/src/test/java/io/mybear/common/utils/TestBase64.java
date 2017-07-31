package io.mybear.common.utils;

import io.mybear.common.constants.SizeOfConstant;
import io.mybear.common.context.Base64Context;
import org.junit.Test;

/**
 * Created by wb-zhangkenan on 2017/7/13.
 *
 * @author wb-zhangkenan
 * @date 2017/07/13
 */
public class TestBase64 {

    @Test
    public void test() {

        Base64Context base64Context = new Base64Context();
        Base64.base64InitEx(base64Context, 0, '-', '_', '.');
        char[] src = "hellohellohellohello".toCharArray();
        int nSrcLen = 20;
        char[] dest = new char[SizeOfConstant.SIZE_OF_INT * 8 + 1];
        boolean bPad = false;
        Base64.base64EncodeEx(base64Context, src, nSrcLen, dest, bPad);
        System.out.println(dest);
    }
}
