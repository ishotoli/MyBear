package io.mybear.common.tracker;

import io.mybear.common.IniFileReader;
import io.mybear.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static io.mybear.common.constants.CommonConstant.*;
import static io.mybear.common.constants.ErrorNo.*;

/**
 * Created by jamie on 2017/8/10.
 */
public class FdfsSharedFunc {
    private static final Logger LOGGER = LoggerFactory.getLogger(FdfsSharedFunc.class);
    public static int g_storage_id_count = 0;
    static List<FDFSStorageIdInfo> g_storage_ids_by_ip = null;  //sorted by group name and storage IP
    static List<FDFSStorageIdInfo> g_storage_ids_by_id = null; //sorted by storage ID
    static List<FDFSStorageIdInfo> g_storage_ids_by_ip_port = null;  //sorted by storage ip and port

    /**
     * fdfs_get_storage_by_id
     *
     * @param tid
     * @return
     */
    public static FDFSStorageIdInfo fdfs_get_storage_by_id(String tid) {
        FDFSStorageIdInfo target = new FDFSStorageIdInfo();
        target.id = tid;
        FDFSStorageIdInfo ppFound;
        int index = Collections.binarySearch(g_storage_ids_by_id, target, Comparator.comparing((x) -> x.group_name));
        return g_storage_ids_by_id.get(index);
    }

    /**
     * fdfs_check_storage_id
     *
     * @return
     */
    public static int fdfs_check_storage_id(String group_name, String id) {
        FDFSStorageIdInfo pFound;
        pFound = fdfs_get_storage_by_id(id);
        if (pFound == null) {
            return ENOENT;
        }
        if (pFound.group_name == null) {
            return ENOENT;
        }
        return pFound.group_name.equals(group_name) ? 0 : EINVAL;
    }

    /**
     * fdfs_get_storage_id_by_ip
     * todo 性能优化 产生过多临时字符串
     *
     * @param group_name
     * @param pIpAddr
     * @return
     */
    public static FDFSStorageIdInfo fdfs_get_storage_id_by_ip(String group_name, String pIpAddr) {
        FDFSStorageIdInfo target = new FDFSStorageIdInfo();
        target.ipAddr = pIpAddr;
        target.group_name = group_name;
        int index = Collections.binarySearch(g_storage_ids_by_ip, target, Comparator.comparing((i) -> i.ipAddr + i.group_name));
        if (index == -1) {
            return null;
        }
        return g_storage_ids_by_ip.get(index);
    }

    /**
     * fdfs_parse_storage_reserved_space
     *
     * @param pIniContext
     * @param pStorageReservedSpace
     * @return
     */
    public static void fdfsParseStorageReservedSpace(IniFileReader pIniContext, FDFSStorageReservedSpace pStorageReservedSpace) throws Exception {
        int result;
        int len;
        String pReservedSpaceStr;
        long storage_reserved;
        pReservedSpaceStr = pIniContext.getStrValue("reserved_storage_space");
        if (pReservedSpaceStr == null) {
            pStorageReservedSpace.flag = TRACKER_STORAGE_RESERVED_SPACE_FLAG_MB;
            pStorageReservedSpace.mb = FDFS_DEF_STORAGE_RESERVED_MB;
            return;
        }
        if ("".equals(pReservedSpaceStr)) {
            LOGGER.error("item \"reserved_storage_space\" is empty!");
            throw new IOException();
        }

        len = pReservedSpaceStr.length();
        if (pReservedSpaceStr.charAt(len - 1) == '%') {
            int end;
            pStorageReservedSpace.flag = TRACKER_STORAGE_RESERVED_SPACE_FLAG_RATIO;
            end = 0;
            char[] chars = pReservedSpaceStr.toCharArray();
            chars[len - 1] = '\0';
            pReservedSpaceStr = new String(pReservedSpaceStr.toCharArray());
            try {
                pStorageReservedSpace.ratio = Double.parseDouble(pReservedSpaceStr);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("item \"reserved_storage_space\": %s%% is invalid!", pReservedSpaceStr);
                throw e;
            }

            if (pStorageReservedSpace.ratio <= 0.00 || pStorageReservedSpace.ratio >= 100.00) {
                LOGGER.error("item \"reserved_storage_space\": %s%% is invalid!",
                        pReservedSpaceStr);
                throw new IOException();
            }
            pStorageReservedSpace.ratio /= 100.00;
            return;
        }
        try {
            storage_reserved = Utils.parseBytes(pReservedSpaceStr, 1);
        } catch (Exception e) {
            throw e;
        }

        pStorageReservedSpace.flag = TRACKER_STORAGE_RESERVED_SPACE_FLAG_MB;
        pStorageReservedSpace.mb = storage_reserved / FDFS_ONE_MB;
        return;
    }

