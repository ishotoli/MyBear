package io.mybear.common;

import java.io.Serializable;

/**
 * Created by zkn on 2017/7/10.
 */
public class FdfsStorePaths implements Serializable{

    private static final long serialVersionUID = -5126937636099720954L;
    /**
     *  //store path count
     */
    private int count;
    /**
     * //file store paths
     */
    private char[] paths = new char[count];

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public char[] getPaths() {
        return paths;
    }

    public void setPaths(char[] paths) {
        this.paths = paths;
    }

    @Override
    public String toString() {
        return "FdfsStorePaths{" +
                "count=" + count +
                ", paths='" + paths + '\'' +
                '}';
    }
}
