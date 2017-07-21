package io.mybear.common;

import java.io.Serializable;

/**
 * Created by zkn on 2017/7/10.
 */
public class FdfsTrunkFullInfo implements Serializable{
    private static final long serialVersionUID = 7265761421190943719L;
    /**
     * //normal or hold
     */
    private char status;
    private FdfsTrunkPathInfo path;
    private FdfsTrunkFileInfo file;

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    public FdfsTrunkPathInfo getPath() {
        return path;
    }

    public void setPath(FdfsTrunkPathInfo path) {
        this.path = path;
    }

    public FdfsTrunkFileInfo getFile() {
        return file;
    }

    public void setFile(FdfsTrunkFileInfo file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "FdfsTrunkFullInfo{" +
                "status=" + status +
                ", path=" + path +
                ", file=" + file +
                '}';
    }
}
