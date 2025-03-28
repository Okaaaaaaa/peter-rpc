package com.peter.proxy;

import lombok.AllArgsConstructor;
import com.peter.remoting.transport.RPCClient;
import com.peter.remoting.dto.RPCRequest;
import com.peter.remoting.dto.RPCResponse;
import com.peter.fault.retry.RetryStrategy;
import com.peter.fault.retry.RetryStrategyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@AllArgsConstructor
public class ClientProxy implements InvocationHandler {
    // 被代理对象
    private RPCClient client;

    private final RetryStrategy retryStrategy = RetryStrategyFactory.getInstance("");

    // interfaceName：UserServiceImpl
    // methodName：getUserById、insertUser
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("触发了"+method.getName()+"的invoke方法");
        // 构建request
        RPCRequest request = RPCRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args)
                .paramsTypes(method.getParameterTypes())
                .build();

        // 传输数据
        // 引入重试机制
        try{
            RPCResponse response = retryStrategy.doRetry(() -> client.sendRequest(request));
            return response.getData();
        }catch (Exception e){
            // 容错机制 pass
            e.printStackTrace();
        }
        return null;
    }

    // 获取代理类的对象 clazz是接口的类
    public <T>T getProxy(Class<T> clazz){
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},this);
        return (T)o;
    }
}
