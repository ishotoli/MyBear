package io.mybear.common;

import io.mybear.storage.storageNio.Connection;

import java.util.function.Function;

/**
 * Created by jamie on 2017/6/22.
 */
@FunctionalInterface
public interface FileDealDoneCallback extends Function<Connection, Integer> {
}
