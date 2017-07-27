package io.mybear.common.utils;

import java.util.Arrays;
import java.util.Random;

import com.alibaba.fastjson.JSON;

import io.mybear.common.Base64Context;
import io.mybear.common.constants.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wb-zhangkenan on 2017/7/10.
 *
 * @author wb-zhangkenan
 * @date 2017/07/10
 */
public class Base64 {

    private static final Logger log = LoggerFactory.getLogger(Base64.class);

    /**
     * Marker value for chars we just ignore, e.g. \n \r high ascii
     */
    public static final int BASE64_IGNORE = -1;
    /**
     * Marker for = trailing pad
     */
    public static final int BASE64_PAD = -2;
    public static final Random random = new Random();

    /**
     * 初始化 Base64Context Base64#base64_init_ex
     *
     * @param base64Context
     */
    public static void base64InitEx(Base64Context base64Context, int nLineLength, char chPlus, char chSplash,
                                    char chPad) {
        if (base64Context == null) {
            base64Context = new Base64Context();
        }
        base64Context.setLineLength(nLineLength);
        base64Context.getLineSeparator()[0] = '\n';
        base64Context.getLineSeparator()[1] = '\0';
        base64Context.setLineSepLen(1);
        // build translate valueToChar table only once.
        // 0..25 -> 'A'..'Z'
        for (int i = 0; i <= 25; i++) {
            base64Context.getValueToChar()[i] = (char)('A' + i);
        }
        // 26..51 -> 'a'..'z'
        for (int i = 0; i <= 25; i++) {
            base64Context.getValueToChar()[i + 26] = (char)('a' + i);
        }
        // 52..61 -> '0'..'9'
        for (int i = 0; i <= 9; i++) {
            base64Context.getValueToChar()[i + 52] = (char)('0' + i);
        }
        base64Context.getValueToChar()[62] = chPlus;
        base64Context.getValueToChar()[63] = chSplash;
        //用-1填充
        Arrays.fill(base64Context.getCharToValue(), BASE64_IGNORE);
        for (int i = 0; i < 64; i++) {
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
    public static int base64EncodeEx(Base64Context base64Context, char[] src, int nSrcLen, char[] dest,
                                     boolean bPad) {
        try {
            int linePos;
            int leftover;
            int combined;
            char[] pDest;
            int c0, c1, c2, c3;
            char[] pRaw;
            char[] pEnd;
            int destLen;
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
                return 0;
            }
            linePos = 0;
            lens[0] = (nSrcLen / 3) * 3;
            lens[1] = 3;
            leftover = nSrcLen - lens[0];
            ppSrcs[0] = src;
            ppSrcs[1] = szPad;
            System.out.println(src);
            szPad[0] = szPad[1] = szPad[2] = '\0';
            switch (leftover) {
                case 0:
                default:
                    loop = 1;
                    break;
                case 1:
                    loop = 2;
                    if (nSrcLen > src.length) {
                        szPad[0] = (char)RandomUtil.randomNextInt();
                    } else {
                        szPad[0] = src[nSrcLen - 1];
                    }
                    break;
                case 2:
                    loop = 2;
                    if (nSrcLen > src.length) {
                        szPad[0] = (char)RandomUtil.randomNextInt();
                        szPad[1] = (char)RandomUtil.randomNextInt();
                    } else {
                        szPad[0] = src[nSrcLen - 2];
                        szPad[1] = src[nSrcLen - 1];
                    }
                    break;
            }
            pDest = dest;
            for (k = 0; k < loop; k++) {
                if (lens[k] < ppSrcs[k].length) {
                    pEnd = new char[ppSrcs[k].length - lens[k]];
                    System.arraycopy(ppSrcs[k], lens[k], pEnd, 0, ppSrcs[k].length - lens[k]);
                } else {
                    pEnd = new char[0];
                }
                pRaw = ppSrcs[k];
                for (int i = 0; pRaw.length > pEnd.length && i < lens[k]; i += 3) {
                    // Start a new line if next 4 chars won't fit on the current line
                    // We can't encapsulete the following code since the variable need to
                    // be local to this incarnation of encode.
                    linePos += 4;
                    if (linePos > base64Context.getLineLength()) {
                        if (base64Context.getLineLength() != 0) {
                            System.arraycopy(base64Context.getLineSeparator(), 0, pDest, flag,
                                base64Context.getLineSepLen());
                            flag += base64Context.getLineSepLen();
                        }
                        linePos = 4;
                    }
                    // get next three bytes in unsigned form lined up,
                    // in big-endian order
                    if ((i + 2) >= pRaw.length) {
                        int temp = pRaw.length;
                        //这里扩容大一些
                        pRaw = Arrays.copyOf(pRaw, pRaw.length + 10);
                        for (int j = 0; j < 10; j++) {
                            pRaw[temp + j] = (char)(RandomUtil.randomNextInt());
                        }
                    }
                    combined = ((pRaw[i]) << 16) | ((pRaw[i + 1]) << 8) | pRaw[i + 2];
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
                    if (pDest.length <= (flag + 4)) {
                        //这里扩容大一些
                        pDest = Arrays.copyOf(pDest, flag + 10);
                    }
                    pDest[flag++] = base64Context.getValueToChar()[c0];
                    pDest[flag++] = base64Context.getValueToChar()[c1];
                    pDest[flag++] = base64Context.getValueToChar()[c2];
                    pDest[flag++] = base64Context.getValueToChar()[c3];
                }
            }
            destLen = flag;
            // deal with leftover bytes
            switch (leftover) {
                case 0:
                default:
                    // nothing to do
                    break;
                case 1:
                    // One leftover byte generates xx==
                    if (bPad) {
                        pDest[destLen - 1] = (char)base64Context.getPadCh();
                        pDest[destLen - 2] = (char)base64Context.getPadCh();
                    } else {
                        Arrays.fill(pDest, flag - 2, pDest.length, pDest[pDest.length - 1]);
                        destLen -= 2;
                    }
                    break;
                case 2:
                    // Two leftover bytes generates xxx=
                    if (bPad) {
                        pDest[destLen - 1] = (char)base64Context.getPadCh();
                    } else {
                        Arrays.fill(pDest, flag - 1, pDest.length, pDest[pDest.length - 1]);
                        destLen -= 1;
                    }
                    break;
            } // end switch;

            return destLen;
        } catch (Exception e) {
            log.error(CommonConstant.LOG_FORMAT, "base64EncodeEx", "base64Context:" +
                JSON.toJSONString(base64Context) + " src" + src + " nSrcLen " + nSrcLen + " bPad " + bPad, "e{}" + e);
            return -1;
        }
    }

    /**
     * 这里会返回一个新的 dest
     *
     * @param context
     * @param src
     * @param nSrcLen
     * @param dest
     * @return
     */
    public static char[] base64_decode_auto(Base64Context context, final char[] src,
                                            final int nSrcLen, char[] dest) {
        int nRemain;
        int nPadLen;
        int nNewLen;
        char[] tmpBuff = new char[256];
        char[] pBuff;
        nRemain = nSrcLen % 4;
        if (nRemain == 0) {
            return base64_decode(context, src, nSrcLen, dest);
        }
        nPadLen = 4 - nRemain;
        nNewLen = nSrcLen + nPadLen;
        if (nNewLen <= tmpBuff.length) {
            pBuff = tmpBuff;
        } else {
            pBuff = new char[nNewLen];
        }
        System.arraycopy(src, 0, pBuff, 0, nSrcLen);
        Arrays.fill(pBuff, nSrcLen, nSrcLen + nPadLen, (char)context.getPadCh());
        return base64_decode(context, pBuff, nNewLen, dest);
    }

    /**
     * decode a well-formed complete base64 string back into an array of bytes.
     * It must have an even multiple of 4 data characters (not counting \n),
     * padded out with = as needed.
     * 这里会返回一个新的dest
     */
    private static char[] base64_decode(Base64Context context, final char[] src,
                                        final int nSrcLen, char[] dest) {
        // tracks where we are in a cycle of 4 input chars.
        int cycle;
        // where we combine 4 groups of 6 bits and take apart as 3 groups of 8.
        int combined;

        // will be an even multiple of 4 chars, plus some embedded \n
        int dummies;
        int value;
        char[] pSrc;
        char[] pSrcEnd;
        char[] pDest;

        cycle = 0;
        combined = 0;
        dummies = 0;
        pDest = dest;
        pSrcEnd = new char[src.length - nSrcLen];
        System.arraycopy(src, nSrcLen, pSrcEnd, 0, src.length - nSrcLen);
        pSrc = src;
        int flag = 0;
        for (int i = 0; i < nSrcLen; i++) {
            value = context.getCharToValue()[pSrc[i]];
            switch (value) {
                case BASE64_IGNORE:
                    // e.g. \n, just ignore it.
                    break;
                case BASE64_PAD:
                    value = 0;
                    dummies++;
                    // fallthrough
                default:
               /* regular value character */
                    switch (cycle) {
                        case 0:
                            combined = value;
                            cycle = 1;
                            break;
                        case 1:
                            combined <<= 6;
                            combined |= value;
                            cycle = 2;
                            break;
                        case 2:
                            combined <<= 6;
                            combined |= value;
                            cycle = 3;
                            break;
                        case 3:
                            combined <<= 6;
                            combined |= value;
                            // we have just completed a cycle of 4 chars.
                            // the four 6-bit values are in combined in big-endian order
                            // peel them off 8 bits at a time working lsb to msb
                            // to get our original 3 8-bit bytes back
                            pDest[flag++] = (char)(combined >> 16);
                            pDest[flag++] = (char)((combined & 0x0000FF00) >> 8);
                            pDest[flag++] = (char)(combined & 0x000000FF);
                            cycle = 0;
                            break;
                    }
                    break;
            }
        } // end for
        if (cycle != 0) {
            char[] tempChar = new char[flag];
            System.arraycopy(pDest, 0, tempChar, 0, tempChar.length);
            log.info(CommonConstant.LOG_FORMAT, "base64_decode", "",
                String.format("Input to decode not an even multiple of 4 characters; pad with %c", context.getPadCh()));
            return tempChar;
        }
        char[] tempChar = new char[flag - dummies];
        System.arraycopy(pDest, 0, tempChar, 0, tempChar.length);
        return tempChar;
    }

    public static void main(String[] args) {
        char[] chars = "hello".toCharArray();
        int len = 0;
        char[] pEnd;
        if (len < chars.length) {
            pEnd = new char[chars.length - len];
            System.arraycopy(chars, len, pEnd, 0, chars.length - len);
        } else {
            pEnd = new char[0];
        }
        System.out.println(Arrays.toString(pEnd));
    }

}

