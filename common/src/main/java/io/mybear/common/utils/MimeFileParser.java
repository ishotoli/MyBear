package io.mybear.common.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by jamie on 2017/8/13.
 */
public class MimeFileParser {
    public static Map<String, String> loadMimeTypesFromFile(String path) throws IOException {
        Map<String, String> res = new HashMap<>();
        Iterator<String[]> iterator = Files.lines(Paths.get(path)).filter((s0) -> {
            s0 = s0.trim();
            return !("".equals(s0) || s0.charAt(0) == '#');
        }).map((s1 -> s1.split("(\\t+)|(\\s+)")))
                .iterator();
        while (iterator.hasNext()) {
            String[] strings = iterator.next();
            String contentType = strings[0];
            int size = strings.length;
            for (int i = 1; i < size; i++) {
                res.put(strings[i], contentType);
            }
        }
        return res;
    }

    public static void main(String[] args) throws IOException {
        Map<String, String> res = loadMimeTypesFromFile("mime.types");
        System.out.println(res);
    }
}
