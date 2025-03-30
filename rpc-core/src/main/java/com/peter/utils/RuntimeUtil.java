package com.peter.utils;

public class RuntimeUtil {
    public static int cpus(){
        return Runtime.getRuntime().availableProcessors();
    }
}
