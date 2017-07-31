package io.mybear.common.trunk;

public class TrunkFileIdentifier {
    public FdfsTrunkPathInfo pathInfo;
    public int id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrunkFileIdentifier that = (TrunkFileIdentifier) o;

        if (id != that.id) return false;
        return pathInfo != null ? pathInfo.equals(that.pathInfo) : that.pathInfo == null;
    }

    @Override
    public int hashCode() {
        int result = pathInfo != null ? pathInfo.hashCode() : 0;
        result = 31 * result + id;
        return result;
    }
}