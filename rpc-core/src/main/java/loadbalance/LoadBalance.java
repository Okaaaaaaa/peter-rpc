package loadbalance;

import remoting.dto.RPCRequest;

import java.util.List;

public interface LoadBalance {
    String balance(List<String> addressList, RPCRequest request);
}
