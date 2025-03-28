package com.peter.remoting.transport.netty.client;

import com.peter.registry.ServiceDiscovery;
import com.peter.registry.zk.ZkServiceDiscoveryImpl;
import com.peter.remoting.transport.RPCClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import com.peter.remoting.dto.RPCRequest;
import com.peter.remoting.dto.RPCResponse;

import java.net.InetSocketAddress;

public class NettyRPCClient implements RPCClient {
    // 启动类，负责连接、指定encoder、decoder
    private static final Bootstrap bootStrap;

    // 线程池，负责处理事件和IO操作
    private static final EventLoopGroup eventLoopGroup;

    private String host;
    private int port;

    private ServiceDiscovery serviceDiscovery;

    public NettyRPCClient(){
//        this.serviceRegister = new ZkServiceRegister();
        this.serviceDiscovery = new ZkServiceDiscoveryImpl();
    }

    // 单例模式
    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootStrap = new Bootstrap();
        bootStrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
    }

    @Override
    public RPCResponse sendRequest(RPCRequest request) {
        try{
            InetSocketAddress inetSocketAddress = serviceDiscovery.serviceDiscovery(request);
            this.host = inetSocketAddress.getHostName();
            this.port = inetSocketAddress.getPort();
            ChannelFuture channelFuture = bootStrap.connect(host,port).sync();
            Channel channel = channelFuture.channel();
            // 向server发送request
            channel.writeAndFlush(request);
            channel.closeFuture().sync();
            AttributeKey<RPCResponse> key = AttributeKey.valueOf("RPCResponse");
            RPCResponse response = channel.attr(key).get();
            System.out.println("client获得server响应："+response);
            return response;
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return null;
    }
}
