package io.mybear.common;

import io.mybear.storage.storageNio.FastTaskInfo;

import java.util.function.Function;

/**
 * Created by zkn on 2017/7/10.
 */
public interface FileBeforeCloseCallback extends Function<FastTaskInfo, Integer> {
}
