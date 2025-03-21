package register;

import remoting.dto.RPCRequest;

import java.net.InetSocketAddress;

public interface ServiceRegister {
    void register(String serviceName, InetSocketAddress inetSocketAddress);
    InetSocketAddress serviceDiscovery(RPCRequest request);
}
