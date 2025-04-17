package com.peter.fault.retry;

import lombok.extern.slf4j.Slf4j;
import com.peter.remoting.dto.RPCResponse;

import java.util.concurrent.Callable;

/**
 * 不重试 - 重试策略
 */
@Slf4j
public class NoRetryStrategy implements RetryStrategy {

    /**
     * 重试
     *
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RPCResponse doRetry(Callable<RPCResponse> callable) throws Exception {
        System.out.println("不重试");
        return callable.call();
    }
}