    /**
     * fdfs_load_storage_ids_from_file
     *
     * @param config_filename
     * @param pIniContext
     * @return
     */
    public static int fdfs_load_storage_ids_from_file(String config_filename, IniFileReader pIniContext) throws IOException {
        String pStorageIdsFilename;
        Stream<String> content;
        long file_size;
        int result = 0;

        pStorageIdsFilename = pIniContext.getStrValue("storage_ids_filename");
        if (pStorageIdsFilename == null) {
            LOGGER.error("conf file \"%s\" must have item\"storage_ids_filename\"!", config_filename);
            return ENOENT;
        }

        if ("".equals(pStorageIdsFilename)) {
            LOGGER.error("conf file \"%s\", storage_ids_filename is emtpy!", config_filename);
            return EINVAL;
        }

        if (pStorageIdsFilename.charAt(0) == '/')  //absolute path
        {
            content = Files.lines(Paths.get(pStorageIdsFilename));
        } else {
            int lastSlash = config_filename.indexOf('/');
            if (lastSlash == -1) {
                content = Files.lines(Paths.get(pStorageIdsFilename));
            } else {
                String full_filename;
                int len = lastSlash;
                if (len > MAX_PATH_SIZE) {
                    LOGGER.error("conf filename: \"%s\" is too long!");
                    return ENOSPC;
                }
                String filepath = config_filename.substring(0, len);
                full_filename = String.format("%s/%s", filepath, pStorageIdsFilename);
                content = Files.lines(Paths.get(full_filename));
            }
        }
        if (result != 0) {
            return result;
        }
        // result = fdfs_load_storage_ids(content, pStorageIdsFilename);
        return result;
    }
//
//    public static int fdfs_load_storage_ids(Stream<String> content, String pStorageIdsFilename) {
//        g_storage_ids_by_ip = new ArrayList<>();
//        Iterator<String> iterator = content.iterator();
//        while (iterator.hasNext()) {
//            String s = iterator.next().trim();
//            if ("".equals(s) || s.charAt(0) == '#') {
//                continue;
//            }
//            g_storage_id_count++;
//        }
//        if (g_storage_id_count == 0) {
//            LOGGER.error("config file: %s, no storage id!", pStorageIdsFilename);
//            return ENOENT;
//        }
//
//    char **lines;
//    char *line;
//    char *id;
//    char *group_name;
//    char *pHost;
//    char *pIpAddr;
//    char *pPort;
//        FDFSStorageIdInfo * pStorageIdInfo;
//        FDFSStorageIdInfo **ppStorageIdInfo;
//        FDFSStorageIdInfo **ppStorageIdEnd;
//        int alloc_bytes;
//        int result;
//        int line_count;
//        int i;
//
//        lines = split(content, '\n', 0, & line_count);
//        if (lines == NULL) {
//            return ENOMEM;
//        }
//
//        result = 0;
//        do {
//            g_storage_id_count = 0;
//            for (i = 0; i < line_count; i++) {
//                trim(lines[i]);
//                if (*lines[i] == '\0' || *lines[i] == '#')
//                {
//                    continue;
//                }
//                g_storage_id_count++;
//            }
//
//            if (g_storage_id_count == 0) {
//                logError("file: "__FILE__", line: %d, " \
//                        "config file: %s, no storage id!", \
//                        __LINE__, pStorageIdsFilename);
//                result = ENOENT;
//                break;
//            }
//
//            alloc_bytes = sizeof(FDFSStorageIdInfo) * g_storage_id_count;
//            g_storage_ids_by_ip = (FDFSStorageIdInfo *) malloc(alloc_bytes);
//            if (g_storage_ids_by_ip == NULL) {
//                result = errno != 0 ? errno : ENOMEM;
//                logError("file: "__FILE__", line: %d, " \
//                        "malloc %d bytes fail, " \
//                        "errno: %d, error info: %s", __LINE__, \
//                        alloc_bytes, result, STRERROR(result));
//                break;
//            }
//            memset(g_storage_ids_by_ip, 0, alloc_bytes);
//
//            alloc_bytes = sizeof(FDFSStorageIdInfo *) * g_storage_id_count;
//            g_storage_ids_by_id = (FDFSStorageIdInfo * *)malloc(alloc_bytes);
//            if (g_storage_ids_by_id == NULL) {
//                result = errno != 0 ? errno : ENOMEM;
//                logError("file: "__FILE__", line: %d, " \
//                        "malloc %d bytes fail, " \
//                        "errno: %d, error info: %s", __LINE__, \
//                        alloc_bytes, result, STRERROR(result));
//                free(g_storage_ids_by_ip);
//                break;
//            }
//            memset(g_storage_ids_by_id, 0, alloc_bytes);
//
//            pStorageIdInfo = g_storage_ids_by_ip;
//            for (i = 0; i < line_count; i++) {
//                line = lines[i];
//                if (*line == '\0' || *line == '#')
//                {
//                    continue;
//                }
//
//                id = line;
//                group_name = line;
//                while (!( * group_name == ' ' || *group_name == '\t' \
//				|| *group_name == '\0'))
//                {
//                    group_name++;
//                }
//
//                if (*group_name == '\0')
//                {
//                    logError("file: "__FILE__", line: %d, " \
//                            "config file: %s, line no: %d, " \
//                            "content: %s, invalid format, " \
//                            "expect group name and ip address!", \
//                            __LINE__, pStorageIdsFilename, \
//                            i + 1, line);
//                    result = EINVAL;
//                    break;
//                }
//
//			*group_name = '\0';
//                group_name++;  //skip space char
//                while (*group_name == ' ' || *group_name == '\t')
//                {
//                    group_name++;
//                }
//
//                pHost = group_name;
//                while (!( * pHost == ' ' || *pHost == '\t' \
//				|| *pHost == '\0'))
//                {
//                    pHost++;
//                }
//
//                if (*pHost == '\0')
//                {
//                    logError("file: "__FILE__", line: %d, " \
//                            "config file: %s, line no: %d, " \
//                            "content: %s, invalid format, " \
//                            "expect ip address!", __LINE__, \
//                            pStorageIdsFilename, i + 1, line);
//                    result = EINVAL;
//                    break;
//                }
//
//			*pHost = '\0';
//                pHost++;  //skip space char
//                while (*pHost == ' ' || *pHost == '\t')
//                {
//                    pHost++;
//                }
//
//                pIpAddr = pHost;
//                pPort = strchr(pHost, ':');
//                if (pPort != NULL) {
//                *pPort = '\0';
//                    pStorageIdInfo -> port = atoi(pPort + 1);
//                } else {
//                    pStorageIdInfo -> port = 0;
//                }
//                if (getIpaddrByName(pIpAddr, pStorageIdInfo -> ip_addr, \
//                        sizeof(pStorageIdInfo -> ip_addr)) == INADDR_NONE) {
//                    logError("file: "__FILE__", line: %d, " \
//                            "invalid host name: %s", __LINE__, pIpAddr);
//                    result = EINVAL;
//                    break;
//                }
//
//                if (!fdfs_is_server_id_valid(id)) {
//                    logError("file: "__FILE__", line: %d, " \
//                            "invalid server id: \"%s\", " \
//                            "which must be a none zero start " \
//                            "integer, such as 100001", __LINE__, id);
//                    result = EINVAL;
//                    break;
//                }
//
//                snprintf(pStorageIdInfo -> id, \
//                        sizeof(pStorageIdInfo -> id), "%s", id);
//                snprintf(pStorageIdInfo -> group_name, \
//                        sizeof(pStorageIdInfo -> group_name), \
//                        "%s", group_name);
//                pStorageIdInfo++;
//            }
//        } while (0);
//
//        freeSplit(lines);
//        if (result != 0) {
//            return result;
//        }
//
//        logDebug("file: "__FILE__", line: %d, " \
//                "g_storage_id_count: %d", __LINE__, g_storage_id_count);
//        pStorageIdInfo = g_storage_ids_by_ip;
//        for (i = 0; i < g_storage_id_count; i++) {
//            char szPortPart[ 16];
//            if (pStorageIdInfo -> port > 0) {
//                sprintf(szPortPart, ":%d", pStorageIdInfo -> port);
//            } else {
//            *szPortPart = '\0';
//            }
//            logDebug("%s  %s  %s%s", pStorageIdInfo -> id,
//                    pStorageIdInfo -> group_name,
//                    pStorageIdInfo -> ip_addr, szPortPart);
//
//            pStorageIdInfo++;
//        }
//
//        ppStorageIdEnd = g_storage_ids_by_id + g_storage_id_count;
//        pStorageIdInfo = g_storage_ids_by_ip;
//        for (ppStorageIdInfo = g_storage_ids_by_id; ppStorageIdInfo < \
//        ppStorageIdEnd;
//        ppStorageIdInfo++)
//        {
//		*ppStorageIdInfo = pStorageIdInfo++;
//        }
//
//        qsort(g_storage_ids_by_ip, g_storage_id_count, \
//                sizeof(FDFSStorageIdInfo), fdfs_cmp_group_name_and_ip);
//        qsort(g_storage_ids_by_id, g_storage_id_count, \
//                sizeof(FDFSStorageIdInfo *), fdfs_cmp_server_id);
//
//        return result;
//    }

}
