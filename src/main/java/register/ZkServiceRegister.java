package registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import common.RPCRequest;
import loadbalance.impl.ConsistentHashLoadBalance;
import loadbalance.LoadBalance;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ZkServiceRegister implements ServiceRegister {
    // zookeeper 客户端
    private CuratorFramework zkClient;

    // zookeeper根路径节点
    private static final String ROOT_PATH = "MyRPC";

    LoadBalance loadBalance = new ConsistentHashLoadBalance();

    // 缓存（服务名，所有提供该服务的服务地址列表）
    private final ConcurrentHashMap<String, List<String>> serviceCache = new ConcurrentHashMap<>();
    // 被监视的服务（能够及时动态更新缓存）
    private final ConcurrentHashMap<String, CuratorCache> cacheMap = new ConcurrentHashMap<>();

    // 构造函数：负责zookeeper client初始化、与server连接
    public ZkServiceRegister(){
        // （用于Zookeeper连接异常或请求失败时）指数时间重试
        RetryPolicy policy = new ExponentialBackoffRetry(1000,3);
        this.zkClient = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(40000)
                .retryPolicy(policy)
                .namespace(ROOT_PATH)
                .build();
        this.zkClient.start();
        System.out.println("Zookeeper连接成功");
    }

    /**
     * 监视某个服务下的所有实例
     */
    private void watchService(String serviceName){
        String path = "/" + serviceName;
        if(cacheMap.containsKey(path)){
            System.out.printf("当前服务%s已有监视器\n", serviceName);
            return;
        }

        CuratorCache cache = CuratorCache.build(zkClient,path);
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forPathChildrenCache(path,zkClient,(client, event) -> {
                    switch (event.getType()){
                        case CHILD_ADDED:
                            System.out.println("节点上线："+event.getData().getPath());
                            updateServiceCache(serviceName);
                            break;
                        case CHILD_UPDATED:
                            System.out.println("节点更新"+event.getData().getPath());
                            updateServiceCache(serviceName);
                            break;
                        case CHILD_REMOVED:
                            System.out.println("节点下线"+event.getData().getPath());
                            updateServiceCache(serviceName);
                            break;
                        default:
                            break;
                    }
                }).build();


        cache.listenable().addListener(listener);
        cache.start();
        cacheMap.put(serviceName, cache);
    }

    private void updateServiceCache(String serviceName){
        try{
            List<String> newAddressList = zkClient.getChildren().forPath("/"+serviceName);
            serviceCache.put(serviceName, newAddressList);
            System.out.println("缓存更新：" + serviceName + "->" + newAddressList);
        } catch (Exception e) {
            System.out.printf("更新服务：%s的缓存失败\n",serviceName);
        }
    }

    /**
     * 服务注册（服务名 -> 服务地址），监听服务变更
     */
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            // serviceName创建成永久节点，当provider下线时，不删除serviceName，只删除地址（即服务仍能被发现，但不能调用）
            // 若当前服务名不存在，则创建
            if(zkClient.checkExists().forPath("/"+serviceName) == null){
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/"+serviceName);
            }
            // 路径地址
            String path = "/" + serviceName + "/" + getServiceAddress(inetSocketAddress);
            // 监听服务变更
            watchService(serviceName);

            // 路径创建为临时节点
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            System.out.printf("服务%s已存在\n",serviceName);
        }
    }

    /**
     * 根据服务名返回服务地址
     */
    @Override
    public InetSocketAddress serviceDiscovery(RPCRequest request) {
        String serviceName = request.getInterfaceName();
        try {
            // 先查缓存，未命中再手动获取
            List<String> addressList = serviceCache.get(serviceName);

            if(addressList == null || addressList.isEmpty()){
                System.out.printf("客户端查询%s的服务器地址缓存未命中，从zookeeper中查询\n",serviceName);
                addressList = zkClient.getChildren().forPath("/" + serviceName);
                serviceCache.put(serviceName, addressList);
                watchService(serviceName);
            }
            if(addressList.isEmpty()){
                System.out.println("无服务可用");
                return null;
            }
            String addr = loadBalance.balance(addressList, request);
            return parseAddress(addr);
        } catch (Exception e) {
            System.out.printf("服务%s不存在\n",serviceName);
        }
        return null;
    }

    // 转换成字符串类型：主机名:端口
    public String getServiceAddress(InetSocketAddress inetSocketAddress){
        return inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort();
    }

    // 将 主机名:端口 转换为地址类型
    public InetSocketAddress parseAddress(String address){
        String[] split = address.split(":");
        return new InetSocketAddress(split[0],Integer.parseInt(split[1]));
    }
}
