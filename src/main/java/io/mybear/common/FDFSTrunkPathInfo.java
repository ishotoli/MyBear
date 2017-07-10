package io.mybear.common;

import java.io.Serializable;

/**
 * Created by zkn on 2017/7/10.
 */
public class FDFSTrunkPathInfo implements Serializable{
    private static final long serialVersionUID = 5490120279817188091L;
    /**
     *  //store which path as Mxx
     */
    private int storePathIndex;
    /**
     * //high sub dir index, front part of HH/HH
     */
    private int subPathHigh;
    /**
     * //low sub dir index, tail part of HH/HH
     */
    private int subPathLow;

    public int getStorePathIndex() {
        return storePathIndex;
    }

    public void setStorePathIndex(int storePathIndex) {
        this.storePathIndex = storePathIndex;
    }

    public int getSubPathHigh() {
        return subPathHigh;
    }

    public void setSubPathHigh(int subPathHigh) {
        this.subPathHigh = subPathHigh;
    }

    public int getSubPathLow() {
        return subPathLow;
    }

    public void setSubPathLow(int subPathLow) {
        this.subPathLow = subPathLow;
    }

    @Override
    public String toString() {
        return "FDFSTrunkPathInfo{" +
                "storePathIndex=" + storePathIndex +
                ", subPathHigh=" + subPathHigh +
                ", subPathLow=" + subPathLow +
                '}';
    }
}
