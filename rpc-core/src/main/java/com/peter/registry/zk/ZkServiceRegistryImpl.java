package com.peter.registry.zk;

import com.peter.config.CustomizedShutdownHook;
import com.peter.registry.zk.util.CuratorUtil;
import com.peter.registry.ServiceRegistry;

import java.net.InetSocketAddress;

public class ZkServiceRegistryImpl implements ServiceRegistry {
    /**
     * provider将自身地址注册到zkServer对应的服务名下
     * @param serviceName
     * @param inetSocketAddress
     */
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        CuratorUtil.registerProviderByServiceName(CuratorUtil.getZkClient(), serviceName, inetSocketAddress);
    }
}
