package fault.retry;

import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;
import common.RPCResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 固定时间间隔 - 重试策略
 *
 * @author chenyue7@foxmail.com
 * @date 16/5/2024
 * @description
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy{

    /**
     * 重试
     *
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RPCResponse doRetry(Callable<RPCResponse> callable) throws Exception {
        Retryer<RPCResponse> retryer = RetryerBuilder.<RPCResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)
                // 默认重试3次
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试次数 {}", attempt.getAttemptNumber());
                    }
                })
                .build();
        return retryer.call(callable);
    }
}
