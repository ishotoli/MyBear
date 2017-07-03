package io.mybear.common;

import java.util.function.Function;

/**
 * Created by jamie on 2017/6/23.
 */
@FunctionalInterface
public interface TaskFinishCallback extends Function<FastTaskInfo, Integer> {
}
