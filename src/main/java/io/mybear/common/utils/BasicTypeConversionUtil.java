package io.mybear.common.utils;

/**
 * Created by zhangkenan on 2017/7/10.
 * 基本类型转换的类，int转long int转char 大小头转换等
 *
 * @author zhangkenan
 * @date 2017/07/10
 */
public class BasicTypeConversionUtil {
    /**
     * 整型转char数组
     *
     * @param n
     * @param buff
     */
    public static void int2buff(int n, char[] buff) {
        buff[0] = (char)((n >> 24) & 0XFF);
        buff[1] = (char)((n >> 16) & 0XFF);
        buff[2] = (char)((n >> 8) & 0XFF);
        buff[3] = (char)(n & 0xFF);
    }

    /**
     * 整型转char数组，需要指定开始的位置
     *
     * @param n
     * @param buff 字节数组
     * @param i    起始位置
     */
    public static void int2buff(int n, char[] buff, int i) {
        buff[i++] = (char)((n >> 24) & 0XFF);
        buff[i++] = (char)((n >> 16) & 0XFF);
        buff[i++] = (char)((n >> 8) & 0XFF);
        buff[i++] = (char)(n & 0xFF);
    }

    /**
     * long转换为char数组
     *
     * @param n
     * @param buff
     */
    public static void long2buff(long n, char[] buff) {

        buff[0] = (char)((n >> 56) & 0xFF);
        buff[1] = (char)((n >> 48) & 0xFF);
        buff[2] = (char)((n >> 40) & 0xFF);
        buff[3] = (char)((n >> 32) & 0xFF);
        buff[4] = (char)((n >> 24) & 0xFF);
        buff[5] = (char)((n >> 16) & 0xFF);
        buff[6] = (char)((n >> 8) & 0xFF);
        buff[7] = (char)(n & 0xFF);
    }

    /**
     * long转换为char数组 需要指定起始位置
     *
     * @param n
     * @param buff
     * @param i    起始位置
     */
    public static void long2buff(long n, char[] buff, int i) {
        
        buff[i++] = (char)((n >> 56) & 0xFF);
        buff[i++] = (char)((n >> 48) & 0xFF);
        buff[i++] = (char)((n >> 40) & 0xFF);
        buff[i++] = (char)((n >> 32) & 0xFF);
        buff[i++] = (char)((n >> 24) & 0xFF);
        buff[i++] = (char)((n >> 16) & 0xFF);
        buff[i++] = (char)((n >> 8) & 0xFF);
        buff[i++] = (char)(n & 0xFF);
    }
}
