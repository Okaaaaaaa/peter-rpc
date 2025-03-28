package com.peter.remoting.transport.netty.server;

import com.peter.provider.ServiceProvider;
import com.peter.remoting.transport.RPCServer;
import com.peter.remoting.transport.netty.codec.MyDecoder;
import com.peter.remoting.transport.netty.codec.MyEncoder;
import com.peter.utils.RuntimeUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.AllArgsConstructor;
import com.peter.serializer.Serializer;

import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class NettyRPCServer implements RPCServer {
    private ServiceProvider serviceProvider;

    @Override
    public void start(int port) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        System.out.println("Netty服务端启动，端口为:"+port);

        try {
            // 启动netty服务器
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 初始化
            serverBootstrap.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 30s后若未收到客户端的请求，就关闭连接
                            pipeline.addLast(new IdleStateHandler(15, 0, 0, TimeUnit.SECONDS));
                            // 加入自定义编码器、解码器
                            pipeline.addLast(new MyDecoder());
                            pipeline.addLast(new MyEncoder());
                            pipeline.addLast(new NettyRPCServerHandler(serviceProvider));
                        }
                    });

            // 同步阻塞，等待端口绑定成功
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            // 等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {

    }
}
