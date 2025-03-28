package com.peter.remoting.transport.netty.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import com.peter.remoting.transport.netty.codec.MyDecoder;
import com.peter.remoting.transport.netty.codec.MyEncoder;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        // 使用自定义的编码器、解码器
        pipeline.addLast(new MyDecoder());
        pipeline.addLast(new MyEncoder());
        pipeline.addLast(new NettyClientHandler());
    }
}