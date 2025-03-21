package fault.retry;

import lombok.extern.slf4j.Slf4j;
import common.RPCResponse;

import java.util.concurrent.Callable;

/**
 * 不重试 - 重试策略
 *
 * @author chenyue7@foxmail.com
 * @date 16/5/2024
 * @description
 */
@Slf4j
public class NoRetryStrategy implements RetryStrategy{

    /**
     * 重试
     *
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RPCResponse doRetry(Callable<RPCResponse> callable) throws Exception {
        System.out.println("发生故障，不重试");
        return callable.call();
    }
}
