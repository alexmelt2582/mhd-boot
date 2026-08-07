//package com.mhd.alert.calculate.periodic;
//
//import com.mhd.alert.entity.AlertDefine;
//
//import java.util.concurrent.Future;
//import java.util.concurrent.ScheduledFuture;
//
///**
// * @author zhao-hao-dong
// */
//public class ScheduledTaskState {
//    private final AlertDefine rule;
//    private ScheduledFuture<?> scheduledFuture;
//    private Future<?> runningFuture;
//    private boolean running;
//    private boolean pending;
//    private boolean cancelled;
//
//    private ScheduledTaskState(AlertDefine rule) {
//        this.rule = rule;
//    }
//
//    private synchronized void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
//        this.scheduledFuture = scheduledFuture;
//    }
//
//    private synchronized void trigger() {
//        if (cancelled) {
//            return;
//        }
//        if (running) {
//            pending = true;
//            return;
//        }
//        running = true;
//        submitLocked();
//    }
//
//    private synchronized void cancel() {
//        cancelled = true;
//        pending = false;
//        ScheduledFuture<?> periodicFuture = scheduledFuture;
//        Future<?> currentFuture = runningFuture;
//        if (periodicFuture != null) {
//            periodicFuture.cancel(true);
//        }
//        if (currentFuture != null) {
//            currentFuture.cancel(true);
//        }
//    }
//
//    private void submitLocked() {
//        try {
//            runningFuture = periodicExecutor.submit(() -> {
//                boolean permitAcquired = false;
//                try {
//                    periodicPermits.acquire();
//                    permitAcquired = true;
//                    if (!Thread.currentThread().isInterrupted()) {
//                        executeRule(rule);
//                    }
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                } catch (Exception e) {
//                    log.error("Periodic alert rule {} execution error: {}", rule.getName(), e.getMessage(), e);
//                } finally {
//                    if (permitAcquired) {
//                        periodicPermits.release();
//                    }
//                    onComplete();
//                }
//            });
//        } catch (RuntimeException e) {
//            running = false;
//            throw e;
//        }
//    }
//
//    private synchronized void onComplete() {
//        runningFuture = null;
//        if (cancelled) {
//            running = false;
//            pending = false;
//            return;
//        }
//        if (!pending) {
//            running = false;
//            return;
//        }
//        pending = false;
//        submitLocked();
//    }
//}
