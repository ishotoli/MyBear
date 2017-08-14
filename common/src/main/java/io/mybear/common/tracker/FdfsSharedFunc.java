package io.mybear.common.tracker;

import io.mybear.common.IniFileReader;
import io.mybear.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static io.mybear.common.constants.CommonConstant.*;
import static io.mybear.common.constants.ErrorNo.*;

/**
 * Created by jamie on 2017/8/10.
 */
public class FdfsSharedFunc {
    public final static List<FDFSStorageIdInfo> g_storage_ids_by_ip = new ArrayList<>();  //sorted by group name and storage IP
    public final static List<FDFSStorageIdInfo> g_storage_ids_by_id = new ArrayList<>(); //sorted by storage ID
    private static final Logger LOGGER = LoggerFactory.getLogger(FdfsSharedFunc.class);
    public static int g_storage_id_count = 0;
    public static List<FDFSStorageIdInfo> g_storage_ids_by_ip_port = null;  //sorted by storage ip and port

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
     * @param
     * @param
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
        result = fdfs_load_storage_ids(content, pStorageIdsFilename);
        return result;
    }

    /**
     * fdfs_load_storage_ids
     *
     * @param content
     * @param pStorageIdsFilename
     * @return
     */
    public static int fdfs_load_storage_ids(Stream<String> content, String pStorageIdsFilename) {
        Iterator<FDFSStorageIdInfo> iterator = content.map((i) -> i.trim())
                .filter((i) -> {
                    boolean r = (!"".equals(i) && (i.charAt(0) != '#'));
                    return r;
                })
                .map(s1 -> {
                    String[] sp = s1.split("(\\t+)|(\\s+)");
                    String ip = sp[2];
                    int ipLastIndex = ip.lastIndexOf(':');
                    if (-1 == ipLastIndex) {
                        return new FDFSStorageIdInfo(sp[0], sp[1], sp[2], 0);
                    } else {
                        int portIndex = ipLastIndex + 1;
                        return new FDFSStorageIdInfo(sp[0], sp[1], ip.substring(0, ipLastIndex), Integer.parseInt(ip.substring(portIndex, ip.length())));
                    }
                }).iterator();
        while (iterator.hasNext()) {
            FDFSStorageIdInfo info = iterator.next();
            FdfsSharedFunc.g_storage_ids_by_id.add(info);
            FdfsSharedFunc.g_storage_ids_by_ip.add(info);
        }
        FdfsSharedFunc.g_storage_ids_by_id.sort(Comparator.comparing((i) -> i.id));
        FdfsSharedFunc.g_storage_ids_by_ip.sort(Comparator.comparing((i2) -> i2.ipAddr));
        return 0;
    }

    /**
     * fdfs_init_ip_port_array
     *
     * @return
     */
    static int fdfs_init_ip_port_array() {
        int result;
        int port_count;
        g_storage_ids_by_ip_port = new ArrayList<>();
        Iterator<FDFSStorageIdInfo> iterator = g_storage_ids_by_ip.iterator();
        while (iterator.hasNext()) {
            FDFSStorageIdInfo info = iterator.next();
            if (info.port > 0) {
                g_storage_ids_by_ip_port.add(info);
            }
        }
        port_count = g_storage_ids_by_ip_port.size();
        if (port_count > 0 && port_count != g_storage_id_count) {
            LOGGER.error("config file: storage_ids.conf, some storages without port,must be the same format as host:port");
            return EINVAL;
        }
        g_storage_ids_by_ip_port.sort(Comparator.comparing((i2) -> i2.port));
        //排序后查重
        int size = g_storage_ids_by_ip_port.size();
        FDFSStorageIdInfo previous = g_storage_ids_by_ip_port.get(0);
        for (int i = 1; i < size; i++) {
            if (g_storage_ids_by_ip_port.get(i).port == previous.port) {
                String szPortPart;
                if (previous.port > 0) {
                    szPortPart = String.format(":%d", previous.port);
                } else {
                    szPortPart = "";
                }
                LOGGER.error("config file: storage_ids.conf, duplicate storage: %s%s", previous.ipAddr, szPortPart);
                return EEXIST;
            }
        }
        return 0;
    }

    /**
     * fdfs_get_storage_id_by_ip_port
     *
     * @param pIpAddr
     * @param port
     * @return
     */
    public static FDFSStorageIdInfo fdfs_get_storage_id_by_ip_port(String pIpAddr, int port) {
        if (g_storage_ids_by_ip_port == null) {
            if (fdfs_init_ip_port_array() != 0) {
                return null;
            }
        }
        FDFSStorageIdInfo fdfsStorageIdInfo = new FDFSStorageIdInfo();
        fdfsStorageIdInfo.port = port;
        fdfsStorageIdInfo.ipAddr = pIpAddr;
        int index = Collections.binarySearch(g_storage_ids_by_ip_port, fdfsStorageIdInfo, Comparator.comparing((i) -> i.ipAddr + i.port));
        if (-1 == index) {
            return null;
        } else {
            return g_storage_ids_by_ip_port.get(index);
        }
    }


    public static void main(String[] args) throws Exception {
        Stream<String> content = Files.lines(Paths.get("D:\\MyBear2\\common\\src\\main\\resources\\storage_ids.conf"));
        fdfs_load_storage_ids(content, null);
        System.out.println("");
    }


}
