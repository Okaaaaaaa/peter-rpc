package com.peter.registry;

import com.peter.remoting.dto.RPCRequest;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {
    InetSocketAddress serviceDiscovery(RPCRequest request);
}
