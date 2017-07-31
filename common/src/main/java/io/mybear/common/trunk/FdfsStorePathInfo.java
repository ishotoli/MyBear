package io.mybear.common.trunk;

import java.io.Serializable;

/**
 * Created by wb-zhangkenan on 2017/7/19.
 *
 * @author wb-zhangkenan
 * @date 2017/07/19
 */
public class FdfsStorePathInfo implements Serializable {

    private static final long serialVersionUID = -895847760190977429L;
    /**
     * //total spaces
     */
    private int totalMb;
    /**
     * //free spaces
     */
    private int freeMb;

    public int getTotalMb() {
        return totalMb;
    }

    public void setTotalMb(int totalMb) {
        this.totalMb = totalMb;
    }

    public int getFreeMb() {
        return freeMb;
    }

    public void setFreeMb(int freeMb) {
        this.freeMb = freeMb;
    }

    @Override
    public String toString() {
        return "FdfsStorePathInfo{" +
                "totalMb=" + totalMb +
                ", freeMb=" + freeMb +
                '}';
    }
}
