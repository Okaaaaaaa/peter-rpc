package registry;

import java.net.InetSocketAddress;

public interface ServiceRegistry {
    public void register(String serviceName, InetSocketAddress inetSocketAddress);
}
