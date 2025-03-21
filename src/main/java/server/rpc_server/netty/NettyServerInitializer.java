package server.rpc_server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.AllArgsConstructor;
import codec.MyDecoder;
import codec.MyEncoder;
import codec.serializer.KryoSerializer;
import server.provider.ServiceProvider;


@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        // 加入自定义编码器、解码器
        pipeline.addLast(new MyDecoder());
        pipeline.addLast(new MyEncoder(new KryoSerializer()));
        pipeline.addLast(new NettyRPCServerHandler(serviceProvider));
    }
}
