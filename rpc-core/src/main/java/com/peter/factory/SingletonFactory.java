package com.peter.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SingletonFactory {
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    private static final Object lock = new Object();
    private SingletonFactory(){}

    /**
     * 获取clazz的单例对象
     */
    public static <T> T getInstance(Class<T> clazz){
        if (clazz == null) {
            throw new IllegalArgumentException();
        }
        String key = clazz.toString();
        // map中有，直接返回
        if(OBJECT_MAP.containsKey(key)){
            return clazz.cast(OBJECT_MAP.get(key));
        }

        // 用双重校验锁创建
        synchronized (lock){
            if(!OBJECT_MAP.containsKey(key)){
                try {
                    // 通过反射，创建clazz的一个实例对象
                    T instance = clazz.getDeclaredConstructor().newInstance();
                    OBJECT_MAP.put(key, instance);
                    return instance;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
            // 其他实例已经创建实例对象
            else {
                return clazz.cast(OBJECT_MAP.get(key));
            }
        }
    }
}
