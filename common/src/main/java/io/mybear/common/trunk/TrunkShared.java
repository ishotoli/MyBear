package io.mybear.common.trunk;

import io.mybear.common.Stat;
import io.mybear.common.constants.CommonConstant;
import io.mybear.common.constants.SizeOfConstant;
import io.mybear.common.context.Base64Context;
import io.mybear.common.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;

import static io.mybear.common.constants.CommonConstant.*;
import static io.mybear.common.constants.ErrorNo.EINVAL;
import static io.mybear.common.constants.ErrorNo.ENOSPC;
import static io.mybear.common.constants.config.FdfsGlobal.FDFS_FILE_EXT_NAME_MAX_LEN;
import static io.mybear.common.utils.BasicTypeConversionUtil.buff2int;
import static io.mybear.common.utils.BasicTypeConversionUtil.buff2long;
import static io.mybear.common.utils.StatUtil.S_ISLNK;
import static io.mybear.common.utils.Utils.FDFS_TRUNK_FILE_TRUE_SIZE;
import static io.mybear.common.utils.Utils.IS_TRUNK_FILE;

/**
 * Created by jamie on 2017/6/21.
 */
public class TrunkShared {

    public static final Logger log = LoggerFactory.getLogger(TrunkShared.class);
    public static final FdfsStorePaths fdfsStorePaths = new FdfsStorePaths();
    //定义Base64Context
    public static final Base64Context base64Context = new Base64Context();
    public static byte FDFS_TRUNK_STATUS_FREE = 0;
    public static byte FDFS_TRUNK_STATUS_HOLD = 1;
    public static byte FDFS_TRUNK_FILE_TYPE_NONE = '0';
    public static byte FDFS_TRUNK_FILE_TYPE_REGULAR = 'F';
    public static byte FDFS_TRUNK_FILE_TYPE_LINK = 'L';
    public static int FDFS_STAT_FUNC_STAT = 0;
    public static int FDFS_STAT_FUNC_LSTAT = 1;
    public static int FDFS_TRUNK_FILE_FILE_TYPE_OFFSET = 0;
    public static int FDFS_TRUNK_FILE_ALLOC_SIZE_OFFSET = 1;
    public static int FDFS_TRUNK_FILE_FILE_SIZE_OFFSET = 5;
    public static int FDFS_TRUNK_FILE_FILE_CRC32_OFFSET = 9;
    public static int FDFS_TRUNK_FILE_FILE_MTIME_OFFSET = 13;
    public static int FDFS_TRUNK_FILE_FILE_EXT_NAME_OFFSET = 17;
    public static int FDFS_TRUNK_FILE_HEADER_SIZE = (17 + FDFS_FILE_EXT_NAME_MAX_LEN + 1);


    public static void trunkSharedInit() {
        //初始化Base64
        Base64.base64InitEx(base64Context, 0, '-', '_', '.');
    }

    public static int trunk_file_lstat(int store_path_index, byte[] true_filename, int filename_len, Stat pStat,
                                       FdfsTrunkFullInfo pTrunkInfo, FDFSTrunkHeader pTrunkHeader) {
        return trunk_file_do_lstat_func(store_path_index, true_filename, filename_len,
                FDFS_STAT_FUNC_LSTAT, pStat, pTrunkInfo, pTrunkHeader, null);
    }

    private static int trunk_file_do_lstat_func(int store_path_index, byte[] true_filename, int filename_len, int stat_func,
                                                Stat pStat, FdfsTrunkFullInfo pTrunkInfo, FDFSTrunkHeader pTrunkHeader, TrunkContentEx pfd) {
        return trunk_file_do_lstat_func_ex(TrunkShared.getFdfsStorePaths(), store_path_index, true_filename, filename_len, stat_func, pStat, pTrunkInfo, pTrunkHeader, pfd);
    }

