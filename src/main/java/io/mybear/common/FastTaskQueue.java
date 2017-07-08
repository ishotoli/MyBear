package io.mybear.common;

/**
 * Created by jamie on 2017/6/23.
 */
public class FastTaskQueue {
    //    struct fast_task_info *head;
//    struct fast_task_info *tail;
//    pthread_mutex_t lock;
    int max_connections;
    int alloc_connections;
    int alloc_task_once;
    int min_buff_size;
    int max_buff_size;
    int arg_size;
    int block_size;
//    bool malloc_whole_block;
}
