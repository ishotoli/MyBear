package io.mybear.common;

/**
 * Created by jamie on 2017/6/22.
 */
@FunctionalInterface
public interface FileDealDoneCallback<T> extends ThrowingConsumer<T> {
}
