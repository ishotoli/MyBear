package io.mybear.common.timer;

/**
 * Created by jamie on 2017/6/23.
 */
//public class FastTimer {
//    int slot_count;    //time wheel slot count
//    long base_time; //base time for slot 0
//    long current_time;
//    ByteBuffer slots;
//
//    int FastTimer(FastTimer timer, final int slot_count,
//    final long current_time)
//    {
//        int bytes;
//        if (slot_count <= 0 || current_time <= 0) {
//            return -1;
//        }
//
//        timer.slot_count = slot_count;
//        timer.base_time = current_time; //base time for slot 0
//        timer.current_time = current_time;
//        Unsafe unsafe;
//        unsafe.
//        bytes = sizeof(FastTimerSlot) * slot_count;
//        timer.slots = (FastTimerSlot *)malloc(bytes);
//        if (timer.slots == NULL) {
//            return errno != 0 ? errno : ENOMEM;
//        }
//        memset(timer->slots, 0, bytes);
//        return 0;
//    }
//}
