package io.mybear.common.utils;

import io.mybear.common.constants.SizeOfConstant;
import java.util.Arrays;

/**
 * Created by zkn on 2017/7/11.
 */
public class HashUtil {

    public static final int BITS_IN_UNIGNED_INT = SizeOfConstant.SIZE_OF_INT * 8;
    public static final int THREE_QUARTERS = (BITS_IN_UNIGNED_INT * 3) / 4;
    public static final int HASH_ONE_EIGHTH = (BITS_IN_UNIGNED_INT / 8);
    //无符号int
    public static final int HASH_HIGH_BITS = (int) ((0xFFFFFFFFL) << (BITS_IN_UNIGNED_INT - HASH_ONE_EIGHTH));

    /**
     * PJW hash算法
     *
     * @param key
     * @param keyLen
     * @return
     */
    public static int PJWHash(Object key, int keyLen) {
        char[] pKey;
        char[] pEnd;
        int hash;
        int test;
        hash = 0;
        pKey = (char[]) key;
        if (keyLen < pKey.length) {
            pEnd = new char[keyLen];
            System.arraycopy(pKey, 0, pEnd, 0, keyLen);
        } else {
            pEnd = pKey;
        }
        for (int i = 0; i < pEnd.length; i++) {
            hash = (hash << HASH_ONE_EIGHTH) + pKey[i];
            if ((test = hash & HASH_HIGH_BITS) != 0) {
                hash = ((hash ^ (test >> THREE_QUARTERS)) & (~HASH_HIGH_BITS));
            }
        }
        return hash;
    }

    public static void main(String[] args){
        System.out.println(PJWHash("hello".toCharArray(),4));
    }
}
