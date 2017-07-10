package io.mybear.common;

import java.util.Arrays;

/**
 * Created by wb-zhangkenan on 2017/7/10.
 *
 * @author wb-zhangkenan
 * @date 2017/07/10
 */
public class Base64Context {

    private char[] lineSeparator = new char[16];
    private int lineSepLen;
    /**
     * max chars per line, excluding line_separator.  A multiple of 4.
     */
    private int line_length;
    /**
     * letter of the alphabet used to encode binary values 0..63
     */
    private char[] valueToChar = new char[64];

    /**
     * binary value encoded by a given letter of the alphabet 0..63
     */
    private int[] charToValue = new int[256];
    private int padCh;

    public char[] getLineSeparator() {
        return lineSeparator;
    }

    public int getLineSepLen() {
        return lineSepLen;
    }

    public int getLine_length() {
        return line_length;
    }

    public char[] getValueToChar() {
        return valueToChar;
    }

    public int[] getCharToValue() {
        return charToValue;
    }

    public int getPadCh() {
        return padCh;
    }

    public void setLineSepLen(int lineSepLen) {
        this.lineSepLen = lineSepLen;
    }

    public void setLine_length(int line_length) {
        this.line_length = line_length;
    }

    public void setPadCh(int padCh) {
        this.padCh = padCh;
    }

    @Override
    public String toString() {
        return "Base64Context{" +
            "lineSeparator=" + Arrays.toString(lineSeparator) +
            ", lineSepLen=" + lineSepLen +
            ", line_length=" + line_length +
            ", valueToChar=" + Arrays.toString(valueToChar) +
            ", charToValue=" + Arrays.toString(charToValue) +
            ", padCh=" + padCh +
            '}';
    }
}
