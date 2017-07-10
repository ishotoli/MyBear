package io.mybear.common.timer;

/**
 * Created by jamie on 2017/6/23.
 */
public class FastTimerEntry {
    long expires;
    Object data;
    //    struct fast_timer_entry *prev;
//    struct fast_timer_entry *next;
    boolean rehash;
}
