package io.mybear.common;

import io.mybear.storage.storageNio.StorageClientInfo;

/**
 * Created by jamie on 2017/6/23.
 */
@FunctionalInterface
public interface TaskFinishCallback extends ThrowingConsumer<StorageClientInfo> {
}
