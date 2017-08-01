package io.mybear.common.trunk;


import java.io.Serializable;

import static io.mybear.common.trunk.TrunkShared.FDFS_TRUNK_STATUS_FREE;


/**
 * Created by zkn on 2017/7/10.
 */
public class FdfsTrunkFullInfo implements Serializable {
    private static final long serialVersionUID = 7265761421190943719L;
    /**
     * //normal or hold
     */
    private byte status;
    private FdfsTrunkPathInfo path;
    private FdfsTrunkFileInfo file;

    public TrunkFileIdentifier genTrunkFileIdentifier() {
        TrunkFileIdentifier id = new TrunkFileIdentifier();
        id.id = file.getId();
        id.pathInfo = path;
        return id;
    }

    public FdfsTrunkFullInfo split(int size) {
        FdfsTrunkFullInfo trunkFullInfo = new FdfsTrunkFullInfo();
        trunkFullInfo.status = FDFS_TRUNK_STATUS_FREE;
        trunkFullInfo.setPath(path);
        FdfsTrunkFileInfo fdfsTrunkFileInfo = new FdfsTrunkFileInfo();
        fdfsTrunkFileInfo.setOffset(this.file.getOffset() + size);
        fdfsTrunkFileInfo.setSize(this.getFile().getSize() - size);
        this.getFile().setSize(size);
        trunkFullInfo.setFile(fdfsTrunkFileInfo);
        return trunkFullInfo;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
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
