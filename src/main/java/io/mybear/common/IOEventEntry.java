package io.mybear.common;

import io.mybear.common.timer.FastTimerEntry;

/**
 * Created by jamie on 2017/6/23.
 */
public class IOEventEntry {
    int fd;
    FastTimerEntry timer;
    //IOEventCallback callback;//typedef void (*IOEventCallback) (int sock, short event, void *arg);
}
