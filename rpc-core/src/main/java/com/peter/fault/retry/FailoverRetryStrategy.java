package com.peter.fault.retry;

import com.peter.remoting.dto.RPCResponse;

import java.util.concurrent.Callable;

public class FailoverRetryStrategy implements RetryStrategy {
    /**
     * 重试
     *
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RPCResponse doRetry(Callable<RPCResponse> callable) throws Exception {
        // 获得当前服务的
        return null;
    }
}
