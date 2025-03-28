package com.peter.remoting.transport.netty.server;

import com.peter.provider.ServiceProvider;
import com.peter.remoting.transport.RPCServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;
import com.peter.serializer.Serializer;

import java.util.ServiceLoader;

@AllArgsConstructor
public class NettyRPCServer implements RPCServer {
    private ServiceProvider serviceProvider;

    @Override
    public void start(int port) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        System.out.println("Netty服务端启动，端口为:"+port);

        ServiceLoader<Serializer> serializers = ServiceLoader.load(Serializer.class);
        for (Serializer serializer : serializers) {
            System.out.println(serializer.getClass().getName());
        }

        try {
            // 启动netty服务器
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 初始化
            serverBootstrap.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(serviceProvider));
            // 同步阻塞
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            // 死循环监听
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
