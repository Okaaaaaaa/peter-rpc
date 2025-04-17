package com.peter.config;

import com.peter.registry.zk.util.CuratorUtil;
import com.peter.utils.concurrent.threadpool.ThreadPoolFactoryUtil;

import java.net.InetSocketAddress;

public class CustomizedShutdownHook {
    private static final CustomizedShutdownHook CUSTOMIZED_SHUTDOWN_HOOK = new CustomizedShutdownHook();

    public static CustomizedShutdownHook getCustomizedShutDownHook(){
        return CUSTOMIZED_SHUTDOWN_HOOK;
    }

    public void unregisterFromZk(InetSocketAddress inetSocketAddress){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CuratorUtil.unregisterProvider(CuratorUtil.getZkClient(), inetSocketAddress);
        }));
    }

    public void serverCloseChannel(){
        Runtime.getRuntime().addShutdownHook(new Thread(()->{

        }));
    }
}
