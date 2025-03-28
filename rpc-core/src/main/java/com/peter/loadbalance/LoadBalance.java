package com.peter.loadbalance;

import com.peter.extension.SPI;
import com.peter.remoting.dto.RPCRequest;

import java.util.List;

@SPI
public interface LoadBalance {
    String balance(List<String> addressList, RPCRequest request);
}
