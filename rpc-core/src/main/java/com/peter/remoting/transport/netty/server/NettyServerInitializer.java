package com.peter.remoting.transport.netty.server;

import com.peter.provider.ServiceProvider;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.AllArgsConstructor;
import com.peter.remoting.transport.netty.codec.MyDecoder;
import com.peter.remoting.transport.netty.codec.MyEncoder;
import com.peter.serializer.KryoSerializer;


@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        // 加入自定义编码器、解码器
        pipeline.addLast(new MyDecoder());
        pipeline.addLast(new MyEncoder());
        pipeline.addLast(new NettyRPCServerHandler(serviceProvider));
    }
}
