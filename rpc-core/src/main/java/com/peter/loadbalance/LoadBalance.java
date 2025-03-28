package com.peter.loadbalance;

import com.peter.remoting.dto.RPCRequest;

import java.util.List;

public interface LoadBalance {
    String balance(List<String> addressList, RPCRequest request);
}
