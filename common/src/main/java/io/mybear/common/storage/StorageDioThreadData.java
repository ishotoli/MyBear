package io.mybear.common.storage;

import java.util.concurrent.ExecutorService;

/**
 * Created by jamie on 2017/7/11.
 */
public class StorageDioThreadData {

    /* for mixed read / write */
    public ExecutorService[] contexts;

    /* for separated read / write */
    public ExecutorService[] reader;
    public ExecutorService[] writer;

    public int count;
}
