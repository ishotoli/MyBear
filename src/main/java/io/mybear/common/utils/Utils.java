package io.mybear.common.utils;

public final class Utils {
    public static long parseBytes(String pStr, int default_unit_bytes) {
        if (pStr == null) return 0;
        pStr = pStr.trim();
        if ("".equals(pStr)) return 0;
        if (Character.isDigit(pStr.charAt(pStr.length() - 1))) return Long.parseLong(pStr);
        long res = Long.parseLong(pStr.substring(0, pStr.length() - 2));
        char c = pStr.charAt(pStr.length() - 2);
        switch (c) {
            case 'G':
            case 'g':
                return res * 1024 * 1024 * 1024;
            case 'M':
            case 'm':
                return res * 1024 * 1024;
            case 'K':
            case 'k':
                return res * 1024;
            default:
                return default_unit_bytes * res;
        }
    }
}
