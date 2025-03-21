package registry;

import remoting.dto.RPCRequest;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {
    InetSocketAddress serviceDiscovery(RPCRequest request);
}
