package io.mybear.tracker;

import java.io.File;

/**
 * Created by jamie on 2017/6/21.
 */
public class FdfsSharedFunc {

    /**
     * 判断文件是否存在
     * @param fileName
     * @return
     */
    public static boolean fileExists(String fileName){
        File file = new File(fileName);
        if(file.exists()){
            return true;
        }
        return false;
    }

}
