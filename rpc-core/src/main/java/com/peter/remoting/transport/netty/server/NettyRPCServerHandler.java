package com.peter.remoting.transport.netty.server;

import com.peter.provider.ServiceProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
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
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    RPCResponse getResponse(RPCRequest request){
        System.out.println("接口（Service）名称："+request.getInterfaceName());
        String interfaceName = request.getInterfaceName();
        Object service = serviceProvider.getService(interfaceName);
        try{
            Method method = service.getClass().getMethod(request.getMethodName(),request.getParamsTypes());
            Object invoke = method.invoke(service,request.getParams());
            return RPCResponse.success(invoke);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("方法执行错误");
            return RPCResponse.fail();
        }
    }
}
