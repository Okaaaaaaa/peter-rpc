package com.peter.extension;

/**
 * 泛型容器，存储一个可变值value，并提供get、set方法
 * @param <T>
 */
public class Holder<T> {
    private volatile T value;

    public T get(){
        return this.value;
    }

    public void set(T value){
        this.value = value;
    }
}
