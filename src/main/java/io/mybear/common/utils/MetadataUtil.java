package io.mybear.common.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by jamie on 2017/7/21.
 */
public class MetadataUtil {
    public static final String FDFS_RECORD_SEPERATOR = "\u0001";
    public static final String FDFS_FIELD_SEPERATOR = "\u0002";
    public static final Comparator<String[]> comparator = (x, y) -> x[0].length() - y[1].length();

    /**
     * split metadata to name value pair array
     *
     * @param meta_buff metadata
     * @return name value pair array
     */
    public static List<String[]> splitMetadata(StringBuilder meta_buff) {
        return splitMetadata(meta_buff, FDFS_RECORD_SEPERATOR, FDFS_FIELD_SEPERATOR);
    }

    public static List<String[]> splitMetadata(StringBuilder meta_buff, String recordSeperator, String filedSeperator) {
        StringBuilder s = meta_buff;
        List<String[]> res = new ArrayList<>();
        int index = 0;
        while (index != -1 && s.length() != 0) {
            index = s.indexOf(recordSeperator);
            String[] c = s.substring(0, index == -1 ? s.length() : index).split(filedSeperator, 2);
            if (c.length == 2) {
                res.add(c);
            }
            s.delete(0, index + 1);
        }
        return res;
    }

    public static StringBuilder packMetadata(List<String[]> meta_list, int meta_count) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < meta_count; i++) {
            s.append(meta_list.get(i)[0]);
            s.append(FDFS_FIELD_SEPERATOR);
            s.append(meta_list.get(i)[1]);
            s.append(FDFS_RECORD_SEPERATOR);
        }
        s.deleteCharAt(s.length() - 1);
        return s;
    }

    public static int sortMetadataBuff(StringBuilder s) {
        List<String[]> meta_list = splitMetadata(s);
        if (meta_list == null || meta_list.size() == 0) return -1;
        meta_list.sort(comparator);
        s.setLength(0);
        s.append(packMetadata(meta_list, meta_list.size()));
        return 0;
    }

    public static void main(String[] args) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("filename").append(FDFS_FIELD_SEPERATOR).append("hihi")
                .append(FDFS_RECORD_SEPERATOR)
                .append("color").append(FDFS_FIELD_SEPERATOR).append("red");
        System.out.println(splitMetadata(new StringBuilder(stringBuilder)));
        System.out.println(sortMetadataBuff(new StringBuilder(stringBuilder)));
    }
}
