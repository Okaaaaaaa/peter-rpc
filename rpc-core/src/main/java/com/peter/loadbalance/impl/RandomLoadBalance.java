package com.peter.loadbalance.impl;

import com.peter.loadbalance.LoadBalance;
import com.peter.remoting.dto.RPCRequest;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance {
    @Override
    public String balance(List<String> addressList, RPCRequest request) {
        Random random = new Random();
        int choice = random.nextInt(addressList.size());
        System.out.println("负载均衡选择了"+choice+"服务器");
        return addressList.get(choice);
    }
}
