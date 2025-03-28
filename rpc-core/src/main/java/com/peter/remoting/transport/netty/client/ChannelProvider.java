package com.peter.remoting.transport.netty.client;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelProvider {
    private final Map<String, Channel> channelMap;
    public ChannelProvider(){
        this.channelMap = new ConcurrentHashMap<>();
    }

    public Channel get(InetSocketAddress inetSocketAddress){
        String key = inetSocketAddress.toString();
        if(channelMap.containsKey(key)){
            Channel channel = channelMap.get(key);
            // 若channel为空或已关闭，则移除
            if(channel != null && channel.isActive()){
                return channel;
            }else {
                channelMap.remove(key);
            }
        }
        return null;
    }

    public void set(InetSocketAddress inetSocketAddress, Channel channel){
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
    }

}
