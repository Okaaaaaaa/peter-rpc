package server.provider;

import register.ZkServiceRegister;
import register.ServiceRegister;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
    private Map<String, Object> interfaceProvider;

    // zk服务注册中心
    private ServiceRegister serviceRegister;
    private String host;
    private int port;

    public ServiceProvider(String host, int port){
        this.host = host;
        this.port = port;
        this.interfaceProvider = new HashMap<>();
        this.serviceRegister = new ZkServiceRegister();
    }

    // 服务注册（给定一个服务对象，可以通过反射获取服务名）
    public void register(Object service){
        // 一个实现类可能implement多个接口
        Class<?>[] interfaces = service.getClass().getInterfaces();

        for(Class<?> clazz: interfaces){
            // 本机映射表
            interfaceProvider.put(clazz.getName(), service);
            // 在注册中心注册服务
            serviceRegister.register(clazz.getName(),new InetSocketAddress(host,port));
            System.out.printf("接口名：%s，服务名：%s \n",clazz.getName(),service.getClass().getName());
        }
    }

    public Object getService(String interfaceName){
        return interfaceProvider.get(interfaceName);
    }
}
