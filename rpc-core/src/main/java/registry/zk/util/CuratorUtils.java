package registry.zk.util;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CuratorUtils {
    public static final String ROOT_PATH = "my-rpc";
    private static final int BASE_SLEEP_TIME_MS = 1000;
    private static final int MAX_RETRIES = 3;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    /**
     * consumer 监视了哪些serviceName
     */
    private static final ConcurrentHashMap<String, CuratorCache> CACHED_SERVICES = new ConcurrentHashMap<>();

    /**
     * consumer 缓存的provider地址
     */
    public static final ConcurrentHashMap<String, List<String>> PROVIDERS_ADDRESS_CACHE = new ConcurrentHashMap<>();


    private static CuratorFramework zkClient;

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
        System.out.println("Zookeeper连接成功");
        return zkClient;
    }

    /**
     * consumer监听server中的某个node
     */
    public static void registerWatcher(CuratorFramework zkClient, String serviceName){
        String path = "/" + serviceName;
        if(CACHED_SERVICES.containsKey(path)){
            System.out.println("当前Zookeeper客户端已经在监视"+serviceName+"服务");
            return;
        }

        CuratorCache cache = CuratorCache.build(zkClient, path);
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forPathChildrenCache(path, zkClient, (client, event)->{
                    switch (event.getType()){
                        case CHILD_ADDED:
                            System.out.println("节点上线："+event.getData().getPath());
                            updateProvidersAddress(zkClient,serviceName);
                            break;
                        case CHILD_UPDATED:
                            System.out.println("节点更新"+event.getData().getPath());
                            updateProvidersAddress(zkClient,serviceName);
                            break;
                        case CHILD_REMOVED:
                            System.out.println("节点下线"+event.getData().getPath());
                            updateProvidersAddress(zkClient,serviceName);
                            break;
                        default:
                            break;
                    }
                }).build();

        cache.listenable().addListener(listener);
        cache.start();
        CACHED_SERVICES.put(serviceName, cache);
    }

    /**
     * consumer更新 serviceName下缓存的provider地址
     */
    public static void updateProvidersAddress(CuratorFramework zkClient, String serviceName){
        try{
            String path = "/" + serviceName;
            List<String> addressList = zkClient.getChildren().forPath(path);
            PROVIDERS_ADDRESS_CACHE.put(serviceName, addressList);
            System.out.println("缓存更新：" + serviceName + "->" + addressList);
        } catch (Exception e){
            System.out.printf("更新服务：%s的缓存失败\n",serviceName);
        }
    }
    

    /**
     * provider在zkServer中加入serviceName（永久节点）、addr（临时节点）
     */
    public static void createNode(CuratorFramework zkClient, String serviceName, InetSocketAddress inetSocketAddress){
        // /my-rpc/userService
        String path = "/" + serviceName;
        try {
            // 将serviceName创建为永久节点
            if(zkClient.checkExists().forPath(path) == null){
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            }
            path += "/" + getServiceAddress(inetSocketAddress);
            registerWatcher(zkClient, serviceName);

            // 将provider地址创建为临时节点
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            System.out.printf("服务%s已存在\n",serviceName);
            throw new RuntimeException(e);
        }
    }

    public static String getServiceAddress(InetSocketAddress inetSocketAddress){
        return inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort();
    }

    public static InetSocketAddress parseAddress(String address){
        String[] strings = address.split(":");
        return new InetSocketAddress(strings[0], Integer.parseInt(strings[1]));
    }
}
