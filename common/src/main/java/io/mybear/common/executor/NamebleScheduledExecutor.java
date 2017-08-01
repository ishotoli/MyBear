package io.mybear.common.executor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class NamebleScheduledExecutor extends ScheduledThreadPoolExecutor
        implements NameableExecutorService {
    private final String name;

    public NamebleScheduledExecutor(String name, int corePoolSize,
                                    ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
