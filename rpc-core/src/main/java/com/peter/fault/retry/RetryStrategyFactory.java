package com.peter.fault.retry;


/**
 * @description 重试策略工厂（用于获取重试器对象）
 */
public class RetryStrategyFactory {


    /**
     * 默认重试器
     */
    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = new NoRetryStrategy();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static RetryStrategy getInstance(String key) {
        return DEFAULT_RETRY_STRATEGY;
    }
}
