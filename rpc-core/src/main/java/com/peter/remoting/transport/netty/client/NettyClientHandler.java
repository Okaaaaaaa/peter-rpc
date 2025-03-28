package com.peter.remoting.transport.netty.client;

import com.peter.factory.SingletonFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import com.peter.remoting.dto.RPCResponse;

import java.util.concurrent.CompletableFuture;

public class NettyClientHandler extends SimpleChannelInboundHandler<RPCResponse> {
    private final UnprocessedRequests unprocessedRequests;

    public NettyClientHandler(){
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCResponse response) throws Exception {
        System.out.println("ClientHandler收到回复："+response.toString());
        // 获取到了response，将其从unprocessedRequests移除
        unprocessedRequests.complete(response);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state() == IdleState.READER_IDLE){
                System.out.println("客户端写读通道长时间空闲，关闭与服务器 "+ctx.channel().remoteAddress() +"连接");
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