    /**
     * int trunk_file_do_lstat_func_ex(const FDFSStorePaths *pStorePaths, \
     * const int store_path_index, const char *true_filename, \
     * const int filename_len, const int stat_func, \
     * struct stat *pStat, FDFSTrunkFullInfo *pTrunkInfo, \
     * FDFSTrunkHeader *pTrunkHeader, int *pfd)
     *
     * @param pStorePaths
     * @param store_path_index
     * @param true_filename
     * @param filename_len
     * @param stat_func
     * @param pStat
     * @param pTrunkInfo
     * @param pTrunkHeader
     * @param pfd
     * @return
     */
    private static int trunk_file_do_lstat_func_ex(FdfsStorePaths pStorePaths, int store_path_index, byte[] true_filename,
                                                   int filename_len, int stat_func, Stat pStat, FdfsTrunkFullInfo pTrunkInfo,
                                                   FDFSTrunkHeader pTrunkHeader, TrunkContentEx pfd) {

        char[] full_filename = new char[MAX_PATH_SIZE];
        char[] buff = new char[128];
        char[] pack_buff = new char[FDFS_TRUNK_FILE_HEADER_SIZE];
        int file_size;
        int buff_len;
        int fd;
        int read_bytes;
        int result;
        int flag = 0;
        File tmpFile = null;
        pTrunkInfo.getFile().setId(0);
        //not trunk file
        String fileName = String.format("%s/data/%s", pStorePaths.getPaths()[store_path_index], new String(true_filename));
        if (filename_len != FDFS_TRUNK_FILENAME_LENGTH) {
            if (fileName.length() > MAX_PATH_SIZE) {
                flag = MAX_PATH_SIZE;
                System.arraycopy(fileName.toCharArray(), 0, full_filename, 0, MAX_PATH_SIZE);
            } else {
                flag = fileName.length();
                System.arraycopy(fileName.toCharArray(), 0, full_filename, 0, fileName.length());
            }
            tmpFile = new File(new String(full_filename, 0, flag));
            if (stat_func == FDFS_STAT_FUNC_STAT) {
                //判断文件是不是
                if (!tmpFile.isFile()) {
                    log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("这不是一个文件 !文件名%s", new String(full_filename)));
                    return -1;
                }
            } else {
                // @TODO lstat 和 stat是否需要进行区分？？？
                tmpFile = new File(new String(full_filename, 0, flag));
                if (!Files.isSymbolicLink(tmpFile.toPath())) {
                    log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("这不是一个软连接 !文件名%s", new String(full_filename)));
                    return -1;
                }
            }
        }
        Arrays.fill(buff, (char) 0);
        char[] src = new char[true_filename.length - FDFS_TRUE_FILE_PATH_LEN];
        System.arraycopy(true_filename, FDFS_TRUE_FILE_PATH_LEN, src, 0, true_filename.length - FDFS_TRUE_FILE_PATH_LEN);
        char[] buff_bak = Base64.base64_decode_auto(TrunkShared.base64Context, src, FDFS_FILENAME_BASE64_LENGTH, buff);
        buff_len = buff_bak.length;
        //文件的读写标记
        int flag_buff = 0;
        if (buff_len > 128) {
            flag_buff = 128;
            System.arraycopy(buff, 0, buff_bak, 0, buff.length);
        } else {
            flag_buff = buff_len;
            System.arraycopy(buff, 0, buff_bak, 0, buff_len);
        }
        file_size = (int) buff2long(buff, SizeOfConstant.SIZE_OF_INT * 2);
        //slave file
        if (!IS_TRUNK_FILE(file_size)) {
            if (fileName.length() > MAX_PATH_SIZE) {
                flag = MAX_PATH_SIZE;
                System.arraycopy(fileName.toCharArray(), 0, full_filename, 0, MAX_PATH_SIZE);
            } else {
                flag = fileName.length();
                System.arraycopy(fileName.toCharArray(), 0, full_filename, 0, fileName.length());
            }
            tmpFile = new File(new String(full_filename, 0, flag));
            if (stat_func == FDFS_STAT_FUNC_STAT) {
                //判断文件是不是
                if (!tmpFile.isFile()) {
                    log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("这不是一个文件 !文件名%s", new String(full_filename)));
                    return -1;
                }
            } else {
                // @TODO lstat 和 stat是否需要进行区分？？？
                tmpFile = new File(new String(full_filename, 0, flag));
                if (!Files.isSymbolicLink(tmpFile.toPath())) {
                    log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("这不是一个软连接 !文件名%s", new String(full_filename)));
                    return -1;
                }
            }
        }
        src = new char[true_filename.length - FDFS_TRUE_FILE_PATH_LEN - FDFS_FILENAME_BASE64_LENGTH];
        System.arraycopy(true_filename, FDFS_TRUE_FILE_PATH_LEN + FDFS_FILENAME_BASE64_LENGTH, src, 0, true_filename.length - FDFS_TRUE_FILE_PATH_LEN - FDFS_FILENAME_BASE64_LENGTH);
        trunk_file_info_decode(src, pTrunkInfo.getFile());
        pTrunkHeader.fileSize = FDFS_TRUNK_FILE_TRUE_SIZE(file_size);
        pTrunkHeader.mtime = buff2int(buff, SizeOfConstant.SIZE_OF_INT);
        pTrunkHeader.crc32 = buff2int(buff, SizeOfConstant.SIZE_OF_INT * 4);
        //文件扩展名
        int srcLength = true_filename.length - (filename_len - (FDFS_FILE_EXT_NAME_MAX_LEN + 1));
        int destLength = FDFS_FILE_EXT_NAME_MAX_LEN + 2;
        if (srcLength > destLength) {
            char[] tmpFileChar = new char[destLength];
            System.arraycopy(true_filename, filename_len - (FDFS_FILE_EXT_NAME_MAX_LEN + 1), tmpFileChar, 0, destLength);
            pTrunkHeader.formattedExtName = tmpFileChar;
        } else {
            char[] tmpFileChar = new char[srcLength];
            System.arraycopy(true_filename, filename_len - (FDFS_FILE_EXT_NAME_MAX_LEN + 1), tmpFileChar, 0, srcLength);
            pTrunkHeader.formattedExtName = tmpFileChar;
        }
        pTrunkHeader.allocSize = pTrunkInfo.getFile().getSize();
        pTrunkInfo.getPath().setStorePathIndex(store_path_index);
        pTrunkInfo.getPath().setSubPathHigh(Integer.parseInt(new String(true_filename, 0, 2), 16));
        pTrunkInfo.getPath().setSubPathLow(Integer.parseInt(new String(true_filename, 3, 2), 16));
        //文件全名
        full_filename = trunk_get_full_filename_ex(pStorePaths, pTrunkInfo, full_filename, full_filename.length);
        String trunkFileName = new String(full_filename);
        RandomAccessFile randomAccessFile = null;
        try {
            //只读模式打开文件
            randomAccessFile = new RandomAccessFile(trunkFileName, "r");
            if (!randomAccessFile.getFD().valid()) {
                return -1;
            }
            randomAccessFile.seek(pTrunkInfo.getFile().getOffset());
            byte[] buff_byte = new byte[128];
            if (randomAccessFile.length() < FDFS_TRUNK_FILE_HEADER_SIZE) {
                log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("读取文件长度出现异常，文件长度小于 %d !文件名%s", FDFS_TRUNK_FILE_HEADER_SIZE, trunkFileName));
                return -1;
            }
            randomAccessFile.read(buff_byte, 0, FDFS_TRUNK_FILE_HEADER_SIZE);
            pTrunkHeader.fileType = buff_byte[FDFS_TRUNK_FILE_FILE_TYPE_OFFSET];
            if (pTrunkHeader.fileType == FDFS_TRUNK_FILE_TYPE_REGULAR) {
                pStat.setSt_mode(StatUtil.S_IFREG);
            } else if (pTrunkHeader.fileType == FDFS_TRUNK_FILE_TYPE_LINK) {
                pStat.setSt_mode(StatUtil.S_IFLNK);
            } else {
                log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("Invalid file type: %d", pTrunkHeader.fileType));
                return -1;
            }
            //包装trunkHeader
            trunk_pack_header(pTrunkHeader, pack_buff);
            //
            char[] compareChar = new char[pack_buff.length];
            System.arraycopy(buff, 0, compareChar, 0, compareChar.length);
            if (!Arrays.equals(pack_buff, buff)) {
                return -1;
            }
            pStat.setSt_size(pTrunkHeader.fileSize);
            pStat.setSt_mtime(pTrunkHeader.mtime);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("只读模式创建文件出现异常!文件名%s", trunkFileName));
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            log.error(CommonConstant.LOG_FORMAT, "trunk_file_do_lstat_func_ex", "", String.format("读取文件跳过相应的字节数出现异常 文件名 %s seek %d!", "只读模式创建文件出现异常!文件名%s", trunkFileName, pTrunkInfo.getFile().getOffset()));
            return -1;
        } finally {
            if (pfd != null) {
                pfd.file = randomAccessFile;
            } else {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    private static void trunk_pack_header(FDFSTrunkHeader pTrunkHeader, char[] buff) {
        buff[FDFS_TRUNK_FILE_FILE_TYPE_OFFSET] = (char) pTrunkHeader.fileType;
        BasicTypeConversionUtil.int2buff(pTrunkHeader.allocSize, buff, FDFS_TRUNK_FILE_ALLOC_SIZE_OFFSET);
        BasicTypeConversionUtil.int2buff(pTrunkHeader.fileSize, buff, FDFS_TRUNK_FILE_FILE_SIZE_OFFSET);
        BasicTypeConversionUtil.int2buff(pTrunkHeader.crc32, buff, FDFS_TRUNK_FILE_FILE_CRC32_OFFSET);
        BasicTypeConversionUtil.int2buff(pTrunkHeader.mtime, buff, FDFS_TRUNK_FILE_FILE_MTIME_OFFSET);
        System.arraycopy(pTrunkHeader.formattedExtName, 0, buff, FDFS_TRUNK_FILE_FILE_EXT_NAME_OFFSET, FDFS_FILE_EXT_NAME_MAX_LEN + 1);
    }

    private static void trunk_pack_header(FDFSTrunkHeader pTrunkHeader, ByteBuffer buff) {
        buff.put(pTrunkHeader.fileType);
        buff.putInt(pTrunkHeader.allocSize);
        buff.putInt(pTrunkHeader.fileSize);
        buff.putInt(pTrunkHeader.crc32);
        buff.putInt(pTrunkHeader.mtime);
        //todo byte
        for (int i = 0; i < pTrunkHeader.formattedExtName.length; i++) {
            buff.put((byte) pTrunkHeader.formattedExtName[i]);
        }
    }

    public static ByteBuffer gputTrunkHeader(int offset, ByteBuffer byteBuffer, byte fileType, int alloc_size, int filesize, int crc32, int mtime, byte[] formatted_ext_name) {
        byteBuffer.put(offset + 0, fileType);//1 byte
        byteBuffer.putInt(offset + 1, alloc_size);
        byteBuffer.putInt(offset + 6, filesize);
        byteBuffer.putInt(offset + 10, crc32);
        byteBuffer.putInt(offset + 14, mtime);
        byteBuffer.put(formatted_ext_name, 0, 7);
        return byteBuffer;
    }

    public static ByteBuffer dofield(int offset, ByteBuffer byteBuffer) {
        byte fileType = byteBuffer.get(offset + 0);//1 byte
        int alloc_size = byteBuffer.getInt(offset + 1);
        int filesize = byteBuffer.getInt(offset + 6);
        int crc32 = byteBuffer.getInt(offset + 10);
        int mtime = byteBuffer.getInt(offset + 14);
        byte[] formatted_ext_name = new byte[7];
        byteBuffer.get(formatted_ext_name, 0, 7);
        return byteBuffer;
    }

    public static void trunk_unpack_header(ByteBuffer byteBuffer, int offset, FDFSTrunkHeader pTrunkHeader) {
        byte fileType = byteBuffer.get(offset + 0);//1 byte
        int alloc_size = byteBuffer.getInt(offset + 1);
        int filesize = byteBuffer.getInt(offset + 6);
        int crc32 = byteBuffer.getInt(offset + 10);
        int mtime = byteBuffer.getInt(offset + 14);
        byte[] formatted_ext_name = new byte[7];
        byteBuffer.get(formatted_ext_name, 0, 7);
        pTrunkHeader.allocSize = alloc_size;
        pTrunkHeader.fileType = fileType;
        pTrunkHeader.fileSize = filesize;
        pTrunkHeader.crc32 = crc32;
        pTrunkHeader.mtime = mtime;
        char[] f = new char[7];
        for (int i = 0; i < f.length; i++) {
            f[i] = (char) formatted_ext_name[i];
        }
        pTrunkHeader.formattedExtName = f;
    }

    public static void trunk_file_info_encode(FdfsTrunkFullInfo pTrunkFile, char[] str) {
        char[] buff = new char[SizeOfConstant.SIZE_OF_INT * 3];
        BasicTypeConversionUtil.int2buff(pTrunkFile.getFile().getId(), buff, 0);
        BasicTypeConversionUtil.int2buff(pTrunkFile.getFile().getOffset(), buff, SizeOfConstant.SIZE_OF_INT);
        BasicTypeConversionUtil.int2buff(pTrunkFile.getFile().getSize(), buff, SizeOfConstant.SIZE_OF_INT * 2);
        Base64.base64EncodeEx(TrunkShared.base64Context, buff, SizeOfConstant.SIZE_OF_INT * 3, str, false);
    }

    public static void trunk_file_info_decode(char[] str, FdfsTrunkFullInfo pTrunkFile) {
        char[] buff = new char[FDFS_TRUNK_FILE_INFO_LEN];
        Base64.base64_decode_auto(TrunkShared.base64Context, str, SizeOfConstant.SIZE_OF_INT * 3, buff);
        pTrunkFile.getFile().setId(buff2int(buff));
        pTrunkFile.getFile().setOffset(buff2int(buff, SizeOfConstant.SIZE_OF_INT));
        pTrunkFile.getFile().setSize(buff2int(buff, SizeOfConstant.SIZE_OF_INT * 2));
    }

    public static char[] trunk_get_full_filename(FdfsTrunkFullInfo pTrunkInfo, char[] full_filename, int length) {
        return trunk_get_full_filename_ex(TrunkShared.fdfsStorePaths, pTrunkInfo, full_filename, length);
    }

    /**
     * 返回full_fileName
     *
     * @param pStorePaths
     * @param pTrunkInfo
     * @param full_filename
     * @param length
     * @return
     */
    public static char[] trunk_get_full_filename_ex(FdfsStorePaths pStorePaths, FdfsTrunkFullInfo pTrunkInfo,
                                                    char[] full_filename, int length) {

        String short_filename;
        String pStorePath;

        pStorePath = pStorePaths.getPaths()[pTrunkInfo.getPath().getStorePathIndex()];
        short_filename = TRUNK_GET_FILENAME(pTrunkInfo.getFile().getId());
        String fullFileName = String.format("%s/data/" + FDFS_STORAGE_DATA_DIR_FORMAT + "/" + FDFS_STORAGE_DATA_DIR_FORMAT + "/%s",
                pStorePath, pTrunkInfo.getPath().getSubPathHigh(),
                pTrunkInfo.getPath().getSubPathLow(),
                short_filename);
        if (fullFileName.length() > length) {
            System.arraycopy(fullFileName.toCharArray(), 0, full_filename, 0, length);
            return full_filename;
        } else {
            char[] tmp = new char[fullFileName.length()];
            System.arraycopy(fullFileName.toCharArray(), 0, tmp, 0, fullFileName.length());
            return tmp;
        }
    }

    public static String TRUNK_GET_FILENAME(int file_id) {
        return String.format("%06d", file_id);
    }

    private static void trunk_file_info_decode(final char[] src, FdfsTrunkFileInfo pTrunkFile) {
        char[] buff = new char[FDFS_TRUNK_FILE_INFO_LEN];
        buff = Base64.base64_decode_auto(TrunkShared.base64Context, src, FDFS_TRUNK_FILE_INFO_LEN, buff);
        pTrunkFile.setId(buff2int(buff));
        pTrunkFile.setOffset(buff2int(buff, SizeOfConstant.SIZE_OF_INT));
        pTrunkFile.setSize(buff2int(buff, SizeOfConstant.SIZE_OF_INT * 2));
    }

    public static boolean IS_TRUNK_FILE_BY_ID(FdfsTrunkFullInfo trunkInfo) {
        return trunkInfo.getFile().getId() > 0;
    }

    /**
     * g_fdfs_store_paths
     *
     * @return
     */
    public static FdfsStorePaths getFdfsStorePaths() {
        return fdfsStorePaths;
    }

    public static String trunk_info_dump(FdfsTrunkFullInfo pTrunkInfo) {
        return pTrunkInfo.toString();
    }

    public static String trunk_header_dump(FDFSTrunkHeader pTrunkHeader) {
        return pTrunkHeader.toString();
    }

    public static boolean fdfs_is_trunk_file(String remote_filename) {
        char[] buff = new char[64];
        if (remote_filename.length() != FDFS_TRUNK_LOGIC_FILENAME_LENGTH) //not trunk file
        {
            return false;
        }
        String src = remote_filename.substring(FDFS_LOGIC_FILE_PATH_LEN);
        //todo 可能是错的
        Base64.base64_decode_auto(TrunkShared.base64Context, src.toCharArray(), FDFS_FILENAME_BASE64_LENGTH, buff);
        long file_size = buff2long(buff, SizeOfConstant.SIZE_OF_INT * 2);
        return IS_TRUNK_FILE(file_size);
    }

    public static int fdfs_decode_trunk_info(int store_path_index, String true_filename, int filename_len, FdfsTrunkFullInfo pTrunkInfo) {
        if (filename_len != FDFS_TRUNK_FILENAME_LENGTH) //not trunk file
        {
            log.warn("trunk filename length: %d != %d, filename: %s", filename_len, FDFS_TRUNK_FILENAME_LENGTH, true_filename);
            return EINVAL;
        }
        pTrunkInfo.getPath().setStorePathIndex(store_path_index);
        pTrunkInfo.getPath().setSubPathHigh(Integer.parseInt(true_filename.substring(0, 2), 16));
        pTrunkInfo.getPath().setSubPathLow(Integer.parseInt(true_filename.substring(2, 4), 16));
        trunk_file_info_decode(true_filename.substring(FDFS_TRUE_FILE_PATH_LEN + FDFS_FILENAME_BASE64_LENGTH).toCharArray(), pTrunkInfo.getFile());
        return 0;
    }

    /**
     * int trunk_file_get_content_ex(const FDFSStorePaths *pStorePaths, \
     * const FDFSTrunkFullInfo *pTrunkInfo, const int file_size, \
     * int *pfd, char *buff, const int buff_size)
     *
     * @param pStorePaths
     * @param pTrunkInfo
     * @param file_size
     * @param contentEx
     * @return
     * @throws IOException
     */
    public static int trunk_file_get_content_ex(FdfsStorePaths pStorePaths, byte[] buff, FdfsTrunkFullInfo pTrunkInfo, int file_size, TrunkContentEx contentEx) throws IOException {
        char[] full_filename = new char[MAX_PATH_SIZE];
        int result;
        int read_bytes = 0;
        RandomAccessFile file;
        if (file_size > buff.length) {
            return ENOSPC;
        }
        if (contentEx.file != null) {
            file = contentEx.file;
        } else {
            trunk_get_full_filename_ex(pStorePaths, pTrunkInfo, full_filename, full_filename.length);
            file = new RandomAccessFile(new String(full_filename), "r");
        }
        read_bytes = file.read(buff, pTrunkInfo.getFile().getOffset() + FDFS_TRUNK_FILE_HEADER_SIZE, file_size);
        if (read_bytes == file_size) {
            result = 0;
        } else {
            result = EINVAL;
        }
        if (contentEx.file != null) {
            file.close();
        }

        return result;
    }

    public static int trunk_file_stat_func_ex(FdfsStorePaths pStorePaths,
                                              int store_path_index,
                                              byte[] true_filename,
                                              int stat_func,
                                              Stat pStat, FdfsTrunkFullInfo pTrunkInfo,
                                              FDFSTrunkHeader pTrunkHeader, TrunkContentEx contentEx) throws IOException {
        int result;
        int src_store_path_index;
        int src_filename_len;
        char[] src_filename = new char[128];
        byte[] src_true_filename = new byte[128];

        result = trunk_file_do_lstat_func_ex(pStorePaths, store_path_index,
                true_filename, true_filename.length, stat_func,
                pStat, pTrunkInfo, pTrunkHeader, contentEx);


        if (result != 0) {
            return result;
        }

        if (!(stat_func == FDFS_STAT_FUNC_STAT && IS_TRUNK_FILE_BY_ID(
                (pTrunkInfo)) && S_ISLNK(pStat.getSt_mode()))) {
            return 0;
        }

        do {
            result = trunk_file_get_content_ex(pStorePaths, true_filename, pTrunkInfo,
                    (int) pStat.getSt_size(), contentEx);
            if (result != 0) {
                break;
            }

            src_filename_len = (int) pStat.getSt_size();
            FilenameResultEx resultEx = StringUtil.storage_split_filename_no_check(new String(src_filename));
            src_store_path_index = resultEx.storePathIndex;
            if (src_store_path_index < 0 || src_store_path_index >= pStorePaths.getCount()) {
                log.error("filename: %s is invalid, invalid store path index: %d, which < 0 or >= %d",
                        src_filename, src_store_path_index,
                        pStorePaths.getCount());
                result = EINVAL;
                break;
            }

            if (contentEx != null && contentEx.file != null) {
                contentEx.file.close();
                contentEx.file = null;
            }

            result = trunk_file_do_lstat_func_ex(pStorePaths,
                    src_store_path_index, src_true_filename,
                    src_filename_len, stat_func, pStat,
                    pTrunkInfo, pTrunkHeader, contentEx);
        } while (false);

        if (result != 0 && contentEx != null) {
            if (contentEx.file != null) {
                contentEx.file.close();
            }
            contentEx.file = null;
        }

        return result;
    }

    public static class TrunkContentEx {
        public RandomAccessFile file;
    }
}