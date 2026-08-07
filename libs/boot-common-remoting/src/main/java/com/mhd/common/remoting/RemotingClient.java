//package com.mhd.common.remoting;
//
//import com.mhd.common.remoting.netty.NettyRemotingProcessor;
//import com.mhd.scada.common.entity.message.ClusterMsg;
//
///**
// * RemotingClient 是一个远程通信客户端接口。
// * 定义了注册消息处理器、异步发送消息和同步发送消息的方法。
// * 它继承自 RemotingService 接口。
// *
// * @author zhao-hao-dong
// */
//public interface RemotingClient extends RemotingService {
//    /**
//     * 注册消息处理器
//     *
//     * @param messageType 消息类型
//     * @param processor   处理器
//     */
//    void registerProcessor(ClusterMsg.MessageType messageType, NettyRemotingProcessor processor);
//
//    /**
//     * 异步发送消息。发送后不等待结果。
//     *
//     * @param request 请求消息
//     */
//    void sendMsg(ClusterMsg.Message request);
//
//    /**
//     * 同步发送消息。发送后会阻塞当前线程，直到收到响应或超时。
//     *
//     * @param request       请求消息
//     * @param timeoutMillis 超时时间（毫秒）
//     * @return 响应消息
//     */
//    ClusterMsg.Message sendMsgSync(ClusterMsg.Message request, int timeoutMillis);
//}
