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
    private FDFSTrunkPathInfo path;
    private FDFSTrunkFileInfo file;

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    public FDFSTrunkPathInfo getPath() {
        return path;
    }

    public void setPath(FDFSTrunkPathInfo path) {
        this.path = path;
    }

    public FDFSTrunkFileInfo getFile() {
        return file;
    }

    public void setFile(FDFSTrunkFileInfo file) {
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
