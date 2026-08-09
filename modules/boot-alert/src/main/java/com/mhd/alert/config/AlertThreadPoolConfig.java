package com.mhd.alert.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * 告警线程池配置
 *
 * @author zhao-hao-dong
 */
@Component
@Slf4j
@Getter
public class AlertThreadPoolConfig implements DisposableBean {
    /**
     * 线程中的任务在接收到应用关闭信号量后最多等待多久就强制终止，其实就是给剩余任务预留的时间， 到时间后线程池必须销毁
     */
    private static final long AWAIT_TERMINATION = 30;
    /**
     * awaitTermination的单位
     */
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    /**
     * 告警工作线程池
     */
    private final ThreadPoolExecutor workerExecutor;
    /**
     * 消息通知线程池
     */
    private final ThreadPoolExecutor notifyExecutor;
    /**
     * 日志工作线程池
     */
    private final ThreadPoolExecutor logWorkerExecutor;

    private final ThreadPoolExecutor alertReduceWorkerExecutor;

    public AlertThreadPoolConfig() {
        this.workerExecutor = buildWorkerExecutor();
        this.notifyExecutor = buildNotifyExecutor();
        this.logWorkerExecutor = buildLogWorkExecutor();
        this.alertReduceWorkerExecutor = buildAlertReduceWorkerExecutor();
        printExecutorStatus(workerExecutor, "Work");
        printExecutorStatus(notifyExecutor, "Notify");
        printExecutorStatus(logWorkerExecutor, "LogWork");
        printExecutorStatus(alertReduceWorkerExecutor, "AlertReduceWorker");
    }

    public void executeJob(Runnable runnable) throws RejectedExecutionException {
        workerExecutor.execute(runnable);
    }

    public void executeNotify(Runnable runnable) throws RejectedExecutionException {
        notifyExecutor.execute(runnable);
    }

    public void executeLogJob(Runnable runnable) throws RejectedExecutionException {
        logWorkerExecutor.execute(runnable);
    }

    /**
     * 构建线程池
     *
     * @return ThreadPoolExecutor
     */
    private ThreadPoolExecutor buildWorkerExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler((thread, throwable) -> {
                    log.error("[Alert] Work thread pool has uncaughtException. Thread: {}, Error: {}", thread.getName(), throwable.getMessage(), throwable);
                })
                .setDaemon(true)
                .setNameFormat("alert-worker-%d")
                .build();
        return new ThreadPoolExecutor(10,
                10, 10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 构建线程池
     *
     * @return ThreadPoolExecutor
     */
    private ThreadPoolExecutor buildNotifyExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler((thread, throwable) -> {
                    log.error("[Alert] Notify thread pool has uncaughtException. Thread: {}, Error: {}", thread.getName(), throwable.getMessage(), throwable);
                })
                .setDaemon(true)
                .setNameFormat("alert-notify-%d")
                .build();
        return new ThreadPoolExecutor(6,
                6, 10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 构建线程池
     *
     * @return ThreadPoolExecutor
     */
    private ThreadPoolExecutor buildLogWorkExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler((thread, throwable) -> {
                    log.error("[Alert] Log work thread pool has uncaughtException. Thread: {}, Error: {}", thread.getName(), throwable.getMessage(), throwable);
                })
                .setDaemon(true)
                .setNameFormat("alert-log-worker-%d")
                .build();
        return new ThreadPoolExecutor(10,
                10, 10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 构建线程池
     *
     * @return ThreadPoolExecutor
     */
    private ThreadPoolExecutor buildAlertReduceWorkerExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler((thread, throwable) -> {
                    log.error("[Alert] AlertReduceWorker thread pool has uncaughtException. Thread: {}, Error: {}", thread.getName(), throwable.getMessage(), throwable);
                })
                .setDaemon(true)
                .setNameFormat("alert-reduce-worker-%d")
                .build();
        return new ThreadPoolExecutor(2,
                2, 10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }

    @Override
    public void destroy() throws Exception {
        closeExecutor(workerExecutor, "Worker");
        closeExecutor(notifyExecutor, "Notify");
        closeExecutor(logWorkerExecutor, "LogWorker");
        closeExecutor(alertReduceWorkerExecutor, "AlertReduceWorker");
    }

    /**
     * 打印线程池状态
     */
    private void printExecutorStatus(ThreadPoolExecutor executor, String name) {
        log.info("[Alert] {} thread pool status: corePoolSize={}, maximumPoolSize={}, keepAliveTime={}s, queueCapacity={}, rejectedExecutionHandler={}",
                name,
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getKeepAliveTime(TimeUnit.SECONDS),
                executor.getQueue().remainingCapacity(),
                executor.getRejectedExecutionHandler().getClass().getSimpleName());
    }

    private void closeExecutor(ThreadPoolExecutor executor, String name) {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(AWAIT_TERMINATION, TIME_UNIT)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(AWAIT_TERMINATION, TIME_UNIT)) {
                        log.info("[Alert] {} thread pool did not terminate", name);
                    }
                }
            } catch (InterruptedException ie) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("[Alert] {} thread pool has been destroyed.", name);
        }
    }
}
