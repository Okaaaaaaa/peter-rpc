package loadbalance;

import common.RPCRequest;

import java.util.List;

public interface LoadBalance {
    String balance(List<String> addressList, RPCRequest request);
}
