package client.rpc_client.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import codec.MyDecoder;
import codec.MyEncoder;
import codec.serializer.KryoSerializer;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        // 使用自定义的编码器、解码器
        pipeline.addLast(new MyDecoder());
        pipeline.addLast(new MyEncoder(new KryoSerializer()));
        pipeline.addLast(new NettyClientHandler());
    }
}