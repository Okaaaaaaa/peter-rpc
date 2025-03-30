package com.peter.remoting.transport.netty.client;

import com.peter.factory.SingletonFactory;
import com.peter.registry.ServiceDiscovery;
import com.peter.registry.zk.ZkServiceDiscoveryImpl;
import com.peter.remoting.transport.RPCClient;
import com.peter.remoting.transport.netty.codec.MyDecoder;
import com.peter.remoting.transport.netty.codec.MyEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import com.peter.remoting.dto.RPCRequest;
import com.peter.remoting.dto.RPCResponse;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class NettyRPCClient implements RPCClient {
    // 启动类，负责连接、指定encoder、decoder
    private static final Bootstrap bootStrap;

    // 线程池，负责处理事件和IO操作
    private static final EventLoopGroup eventLoopGroup;

    /**
     * 管理多个Channel连接
     */
    private final ChannelProvider channelProvider;

    private final UnprocessedRequests unprocessedRequests;


    private ServiceDiscovery serviceDiscovery;

    public NettyRPCClient(){
        this.serviceDiscovery = new ZkServiceDiscoveryImpl();
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    // 单例模式
    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootStrap = new Bootstrap();
        bootStrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        // 写空闲超过15s，关闭连接
                        pipeline.addLast(new IdleStateHandler(0, 15, 0, TimeUnit.SECONDS));
                        // 使用自定义的编码器、解码器
                        pipeline.addLast(new MyDecoder());
                        pipeline.addLast(new MyEncoder());
                        pipeline.addLast(new NettyClientHandler());
                }
            });
    }

    @Override
    public RPCResponse sendRequest(RPCRequest request) {
        // 服务发现，通过负载均衡返回目标provider地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.serviceDiscovery(request);
        // 若有通道，则复用；若无，则建立连接
        Channel channel = getChannel(inetSocketAddress);
        // rpc请求结果的future
        CompletableFuture<RPCResponse> resultFuture = new CompletableFuture<>();

        if(channel.isActive()){
            unprocessedRequests.put(request.getRequestId(), resultFuture);
            // 发送请求
            channel.writeAndFlush(request).addListener((ChannelFutureListener) future ->{
                if(future.isSuccess()){
                    System.out.println("请求已发送");
                }else{
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }

        try {
            return resultFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

//        try {
//            ChannelFuture channelFuture = bootStrap.connect(inetSocketAddress).sync();
//            Channel channel = channelFuture.channel();
//            channel.writeAndFlush(request);
//            // 阻塞client，直到关闭连接
//            channel.closeFuture().sync();
//            AttributeKey<RPCResponse> key = AttributeKey.valueOf("RPCResponse");
//            RPCResponse response = channel.attr(key).get();
//            return response;
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress){
        Channel channel = this.channelProvider.get(inetSocketAddress);
        // channel为空，说明之前没有建立连接
        if(channel == null){
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    public Channel doConnect(InetSocketAddress inetSocketAddress){
        System.out.println("目标服务器："+inetSocketAddress.toString());
        try {
            ChannelFuture channelFuture = bootStrap.connect(inetSocketAddress).sync();
            if(channelFuture.isSuccess()){
                System.out.println("连接服务器成功:"+inetSocketAddress.toString());
            }else {
                System.out.println("连接服务器失败:"+inetSocketAddress.toString());
            }
            return channelFuture.channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
