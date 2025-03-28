package com.peter.remoting.transport.netty.client;

import com.peter.remoting.dto.RPCResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnprocessedRequests {
    private static final Map<String, CompletableFuture<RPCResponse>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RPCResponse> future){
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
    }

    public void complete(RPCResponse response){
        // 从map中移除
        CompletableFuture<RPCResponse> future = UNPROCESSED_RESPONSE_FUTURES.remove(response.getRequestId());
        // 通知等待方，结果已经准备好
        if(future != null){
            future.complete(response);
        }else {
            throw new IllegalStateException();
        }
    }

}
