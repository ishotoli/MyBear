package io.mybear.common.utils;

import io.mybear.common.Base64Context;

/**
 * Created by wb-zhangkenan on 2017/7/10.
 *
 * @author wb-zhangkenan
 * @date 2017/07/10
 */
public class Base64 {

    public static final int BASE64_IGNORE = -1;
    public static final int BASE64_PAD = -2;

    /**
     * 初始化 Base64Context Base64#base64_init_ex
     *
     * @param base64Context
     */
    public static void base64InitEx(Base64Context base64Context, int nLineLength, char chPlus, char chSplash, char chPad) {
        if (base64Context == null) {
            base64Context = new Base64Context();
        }
        int i;
        base64Context.setLineLength(nLineLength);
        base64Context.getLineSeparator()[0] = '\n';
        base64Context.getLineSeparator()[1] = '\0';
        base64Context.setLineSepLen(1);

        // build translate valueToChar table only once.
        // 0..25 -> 'A'..'Z'
        for (i = 0; i <= 25; i++) {
            base64Context.getValueToChar()[i] = (char) ('A' + i);
        }
        // 26..51 -> 'a'..'z'
        for (i = 0; i <= 25; i++) {
            base64Context.getValueToChar()[i + 26] = (char) ('a' + i);
        }
        // 52..61 -> '0'..'9'
        for (i = 0; i <= 9; i++) {
            base64Context.getValueToChar()[i + 52] = (char) ('0' + i);
        }
        base64Context.getValueToChar()[62] = chPlus;
        base64Context.getValueToChar()[63] = chSplash;
        //memset(context -> charToValue, BASE64_IGNORE, sizeof(context -> charToValue));
        for (i = 0; i < 64; i++) {
            base64Context.getCharToValue()[base64Context.getValueToChar()[i]] = i;
        }
        base64Context.setPadCh(chPad);
        base64Context.getCharToValue()[chPad] = BASE64_PAD;
    }

    /**
     * 类型转换
     *
     * @return
     */
    public static char[] base64EncodeEx(Base64Context base64Context, char[] src, int nSrcLen, char[] dest, int destLen,
                                        boolean bPad) {
        int linePos;
        int leftover;
        int combined;
        char[] pDest;
        int c0, c1, c2, c3;
        char[] pRaw;
        char[] pEnd;
        //二维数组
        final char[][] ppSrcs = new char[2][];
        int[] lens = new int[2];
        char[] szPad = new char[3];
        int k;
        int loop;
        int flag = 0;
        if (nSrcLen <= 0) {
            dest = new char[0];
            destLen = 0;
            return dest;
        }
        linePos = 0;
        lens[0] = (nSrcLen / 3) * 3;
        lens[1] = 3;
        leftover = nSrcLen - lens[0];
        ppSrcs[0] = src;
        ppSrcs[1] = szPad;

        szPad[0] = szPad[1] = szPad[2] = '\0';
        switch (leftover) {
            case 0:
            default:
                loop = 1;
                break;
            case 1:
                loop = 2;
                szPad[0] = src[nSrcLen - 1];
                break;
            case 2:
                loop = 2;
                szPad[0] = src[nSrcLen - 2];
                szPad[1] = src[nSrcLen - 1];
                break;
        }
        pDest = dest;
        for (k = 0; k < loop; k++) {
            pEnd = ppSrcs[k] + lens[k];
            for (pRaw = ppSrcs[k]; pRaw < pEnd; pRaw += 3) {
                // Start a new line if next 4 chars won't fit on the current line
                // We can't encapsulete the following code since the variable need to
                // be local to this incarnation of encode.
                linePos += 4;
                if (linePos > base64Context.getLineLength()) {
                    if (base64Context.getLineLength() != 0) {
                        System.arraycopy(base64Context.getLineSeparator(), 0, pDest, flag, base64Context.getLineSepLen());
                        //pDest += base64Context.getLineSepLen();
                        flag += base64Context.getLineSepLen();
                    }
                    linePos = 4;
                }
                // get next three bytes in unsigned form lined up,
                // in big-endian order
                combined = ((pRaw[0]) << 16) | ((pRaw[1]) << 8) | pRaw[2];
                // break those 24 bits into a 4 groups of 6 bits,
                // working LSB to MSB.
                c3 = combined & 0x3f;
                combined >>= 6;
                c2 = combined & 0x3f;
                combined >>= 6;
                c1 = combined & 0x3f;
                combined >>= 6;
                c0 = combined & 0x3f;
                // Translate into the equivalent alpha character
                // emitting them in big-endian order.
                pDest[flag++] = (char) base64Context.getCharToValue()[c0];
                pDest[flag++] = (char) base64Context.getCharToValue()[c1];
                pDest[flag++] = (char) base64Context.getCharToValue()[c2];
                pDest[flag++] = (char) base64Context.getCharToValue()[c3];
            }
        }

  *pDest = '\0';
        destLen = pDest - dest;
        // deal with leftover bytes
        switch (leftover) {
            case 0:
            default:
                // nothing to do
                break;
            case 1:
                // One leftover byte generates xx==
                if (bPad) {
                    pDest[pDest.length - 1] = (char) base64Context.getPadCh();
                    pDest[pDest.length - 2] = (char) base64Context.getPadCh();
                } else {
                    pDest[pDest.length - 2] = '\0';
                    destLen -= 2;
                }
                break;
            case 2:
                // Two leftover bytes generates xxx=
                if (bPad) {
                    pDest[pDest.length - 1] = (char) base64Context.getPadCh();
                } else {
                    pDest[pDest.length - 1] = '\0';
                    destLen -= 1;
                }
                break;
        } // end switch;
        return dest;
    }

}
