package io.mybear.common;

/**
 * Created by zkn on 2017/7/10.
 */
public class FDFSTrunkFileInfo {
    /**
     * //trunk file id
     */
    private int id;
    /**
     * //file offset
     */
    private int offset;
    /**
     * //space size
     */
    private int size;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "FDFSTrunkFileInfo{" +
                "id=" + id +
                ", offset=" + offset +
                ", size=" + size +
                '}';
    }
}
