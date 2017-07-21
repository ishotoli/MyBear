package io.mybear.storage.storageNio;

import java.util.concurrent.ExecutorService;

/**
 * @author wuzh
 */
public interface NameableExecutorService extends ExecutorService {

    String getName();
}