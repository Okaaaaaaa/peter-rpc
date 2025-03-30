package com.peter.registry.zk.util;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CuratorUtil {
    public static final String ROOT_PATH = "my-rpc";
    private static final int BASE_SLEEP_TIME_MS = 1000;
    private static final int MAX_RETRIES = 3;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    // consumer缓存的provider地址
    public static final ConcurrentHashMap<String, List<String>> PROVIDERS_ADDRESS_CACHE = new ConcurrentHashMap<>();

    // 当前provider在zkServer上注册的路径集合
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();


    private static CuratorFramework zkClient;

    private CuratorUtil(){}


    /**
     * consumer、provider获得zkClient（与zkServer连接）
     */
    public static CuratorFramework getZkClient(){
        if(zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED){
            return zkClient;
        }

        // zkClient与server连接失败时的重试策略
        RetryPolicy policy = new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(DEFAULT_ZOOKEEPER_ADDRESS)
                .sessionTimeoutMs(40000)
                .retryPolicy(policy)
                .namespace(ROOT_PATH)
                .build();
        zkClient.start();
        System.out.println("ZkClient与ZkServer连接成功");
        return zkClient;
    }

    // =========== Consumer 相关方法 ===========

    /**
     * 【consumer】监听server中的指定service下的provider列表
     */
    public static void registerWatcher(CuratorFramework zkClient, String serviceName){
        String path = "/" + serviceName;
        CuratorCache cache = CuratorCache.build(zkClient, path);
        CuratorCacheListener listener = (type, childData, childData1) -> {
            try {
                List<String> addresses = zkClient.getChildren().forPath(path);
                // 监听到节点变动时，更新consumer本地缓存
                switch (type){
                    case NODE_CREATED:
                        System.out.println("节点上线："+childData1.getPath());
                        PROVIDERS_ADDRESS_CACHE.put(serviceName, addresses);
                        break;
                    case NODE_CHANGED:
                        System.out.println("节点更新："+childData1.getPath());
                        PROVIDERS_ADDRESS_CACHE.put(serviceName, addresses);
                        break;
                    case NODE_DELETED:
                        System.out.println("节点下线："+childData.getPath());
                        PROVIDERS_ADDRESS_CACHE.put(serviceName, addresses);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        cache.listenable().addListener(listener);
        cache.start();
    }

    /**
     * 【consumer】获取provider的地址列表
     * 缓存 || 直接获取并更新缓存
     */
    public static List<String> getProviderAddressList(CuratorFramework zkClient, String serviceName){
        if(PROVIDERS_ADDRESS_CACHE.containsKey(serviceName)){
            return PROVIDERS_ADDRESS_CACHE.get(serviceName);
        }
        List<String> result = null;
        String path = "/"+serviceName;
        try {
            result = zkClient.getChildren().forPath(path);
            PROVIDERS_ADDRESS_CACHE.put(serviceName, result);
            registerWatcher(zkClient, serviceName);
        } catch (Exception e) {
            System.out.println("获取服务"+serviceName+"提供者地址列表失败");
        }
        return result;
    }


    // =========== Provider 相关方法 ===========

    /**
     * 【provider】
     * 在zkServer中加入serviceName（永久节点）、addr（临时节点）
     */
    public static void registerProvider(CuratorFramework zkClient, String serviceName, InetSocketAddress inetSocketAddress){
        // /my-rpc/userService
        String path = "/" + serviceName;

        try {
            // 将service创建为永久节点
            if(zkClient.checkExists().forPath(path) == null){
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            }

            // 将provider地址创建为临时节点
            path += "/" + getServiceAddress(inetSocketAddress);
            if(REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null){
                System.out.println("路径 "+path+" 已注册过");
            }else{
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
                REGISTERED_PATH_SET.add(path);
            }
        } catch (Exception e) {
            System.out.printf("服务%s已存在\n",serviceName);
        }
    }

    public static void unregisterProvider(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        System.out.println("===================================================");
        System.out.println("provider下线，注销所有相关节点");
        REGISTERED_PATH_SET.parallelStream().forEach(p -> {
            // 删除当前provider注册的所有节点
            try{
                if(p.endsWith(getServiceAddress(inetSocketAddress))){
                    zkClient.delete().forPath(p);
                }
            }catch (Exception e){
                System.out.println("provider"+inetSocketAddress.toString()+"注销节点失败");
            }
        });
    }

    public static String getServiceAddress(InetSocketAddress inetSocketAddress){
        return inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort();
    }

    public static InetSocketAddress parseAddress(String address){
        String[] strings = address.split(":");
        return new InetSocketAddress(strings[0], Integer.parseInt(strings[1]));
    }
}
