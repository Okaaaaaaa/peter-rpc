package com.peter.remoting.transport.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import com.peter.remoting.dto.RPCResponse;

public class NettyClientHandler extends SimpleChannelInboundHandler<RPCResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCResponse response) throws Exception {
        AttributeKey<RPCResponse> key = AttributeKey.valueOf("RPCResponse");
        ctx.channel().attr(key).set(response);
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
