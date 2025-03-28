package com.peter.fault.retry;

import com.peter.remoting.dto.RPCResponse;

import java.util.concurrent.Callable;

public interface RetryStrategy {

    /**
     * 重试
     *
     * @param callable
     * @return
     * @throws Exception
     */
    RPCResponse doRetry(Callable<RPCResponse> callable) throws Exception;
}
