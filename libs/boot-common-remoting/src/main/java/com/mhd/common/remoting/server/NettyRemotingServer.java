//package com.mhd.common.remoting.server;
//
//import com.google.common.util.concurrent.ThreadFactoryBuilder;
//import com.mhd.common.remoting.RemotingServer;
//import com.mhd.common.remoting.netty.NettyEventListener;
//import com.mhd.common.remoting.netty.NettyHook;
//import com.mhd.common.remoting.netty.NettyRemotingAbstract;
//import com.mhd.scada.common.entity.message.ClusterMsg;
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.channel.*;
//import io.netty.channel.epoll.EpollEventLoopGroup;
//import io.netty.channel.epoll.EpollServerSocketChannel;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.handler.codec.compression.ZlibCodecFactory;
//import io.netty.handler.codec.compression.ZlibWrapper;
//import io.netty.handler.codec.protobuf.ProtobufDecoder;
//import io.netty.handler.codec.protobuf.ProtobufEncoder;
//import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
//import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
//import io.netty.handler.logging.LogLevel;
//import io.netty.handler.logging.LoggingHandler;
//import io.netty.handler.timeout.IdleStateHandler;
//
//import java.net.InetSocketAddress;
//import java.nio.channels.Channel;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.ThreadFactory;
//
///**
// * Netty Remoting Server 实现类
// *
// * @author zhao-hao-dong
// */
//public class NettyRemotingServer extends NettyRemotingAbstract implements RemotingServer {
//    /**
//     * Netty Server 配置
//     */
//    private final NettyServerConfig nettyServerConfig;
//    /**
//     * 线程池，用于提交长耗时任务，避免阻塞 Netty 的 IO 线程
//     */
//    private final ExecutorService threadPool;
//    /**
//     * boss 线程组，用于服务端接受客户端的连接
//     */
//    private EventLoopGroup bossGroup;
//    /**
//     * worker 线程组，用于服务端接受客户端的数据读写
//     */
//    private EventLoopGroup workerGroup;
//    /**
//     * Netty Server Channel
//     */
//    private Channel channel = null;
//
//    public NettyRemotingServer(final NettyServerConfig nettyServerConfig,
//                               final NettyEventListener nettyEventListener,
//                               final ExecutorService threadPool) {
//        super(nettyEventListener);
//        this.nettyServerConfig = nettyServerConfig;
//        this.threadPool = threadPool;
//    }
//
//    @Override
//    public void sendMsg(final Channel channel, final ClusterMsg.Message request) {
//        this.sendMsgImpl(channel, request);
//    }
//
//    @Override
//    public ClusterMsg.Message sendMsgSync(final Channel channel, final ClusterMsg.Message request, final int timeoutMillis) {
//        return this.sendMsgSyncImpl(channel, request, timeoutMillis);
//    }
//
//    @Override
//    public void registerHook(List<NettyHook> nettyHookList) {
//        this.nettyHookList.addAll(nettyHookList);
//    }
//
//    @Override
//    public void start() {
//        // 提交一个长耗时任务到线程池，在后台异步启动 Netty 服务端并维持连接
//        this.threadPool.execute(() -> {
//            // 1. 创建 Netty Server 的自定义线程工厂
//            ThreadFactory bossThreadFactory = new ThreadFactoryBuilder()
//                    .setUncaughtExceptionHandler((thread, throwable) -> {
//                        log.error("Netty server boss has uncaughtException.");
//                        log.error(throwable.getMessage(), throwable);
//                    })
//                    .setDaemon(true)
//                    .setNameFormat("netty-server-boss-%d")
//                    .build();
//            ThreadFactory workerThreadFactory = new ThreadFactoryBuilder()
//                    .setUncaughtExceptionHandler((thread, throwable) -> {
//                        log.error("Netty server worker has uncaughtException.");
//                        log.error(throwable.getMessage(), throwable);
//                    })
//                    .setDaemon(true)
//                    .setNameFormat("netty-server-worker-%d")
//                    .build();
//
//            // 2. 根据配置选择使用 Epoll 或 NIO 创建 EventLoopGroup
//            if (this.useEpoll()) {
//                bossGroup = new EpollEventLoopGroup(bossThreadFactory);
//                workerGroup = new EpollEventLoopGroup(workerThreadFactory);
//            } else {
//                bossGroup = new NioEventLoopGroup(bossThreadFactory);
//                workerGroup = new NioEventLoopGroup(workerThreadFactory);
//            }
//
//            // 3. 启动 Netty Server
//            try {
//                int port = this.nettyServerConfig.getPort();
//                // 创建 ServerBootstrap 对象，用于 Netty Server 启动
//                ServerBootstrap b = new ServerBootstrap();
//                b.group(bossGroup, workerGroup)
//                        .channel(this.useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
//                        .handler(new LoggingHandler(LogLevel.INFO))
//                        .localAddress(new InetSocketAddress(port))
//                        .option(ChannelOption.SO_BACKLOG, 1024) // 设置 TCP 连接请求的等待队列长度为 1024。当客户端并发连接请求非常多，Boss 线程处理不过来时，多余的连接请求会在这个队列中排队。
//                        .option(ChannelOption.SO_REUSEADDR, true) // 允许重复使用本地地址和端口。对于服务端来说，设置为 true 可以在服务端重启后立即绑定到相同的端口，而不会因为 TIME_WAIT 状态而导致绑定失败。
//                        .childOption(ChannelOption.TCP_NODELAY, true) // 关闭 TCP 的 Nagle 算法。Nagle 算法会将小数据包合并发送以减少网络开销，但这会导致延迟。设置为 true 表示数据一旦产生就立即发送，保证通信的低延迟。
//                        .childOption(ChannelOption.SO_KEEPALIVE, false) // 关闭 TCP 底层的心跳保活机制。自己实现应用层的心跳检测
//                        .childHandler(new ChannelInitializer<SocketChannel>() {
//                            @Override
//                            protected void initChannel(SocketChannel channel) throws Exception {
//                                NettyRemotingServer.this.initChannel(channel);
//                            }
//                        });
//                // 启动并获取通道
//                channel = b.bind().sync().channel();
//                // 阻塞主线程
//                channel.closeFuture().sync();
//            } catch (InterruptedException ignored) {
//                log.info("Netty server shutdown now!");
//            } catch (Exception e) {
//                log.error("Netty Server start exception, {}", e.getMessage());
//                throw new RuntimeException(e);
//            } finally {
//                bossGroup.shutdownGracefully();
//                workerGroup.shutdownGracefully();
//            }
//        });
//    }
//
//    private void initChannel(final SocketChannel channel) {
//        ChannelPipeline pipeline = channel.pipeline();
//        // zip
//        pipeline.addLast(ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP, 0));
//        pipeline.addLast(ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP, 0));
//        // protocol buf encode decode
//        pipeline.addLast(new ProtobufVarint32FrameDecoder());
//        pipeline.addLast(new ProtobufDecoder(ClusterMsg.Message.getDefaultInstance()));
//        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
//        pipeline.addLast(new ProtobufEncoder());
//        // idle state
//        pipeline.addLast(new IdleStateHandler(0, 0, nettyServerConfig.getIdleStateEventTriggerTime()));
//        pipeline.addLast(new NettyServerHandler());
//    }
//
//    @Override
//    public void shutdown() {
//        try {
//            this.bossGroup.shutdownGracefully();
//            this.workerGroup.shutdownGracefully();
//        } catch (Exception e) {
//            log.error("Netty Server shutdown exception, ", e);
//        }
//    }
//
//    @Override
//    public boolean isStart() {
//        return this.channel != null && this.channel.isActive();
//    }
//
//    /**
//     * netty server handler
//     * ChannelHandler.Sharable 代表允许跨连接共享实例。
//     * 在 Netty 中，默认情况下每个新建立的 TCP 连接（Channel）都会创建一个新的 ChannelPipeline
//     * 加上 @ChannelHandler.Sharable 后，意味着你可以创建一个全局的 NettyServerHandler 单例对象，并将其添加到成千上万个不同的 ChannelPipeline 中。
//     */
//    @ChannelHandler.Sharable
//    public class NettyServerHandler extends SimpleChannelInboundHandler<ClusterMsg.Message> {
//
//        @Override
//        public void channelActive(ChannelHandlerContext ctx) throws Exception {
//            NettyRemotingServer.this.channelActive(ctx);
//        }
//
//        @Override
//        protected void channelRead0(ChannelHandlerContext ctx, ClusterMsg.Message msg) throws Exception {
//            NettyRemotingServer.this.processReceiveMsg(ctx, msg);
//        }
//
//        @Override
//        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//            NettyRemotingServer.this.channelIdle(ctx, evt);
//        }
//    }
//}
