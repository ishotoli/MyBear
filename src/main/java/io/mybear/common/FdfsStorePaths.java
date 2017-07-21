package io.mybear.common;

import java.io.Serializable;

/**
 * Created by zkn on 2017/7/10.
 */
public class FdfsStorePaths implements Serializable {

    private static final long serialVersionUID = -5126937636099720954L;
    /**
     * //store path count
     */
    private int count;
    /**
     * //file store paths
     */
    private String[] paths;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
        paths = new String[count];
    }

    public String[] getPaths() {
        return paths;
    }

    @Override
    public String toString() {
        return "FdfsStorePaths{" +
            "count=" + count +
            ", paths='" + paths + '\'' +
            '}';
    }
}
