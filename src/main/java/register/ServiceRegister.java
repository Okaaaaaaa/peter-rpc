package register;

import common.RPCRequest;

import java.net.InetSocketAddress;

public interface ServiceRegister {
    void register(String serviceName, InetSocketAddress inetSocketAddress);
    InetSocketAddress serviceDiscovery(RPCRequest request);
}
