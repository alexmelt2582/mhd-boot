//package com.mhd.common.remoting.netty;
//
//import cn.hutool.core.collection.CollUtil;
//import com.mhd.boot.common.utils.NetworkUtils;
//import com.mhd.common.remoting.RemotingService;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.epoll.Epoll;
//import io.netty.handler.timeout.IdleState;
//import io.netty.handler.timeout.IdleStateEvent;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * Netty Remoting 抽象类
// *
// * @author zhao-hao-dong
// * @see <a href="https://github.com/apache/rocketmq/blob/develop/remoting/src/main/java/org/apache/rocketmq/remoting/netty/NettyRemotingAbstract.java">NettyRemotingAbstract</a>
// */
//public abstract class NettyRemotingAbstract implements RemotingService {
//    protected final Logger log = LoggerFactory.getLogger(this.getClass());
//    /**
//     * Netty Remoting 处理器表
//     */
//    protected ConcurrentHashMap<ClusterMsg.MessageType, NettyRemotingProcessor> processorTable = new ConcurrentHashMap<>();
//    /**
//     * Netty Remoting 响应表
//     */
//    protected ConcurrentHashMap<String, ResponseFuture> responseTable = new ConcurrentHashMap<>();
//    /**
//     * Netty Remoting 钩子列表。在请求被处理前，会依次执行这些钩子，可用于日志、监控等通用逻辑。
//     */
//    protected List<NettyHook> nettyHookList = new ArrayList<>();
//    /**
//     * Netty Remoting 事件监听器
//     */
//    protected NettyEventListener nettyEventListener;
//
//    protected NettyRemotingAbstract(NettyEventListener nettyEventListener) {
//        this.nettyEventListener = nettyEventListener;
//    }
//
//    /**
//     * 注册处理器
//     *
//     * @param messageType ClusterMsg.MessageType
//     * @param processor   NettyRemotingProcessor
//     */
//    public void registerProcessor(final ClusterMsg.MessageType messageType, final NettyRemotingProcessor processor) {
//        this.processorTable.put(messageType, processor);
//    }
//
//    /**
//     * 处理接收到的消息
//     *
//     * @param ctx     ChannelHandlerContext
//     * @param message ClusterMsg.Message
//     */
//    protected void processReceiveMsg(ChannelHandlerContext ctx, ClusterMsg.Message message) {
//        if (ClusterMsg.Direction.REQUEST.equals(message.getDirection())) {
//            this.processRequestMsg(ctx, message);
//        } else {
//            this.processResponseMsg(ctx, message);
//        }
//    }
//
//    /**
//     * 处理请求消息（REQUEST）。
//     * 1. 执行所有前置钩子。
//     * 2. 根据消息类型查找处理器。
//     * 3. 调用处理器处理业务，并将返回的响应消息写回客户端。
//     *
//     * @param ctx
//     * @param request
//     */
//    protected void processRequestMsg(ChannelHandlerContext ctx, ClusterMsg.Message request) {
//        // 1. 执行所有前置钩子。
//        this.doBeforeRequest(ctx, request);
//
//        // 2. 根据消息类型查找处理器。
//        NettyRemotingProcessor processor = this.processorTable.get(request.getType());
//        if (processor == null) {
//            log.info("request type {} not supported", request.getType());
//            return;
//        }
//        // 3. 调用处理器处理业务，并将返回的响应消息写回客户端。
//        ClusterMsg.Message response = processor.handle(ctx, request);
//        if (response != null) {
//            ctx.writeAndFlush(response);
//        }
//    }
//
//    /**
//     * 执行所有注册的前置请求钩子。
//     */
//    private void doBeforeRequest(ChannelHandlerContext ctx, ClusterMsg.Message request) {
//        if (CollUtil.isEmpty(this.nettyHookList)) {
//            return;
//        }
//        for (NettyHook nettyHook : this.nettyHookList) {
//            nettyHook.doBeforeRequest(ctx, request);
//        }
//    }
//
//    /**
//     * 处理响应消息（RESPONSE）。
//     * 1. 首先尝试作为同步调用的响应处理：检查 responseTable 中是否存在对应的 Future。
//     * 2. 如果不是同步响应，则尝试作为异步消息处理：查找是否有处理器专门处理此类型的响应消息。
//     */
//    protected void processResponseMsg(ChannelHandlerContext ctx, ClusterMsg.Message response) {
//        // 1. 处理同步响应
//        if (this.responseTable.containsKey(response.getIdentity())) {
//            ResponseFuture responseFuture = this.responseTable.get(response.getIdentity());
//            responseFuture.putResponse(response);
//        } else {
//            // 2. 处理异步响应（对方主动推送的消息）
//            NettyRemotingProcessor processor = this.processorTable.get(response.getType());
//            if (processor != null) {
//                ClusterMsg.Message repMessage = processor.handle(ctx, response);
//                if (repMessage != null) {
//                    ctx.writeAndFlush(repMessage);
//                }
//            }
//        }
//    }
//
//    /**
//     * 异步发送消息。发送后不等待结果，通过 Listener 监听发送是否成功。
//     *
//     * @param channel 发送消息的通道
//     * @param request 请求消息
//     */
//    protected void sendMsgImpl(final Channel channel, final ClusterMsg.Message request) {
//        channel.writeAndFlush(request).addListener(future -> {
//            if (!future.isSuccess()) {
//                log.warn("send request message failed. address: {}, ", channel.remoteAddress(), future.cause());
//            }
//        });
//    }
//
//    /**
//     * 同步发送消息。发送后会阻塞当前线程，直到收到响应或超时。
//     *
//     * @param channel       发送消息的通道
//     * @param request       请求消息
//     * @param timeoutMillis 超时时间（毫秒）
//     * @return 响应消息，超时或失败则返回 null
//     */
//    protected ClusterMsg.Message sendMsgSyncImpl(final Channel channel, final ClusterMsg.Message request, final int timeoutMillis) {
//        final String identity = request.getIdentity();
//
//        try {
//            // 1. 创建并注册 ResponseFuture
//            ResponseFuture responseFuture = new ResponseFuture();
//            this.responseTable.put(identity, responseFuture);
//
//            // 2. 发送请求
//            channel.writeAndFlush(request).addListener(future -> {
//                if (!future.isSuccess()) {
//                    responseTable.remove(identity);
//                    log.warn("send request message failed. request: {}, address: {}, ", request, channel.remoteAddress(), future.cause());
//                }
//            });
//
//            // 3. 等待响应
//            ClusterMsg.Message response = responseFuture.waitResponse(timeoutMillis);
//            if (response == null) {
//                log.warn("get response message failed, message is null");
//            }
//            return response;
//        } catch (InterruptedException e) {
//            log.warn("get response message failed, ", e);
//        } finally {
//            // 4. 无论成功失败，最后都要清理
//            responseTable.remove(identity);
//        }
//        return null;
//    }
//
//    /**
//     * 处理 Channel 激活事件。
//     */
//    protected void channelActive(ChannelHandlerContext ctx) throws Exception {
//        if (this.nettyEventListener != null && ctx.channel().isActive()) {
//            this.nettyEventListener.onChannelActive(ctx.channel());
//        }
//    }
//
//    /**
//     * 处理 Channel 空闲事件。
//     * 当连接在指定时间内没有读写操作时触发，通常会关闭连接。
//     */
//    protected void channelIdle(ChannelHandlerContext ctx, Object evt) throws Exception {
//        IdleStateEvent event = (IdleStateEvent) evt;
//        if (this.nettyEventListener != null && event.state() == IdleState.ALL_IDLE) {
//            ctx.channel().closeFuture();
//            this.nettyEventListener.onChannelIdle(ctx.channel());
//        }
//    }
//
//    /**
//     * 判断是否使用 Netty 原生 Epoll 模式。
//     * 需同时满足：Linux 系统、且 Epoll 依赖可用。
//     */
//    protected boolean useEpoll() {
//        return NetworkUtils.isLinuxPlatform() // 1. 必须是 Linux 系统
//                && Epoll.isAvailable(); // 2. 运行时 Epoll 原生组件可用（防止缺依赖报错）
//    }
//}
