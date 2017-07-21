package io.mybear.common;


import io.mybear.storage.StorageGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Created by jamie on 2017/7/5.
 */
public class ProcessAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessAction.class);

    public static String getBasePathFromConfFile(String filename) throws Exception {
        StorageGlobal.init(filename);
        return StorageGlobal.BASE_PATH;
    }


    public static void writeToPidFile(Path pidFilename) throws Exception {
        createPidFile(pidFilename);
    }

    public static void deletePidFile(Path pidFilename) throws Exception {
        long pid = getPidFromFile(pidFilename);
        Files.delete(pidFilename);
    }

    public static void processStop(Path pidFilename) throws Exception {
        long pid = getPidFromFile(pidFilename);
        if (!doStop(pidFilename, pid)) {
            LOGGER.error("waiting for pid [%d] exit ...\n", pidFilename);
        } else {
            LOGGER.error("pid [%d] exit.\n", (int) pid);
        }
        do {
            Thread.sleep(1);
        } while (!doStop(pidFilename, pid));
        LOGGER.error("pid [%d] exit.\n", (int) pid);

    }

    public static void processRestart(Path pidFilename) throws Exception {
        processStop(pidFilename);
        LOGGER.error("starting ...\n");
    }

    public static void processExist(Path pidFilename) {

    }

    public static void processStart(Path pidFilename) {

    }

    public static boolean processAction(Path pidFilename, String action) throws Exception {
        if (action != null) {
            switch (action.trim()) {
                case "stop":
                    processStop(pidFilename);
                    return true;
                case "restart":
                    processRestart(pidFilename);
                    return true;
                case "start":
                    processStart(pidFilename);
                    return true;
                default:
                    break;

            }
            throw new Exception(String.format("invalid action: %s\n", action));
        }
        return false;
    }

    public static boolean doStop(Path pidFilename, long pid) {
        try {
            kiil(pid);
            return true;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return false;
    }

    public static void kiil(long pid) {


    }

    public static long getPidFromFile(Path pidFilename) throws IOException {
        return Long.parseLong(Files.readAllLines(pidFilename).get(0));
    }

    public static long getPID() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(processName.split("@")[0]);

    }

    public static void createPidFile(Path filePath) throws IOException {
        long pid = getPID();
        Files.write(filePath, String.valueOf(pid).getBytes(StandardCharsets.US_ASCII), StandardOpenOption.CREATE_NEW);
    }
}
