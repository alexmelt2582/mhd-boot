//package com.mhd.common.remoting.client;
//
//import com.google.common.util.concurrent.ThreadFactoryBuilder;
//import com.mhd.common.remoting.RemotingClient;
//import com.mhd.common.remoting.netty.NettyEventListener;
//import com.mhd.common.remoting.netty.NettyRemotingAbstract;
//import com.mhd.scada.common.entity.message.ClusterMsg;
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.*;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.codec.compression.ZlibCodecFactory;
//import io.netty.handler.codec.compression.ZlibWrapper;
//import io.netty.handler.codec.protobuf.ProtobufDecoder;
//import io.netty.handler.codec.protobuf.ProtobufEncoder;
//import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
//import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
//
//import java.nio.channels.Channel;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.ThreadFactory;
//import java.util.concurrent.TimeUnit;
//
///**
// * Netty Remoting Client 实现类
// *
// * @author zhao-hao-dong
// * @see <a href="https://github.com/apache/rocketmq/blob/develop/remoting/src/main/java/org/apache/rocketmq/remoting/netty/NettyRemotingClient.java">NettyRemotingClient</a>
// */
//public class NettyRemotingClient extends NettyRemotingAbstract implements RemotingClient {
//    /**
//     * 默认 Client Worker 线程数量
//     */
//    private static final int DEFAULT_WORKER_THREAD_NUM = Math.min(4, Runtime.getRuntime().availableProcessors());
//    /**
//     * Client 配置
//     */
//    private final NettyClientConfig nettyClientConfig;
//    /**
//     * 线程池，用于执行长耗时任务
//     */
//    private final ExecutorService threadPool;
//    /**
//     * Netty Bootstrap 对象，用于配置和启动客户端
//     */
//    private final Bootstrap bootstrap = new Bootstrap();
//    /**
//     * Netty Worker 线程组，用于处理网络事件
//     */
//    private EventLoopGroup workerGroup;
//    /**
//     * Netty Channel 对象，表示与服务器的连接
//     */
//    private Channel channel;
//
//    public NettyRemotingClient(final NettyClientConfig nettyClientConfig,
//                               final NettyEventListener nettyEventListener,
//                               final ExecutorService threadPool) {
//        super(nettyEventListener);
//        this.nettyClientConfig = nettyClientConfig;
//        this.threadPool = threadPool;
//    }
//
//    @Override
//    public void sendMsg(final ClusterMsg.Message request) {
//        this.sendMsgImpl(this.channel, request);
//    }
//
//    @Override
//    public ClusterMsg.Message sendMsgSync(ClusterMsg.Message request, int timeoutMillis) {
//        return this.sendMsgSyncImpl(this.channel, request, timeoutMillis);
//    }
//
//    @Override
//    public void start() {
//        // 提交一个长耗时任务到线程池，在后台异步启动 Netty 客户端并维持连接
//        this.threadPool.execute(() -> {
//            // 1. 创建 Netty Client 的自定义线程工厂
//            ThreadFactory threadFactory = new ThreadFactoryBuilder()
//                    .setUncaughtExceptionHandler((thread, throwable) -> {
//                        log.error("Netty client worker has uncaughtException.");
//                        log.error(throwable.getMessage(), throwable);
//                    })
//                    .setDaemon(true)
//                    .setNameFormat("netty-client-worker-%d")
//                    .build();
//
//            // 2. 从系统属性中获取Client Worker 线程数，如果没有设置则使用默认值
//            String envThreadNum = System.getProperty("scada.client.worker.thread.num");
//            int workerThreadNum = envThreadNum != null ? Integer.parseInt(envThreadNum) : DEFAULT_WORKER_THREAD_NUM;
//
//            // 3. 配置 Netty 客户端
//            this.workerGroup = new NioEventLoopGroup(workerThreadNum, threadFactory);
//            this.bootstrap.group(workerGroup)
//                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.nettyClientConfig.getConnectTimeoutMillis())
//                    .channel(NioSocketChannel.class)
//                    .handler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        protected void initChannel(SocketChannel channel) throws Exception {
//                            NettyRemotingClient.this.initChannel(channel);
//                        }
//                    });
//
//            // 4. 断线重连核心循环
//            // 循环条件：当前线程未被中断，且（是第一次连接 OR 通道为空 OR 通道已断开/不活跃）
//            this.channel = null;
//            boolean first = true;
//            while (!Thread.currentThread().isInterrupted()
//                    && (first || this.channel == null || !this.channel.isActive())) {
//                first = false;
//                try {
//                    // 发起与服务器的同步连接，并获取连接成功的 Channel
//                    this.channel = this.bootstrap
//                            .connect(this.nettyClientConfig.getServerHost(), this.nettyClientConfig.getServerPort())
//                            .sync().channel();
//
//                    // 同步等待 Channel 关闭。
//                    // 只要连接保持正常，代码会一直阻塞在这里；
//                    // 一旦连接断开（如网络波动、服务端重启），closeFuture 会结束阻塞，循环继续触发重连。
//                    this.channel.closeFuture().sync();
//                } catch (InterruptedException ignored) {
//                    log.info("Netty client shutdown now!");
//                    Thread.currentThread().interrupt();
//                } catch (Exception e2) {
//                    log.error("Netty client connect to server error: {}. try after 10s.", e2.getMessage());
//                    try {
//                        TimeUnit.SECONDS.sleep(10);
//                    } catch (InterruptedException ignored) {
//                    }
//                }
//            }
//
//            // 5. 循环退出后，优雅地关闭 Netty 线程组，释放系统资源
//            workerGroup.shutdownGracefully();
//        });
//    }
//
//    private void initChannel(final SocketChannel channel) {
//        ChannelPipeline pipeline = channel.pipeline();
//        // zip
//        pipeline.addLast(ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP, 0)); // 发送数据时压缩
//        pipeline.addLast(ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP, 0)); // 接收数据时解压
//        // protocol buf encode decode
//        pipeline.addLast(new ProtobufVarint32FrameDecoder()); // 解码：处理粘包/拆包
//        pipeline.addLast(new ProtobufDecoder(ClusterMsg.Message.getDefaultInstance()));
//        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender()); // 编码：加上长度前缀
//        pipeline.addLast(new ProtobufEncoder()); // 编码：Java 对象转二进制
//        pipeline.addLast(new NettyClientHandler());
//    }
//
//    @Override
//    public void shutdown() {
//        try {
//            if (this.channel != null) {
//                this.channel.close();
//            }
//            this.workerGroup.shutdownGracefully();
//        } catch (Exception e) {
//            log.error("Netty client shutdown exception, ", e);
//        }
//    }
//
//    @Override
//    public boolean isStart() {
//        return this.channel != null && this.channel.isActive();
//    }
//
//    class NettyClientHandler extends SimpleChannelInboundHandler<ClusterMsg.Message> {
//        @Override
//        public void channelActive(ChannelHandlerContext ctx) throws Exception {
//            NettyRemotingClient.this.channelActive(ctx);
//        }
//
//        @Override
//        protected void channelRead0(ChannelHandlerContext ctx, ClusterMsg.Message msg) throws Exception {
//            NettyRemotingClient.this.processReceiveMsg(ctx, msg);
//        }
//
//        @Override
//        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//            NettyRemotingClient.this.channelIdle(ctx, evt);
//        }
//    }
//}
