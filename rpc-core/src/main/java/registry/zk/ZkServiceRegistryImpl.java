package registry.zk;

import registry.ServiceRegistry;
import registry.zk.util.CuratorUtils;

import java.net.InetSocketAddress;

public class ZkServiceRegistryImpl implements ServiceRegistry {
    /**
     * provider将自身地址注册到zkServer对应的服务名下
     * @param serviceName
     * @param inetSocketAddress
     */
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        CuratorUtils.createNode(CuratorUtils.getZkClient(), serviceName, inetSocketAddress);
    }
}
