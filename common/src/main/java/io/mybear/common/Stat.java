package io.mybear.common;

import java.io.Serializable;

/**
 * Created by zkn on 2017/7/23.
 */
public class Stat implements Serializable {

    private static final long serialVersionUID = 9071093279482097846L;
    /**
     * Equivalent to drive number 0=A 1=B ...
     */
    private long st_dev;
    /**
     * Always zero
     */
    private short st_ino;
    /**
     * See above constants
     */
    private int st_mode;
    /**
     * Number of links.
     */
    private short st_nlink;
    /**
     * User: Maybe significant on NT ?
     */
    private short st_uid;
    /**
     * Group: Ditto
     */
    private short st_gid;
    /**
     * Seems useless (not even filled in)
     */
    private int st_rdev;
    /**
     * File size in bytes
     */
    private long st_size;
    /**
     * Accessed date (always 00:00 hrs local * on FAT)
     */
    private long st_atime;
    /**
     * Modified time
     */
    private long st_mtime;
    /**
     * Creation time
     */
    private long st_ctime;

    public long getSt_dev() {
        return st_dev;
    }

    public void setSt_dev(long st_dev) {
        this.st_dev = st_dev;
    }

    public short getSt_ino() {
        return st_ino;
    }

    public void setSt_ino(short st_ino) {
        this.st_ino = st_ino;
    }

    public int getSt_mode() {
        return st_mode;
    }

    public void setSt_mode(int st_mode) {
        this.st_mode = st_mode;
    }

    public short getSt_nlink() {
        return st_nlink;
    }

    public void setSt_nlink(short st_nlink) {
        this.st_nlink = st_nlink;
    }

    public short getSt_uid() {
        return st_uid;
    }

    public void setSt_uid(short st_uid) {
        this.st_uid = st_uid;
    }

    public short getSt_gid() {
        return st_gid;
    }

    public void setSt_gid(short st_gid) {
        this.st_gid = st_gid;
    }

    public int getSt_rdev() {
        return st_rdev;
    }

    public void setSt_rdev(int st_rdev) {
        this.st_rdev = st_rdev;
    }

    public long getSt_size() {
        return st_size;
    }

    public void setSt_size(long st_size) {
        this.st_size = st_size;
    }

    public long getSt_atime() {
        return st_atime;
    }

    public void setSt_atime(long st_atime) {
        this.st_atime = st_atime;
    }

    public long getSt_mtime() {
        return st_mtime;
    }

    public void setSt_mtime(long st_mtime) {
        this.st_mtime = st_mtime;
    }

    public long getSt_ctime() {
        return st_ctime;
    }

    public void setSt_ctime(long st_ctime) {
        this.st_ctime = st_ctime;
    }

    @Override
    public String toString() {
        return "Stat{" +
                "st_dev=" + st_dev +
                ", st_ino=" + st_ino +
                ", st_mode=" + st_mode +
                ", st_nlink=" + st_nlink +
                ", st_uid=" + st_uid +
                ", st_gid=" + st_gid +
                ", st_rdev=" + st_rdev +
                ", st_size=" + st_size +
                ", st_atime=" + st_atime +
                ", st_mtime=" + st_mtime +
                ", st_ctime=" + st_ctime +
                '}';
    }
}
