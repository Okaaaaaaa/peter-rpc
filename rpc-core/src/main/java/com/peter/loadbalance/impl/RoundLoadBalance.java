package com.peter.loadbalance.impl;

import com.peter.loadbalance.LoadBalance;
import com.peter.remoting.dto.RPCRequest;

import java.util.List;

public class RoundLoadBalance implements LoadBalance {
    private int choice = -1;
    @Override
    public String balance(List<String> addressList, RPCRequest request) {
        choice++;
        choice = choice % addressList.size();
        System.out.println("负载均衡选择了"+choice+"服务器");
        return addressList.get(choice);
    }
}
