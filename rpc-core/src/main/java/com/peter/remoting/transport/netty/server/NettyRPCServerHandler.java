package com.peter.remoting.transport.netty.server;

import com.peter.provider.ServiceProvider;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.AllArgsConstructor;
import com.peter.remoting.dto.RPCRequest;
import com.peter.remoting.dto.RPCResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@AllArgsConstructor
public class NettyRPCServerHandler extends SimpleChannelInboundHandler<RPCRequest> {
    private ServiceProvider serviceProvider;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCRequest request) throws Exception {
        System.out.println("\n =================================================");
        System.out.println("服务端收到消息："+request);
        RPCResponse response = getResponse(request);
        ctx.writeAndFlush(response);
        // 每次处理完请求后，都会关闭通道，导致客户端无法复用
//        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 获取客户端的地址并打印
        Channel channel = ctx.channel();
        String clientAddress = channel.remoteAddress().toString();
        System.out.println("客户端连接成功: " + clientAddress);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state() == IdleState.READER_IDLE){
                System.out.println("服务器读通道长时间空闲，关闭与客户端 "+ctx.channel().remoteAddress() +"连接");
                ctx.close();
            }
        }
    }

    RPCResponse getResponse(RPCRequest request){
        System.out.println("接口（Service）名称："+request.getInterfaceName());
        String interfaceName = request.getInterfaceName();
        Object service = serviceProvider.getService(interfaceName);
        try{
            Method method = service.getClass().getMethod(request.getMethodName(),request.getParamsTypes());
            Object invoke = method.invoke(service,request.getParams());
            return RPCResponse.success(invoke, request.getRequestId());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("方法执行错误");
            return RPCResponse.fail();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
