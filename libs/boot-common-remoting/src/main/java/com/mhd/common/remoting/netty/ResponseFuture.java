//package com.mhd.common.remoting.netty;
//
//
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
///**
// * Netty 异步请求响应Future
// *
// * @author zhao-hao-dong
// */
//public class ResponseFuture {
//    private final CountDownLatch countDownLatch = new CountDownLatch(1);
//
//    private ClusterMsg.Message response;
//
//    public ClusterMsg.Message waitResponse(final long timeoutMillis) throws InterruptedException {
//        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
//        return this.response;
//    }
//
//    public void putResponse(final ClusterMsg.Message response) {
//        this.response = response;
//        this.countDownLatch.countDown();
//    }
//}
