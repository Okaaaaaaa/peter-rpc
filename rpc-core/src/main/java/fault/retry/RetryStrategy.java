package fault.retry;

import remoting.dto.RPCResponse;

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
