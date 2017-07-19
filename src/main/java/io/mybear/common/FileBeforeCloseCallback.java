package io.mybear.common;


import io.mybear.storage.storageNio.StorageClientInfo;

/**
 * Created by zkn on 2017/7/10.
 */
public interface FileBeforeCloseCallback extends ThrowingConsumer<StorageClientInfo> {
}
