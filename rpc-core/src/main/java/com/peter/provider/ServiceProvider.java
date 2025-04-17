package com.peter.provider;

import com.peter.config.CustomizedShutdownHook;
import com.peter.registry.ServiceRegistry;
import com.peter.registry.zk.ZkServiceRegistryImpl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceProvider {
    private Map<String, Object> interfaceProvider;

    // zk服务注册中心
    private ServiceRegistry serviceRegistry;

    private String host;
    private int port;

    public ServiceProvider(String host, int port){
        this.host = host;
        this.port = port;
        this.interfaceProvider = new ConcurrentHashMap<>();
        this.serviceRegistry = new ZkServiceRegistryImpl();
        // JVM关闭hook：注销zk节点
        CustomizedShutdownHook.getCustomizedShutDownHook().unregisterFromZk(new InetSocketAddress(host, port));
    }

    // 服务注册（给定一个服务对象，可以通过反射获取服务名）
    public void register(Object service){
        // 一个实现类可能implement多个接口
        Class<?>[] interfaces = service.getClass().getInterfaces();

        for(Class<?> clazz: interfaces){
            // 本机映射表
            interfaceProvider.put(clazz.getName(), service);
            // 在注册中心注册服务
            serviceRegistry.register(clazz.getName(),new InetSocketAddress(host,port));
        }
    }

    public Object getService(String interfaceName){
        return interfaceProvider.get(interfaceName);
    }
}
