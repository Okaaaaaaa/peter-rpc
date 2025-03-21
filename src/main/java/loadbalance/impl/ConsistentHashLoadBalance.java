package loadbalance.impl;

import common.RPCRequest;
import loadbalance.LoadBalance;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConsistentHashLoadBalance implements LoadBalance {

    // 存储每个服务名对应的“环”，即ConsistentHashSelector
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    public String balance(List<String> addressList, RPCRequest request) {
        // 不直接使用addressList的hashcode：避免因
        int identityHashCode = new TreeSet<>(addressList).hashCode();
        String serviceName = request.getInterfaceName();

        // 获取该服务的selector
        ConsistentHashSelector selector = selectors.get(serviceName);

        // 调用selector的select方法
        if(selector == null || selector.identityHashCode != identityHashCode){
            System.out.println("初始化"+serviceName+"的HashRing");
            selectors.put(serviceName, new ConsistentHashSelector(addressList, 8, identityHashCode));
            selector = selectors.get(serviceName);
        }
        // key是由服务名称和请求参数组合而成的字符串。这种方式确保了相同请求的参数总是映射到同一个服务地址。
        String serverAddress = selector.select(serviceName+ Arrays.toString(request.getParams()));
        System.out.println("经一致性哈希算法选择出的serverAddress："+serverAddress);
        return serverAddress;
    }

    private int getAddressListHashCode(List<String> addressList){
        return addressList.stream().map(String::hashCode).reduce(0, Integer::sum);
    }

    static class ConsistentHashSelector{
        private final TreeMap<Long, String> virtualInvokers;

        private final int identityHashCode;

        /**
         *
         * @param invokers 节点列表，如 Node A、Node B、Node C
         * @param replicaNumber  副本数，即每个节点有多少个虚拟节点，如8
         * @param identityHashCode
         */
        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode){
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            // 遍历各个节点
            for(String invoker: invokers){
                // 当replicaNumber=8时，需要创建2组md5值
                for(int i=0; i<replicaNumber/4;i++){
                    byte[] digest = md5(invoker+i);
                    for(int h=0;h<4;h++){
                        long hash = hash(digest,h);
                        // Hash环
                        virtualInvokers.put(hash, invoker);
                    }
                }
            }
        }

        /**
         * 对输入的字符串key进行md5 Hash计算，并返回对应的Hash值
         */
        static byte[] md5(String key){
            // 假设传入的key是"hello world"
            MessageDigest md;
            try{
                md = MessageDigest.getInstance("MD5");
                // 将key转换成字节数组 [72, 101, 108, 108, 111]
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            // 返回的md5 Hash值[-25, 76, -91, -94, -31, 112, 116, -68, 109, -41, -92, 79, -3, 17, 97, -29]
            return md.digest();
        }

        // md5算法生成16字节，该hash函数每次取连续的4个字节（组成一个hash值），因此传入idx取值为0、1、2、3
        // 一个digest可以生成4个不同的hash值
        static long hash(byte[] digest, int idx){
            return ((long) (digest[3+idx*4] & 255) << 24 |
                    (long) (digest[2+idx*4] & 255) << 16 |
                    (long) (digest[1+idx*4] & 255) << 8 |
                    (long) (digest[idx*4] & 255)) & 4294967295L;
        }

        // 给定一个hashCode，在环上往后找第一个节点并返回节点地址
        public String selectForKey(long hashCode){
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();
            if(entry == null){
                entry = virtualInvokers.firstEntry();
            }
            return entry.getValue();
        }

        public String select(String key){
            byte[] digest = md5(key);
            return selectForKey(hash(digest,0));
        }
    }
}
