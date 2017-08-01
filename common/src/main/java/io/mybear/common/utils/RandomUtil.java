package io.mybear.common.utils;

import java.util.Random;

/**
 * Created by wb-zhangkenan on 2017/7/14.
 * Random的随机类
 *
 * @author wb-zhangkenan
 * @date 2017/07/14
 */
public class RandomUtil {
    /**
     * 取固定值的随机数
     */
    private static final Random fixedRandom = new Random(32767);

    private static final Random random = new Random();

    /**
     * 返回一个固定的随机值
     *
     * @return
     */
    public static int randomFixedInt() {

        return fixedRandom.nextInt() >>> 26;
    }

    public static int randomNextInt() {
        return random.nextInt();
    }

    public static void main(String[] args) {

        System.out.println(randomFixedInt());
    }
}
