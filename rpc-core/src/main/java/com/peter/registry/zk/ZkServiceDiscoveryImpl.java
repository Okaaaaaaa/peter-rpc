package com.peter.registry.zk;

import com.peter.enums.LoadBalanceTypeEnum;
import com.peter.extension.ExtensionLoader;
import com.peter.loadbalance.LoadBalance;
import com.peter.registry.zk.util.CuratorUtil;
import org.apache.curator.framework.CuratorFramework;
import com.peter.registry.ServiceDiscovery;
import com.peter.remoting.dto.RPCRequest;

import java.net.InetSocketAddress;
import java.util.List;

public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadbalance;

    public ZkServiceDiscoveryImpl(){
        this.loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class)
                .getExtensionInstance(LoadBalanceTypeEnum.CONSISTENT_HASH.getName());
    }

    @Override
    public InetSocketAddress serviceDiscovery(RPCRequest request) {
        String serviceName = request.getInterfaceName();
        CuratorFramework zkClient = CuratorUtil.getZkClient();
        List<String> providers = CuratorUtil.getProviderAddressList(zkClient, serviceName);
        if(providers == null || providers.isEmpty()){
            throw new RuntimeException("服务"+serviceName+"没有提供者可用");
        }

        // 负载均衡选出一个服务器
        String address = loadbalance.balance(providers, request);
        return CuratorUtil.parseAddress(address);
    }
}
