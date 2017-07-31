package io.mybear.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 弱精度的计时器，考虑性能不使用同步策略。
 **/
public class TimeUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeUtil.class);
    private static long CURRENT_TIME = System.currentTimeMillis();

    public static final long currentTimeMillis() {
        return CURRENT_TIME;
    }

    public static final void update() {
        CURRENT_TIME = System.currentTimeMillis();
        //   LOGGER.debug("{}", CURRENT_TIME);
    }

}