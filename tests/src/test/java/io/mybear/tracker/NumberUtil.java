package io.mybear.tracker;

public class NumberUtil {


    /**
     * long to bytes (big endian)
     *
     * @param num 待转换数字
     * @return bytes
     */
    public static byte[] long2byte(long num) {
        byte[] result = new byte[8];
        result[0] = (byte) ((num >> 56) & 0xFFL);
        result[1] = (byte) ((num >> 48) & 0xFFL);
        result[2] = (byte) ((num >> 40) & 0xFFL);
        result[3] = (byte) ((num >> 32) & 0xFFL);
        result[4] = (byte) ((num >> 24) & 0xFFL);
        result[5] = (byte) ((num >> 16) & 0xFFL);
        result[6] = (byte) ((num >> 8) & 0xFFL);
        result[7] = (byte) (num & 0xFFL);

        return result;
    }

    /**
     * int to bytes (big endian)
     *
     * @param num 待转换数字
     * @return bytes
     */
    public static byte[] int2byte(long num) {
        byte[] result = new byte[4];
        result[0] = (byte) ((num >> 24) & 0xFF);
        result[1] = (byte) ((num >> 16) & 0xFF);
        result[2] = (byte) ((num >> 8) & 0xFF);
        result[3] = (byte) (num & 0xFF);

        return result;
    }
}
