package com.mhd.boot.common.config;

import com.mhd.boot.common.utils.SpringUtils;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.util.concurrent.*;

/**
 * @author zhao-hao-dong
 */
@Slf4j
@AutoConfiguration
public class ThreadPoolConfig {
    /**
     * 核心线程数 = cpu 核心数 + 1
     */
    private final int core = Runtime.getRuntime().availableProcessors() + 1;

    private ScheduledExecutorService scheduledExecutorService;

    /**
     * 执行周期性或定时任务
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        // daemon 必须为 true
        BasicThreadFactory.Builder builder = new BasicThreadFactory.Builder().daemon(true);
        if (SpringUtils.isVirtual()) {
            builder.namingPattern("virtual-schedule-pool-%d").wrappedFactory(new VirtualThreadTaskExecutor().getVirtualThreadFactory());
        } else {
            builder.namingPattern("schedule-pool-%d");
        }
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(core,
                builder.build(),
                new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                printException(r, t);
            }
        };
        this.scheduledExecutorService = scheduledThreadPoolExecutor;
        return scheduledThreadPoolExecutor;
    }

    /**
     * 销毁事件
     * 停止线程池
     * 先使用shutdown, 停止接收新任务并尝试完成所有已存在任务.
     * 如果超时, 则调用shutdownNow, 取消在workQueue中Pending的任务,并中断所有阻塞函数.
     * 如果仍然超時，則強制退出.
     * 另对在shutdown时线程本身被调用中断做了处理.
     */
    @PreDestroy
    public void destroy() {
        try {
            ScheduledExecutorService pool = scheduledExecutorService;
            if (pool != null && !pool.isShutdown()) {
                log.info("shutdown scheduledExecutorService start...");
                pool.shutdown();
                try {
                    if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                        pool.shutdownNow();
                        if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                            log.info("shutdown scheduledExecutorService fail. pool did not terminate");
                        }
                    }
                } catch (InterruptedException ie) {
                    pool.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                log.info("shutdown scheduledExecutorService success");
            }
        } catch (Exception e) {
            log.error("shutdown scheduledExecutorService error: {}", e.getMessage(), e);
        }
    }

    /**
     * 打印线程异常信息
     */
    public static void printException(Runnable r, Throwable t) {
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
                if (future.isDone()) {
                    future.get();
                }
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            log.error(t.getMessage(), t);
        }
    }
}
