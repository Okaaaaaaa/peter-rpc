package com.peter.remoting.transport.socket;

import com.peter.registry.ServiceDiscovery;
import com.peter.registry.zk.ZkServiceDiscoveryImpl;
import lombok.AllArgsConstructor;
import com.peter.remoting.transport.RPCClient;
import com.peter.remoting.dto.RPCRequest;
import com.peter.remoting.dto.RPCResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@AllArgsConstructor
public class SocketRPCClient implements RPCClient {
    private String host;
    private int port;
    private ServiceDiscovery serviceDiscovery;

    public SocketRPCClient(){
        this.serviceDiscovery = new ZkServiceDiscoveryImpl();
    }
    @Override
    public RPCResponse sendRequest(RPCRequest request) {
        // 当客户端要调用某个服务时，request中指定了服务名，用这个服务名去zk中查找需要进行通信的服务端的host、port
        String serviceName = request.getInterfaceName();
        InetSocketAddress inetSocketAddress = serviceDiscovery.serviceDiscovery(request);
        host = inetSocketAddress.getHostName();
        port = inetSocketAddress.getPort();

        try{
            Socket socket = new Socket(host,port);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            System.out.println("客户端发送请求："+request);
            oos.writeObject(request);
            oos.flush();

            RPCResponse response = (RPCResponse) ois.readObject();
            return response;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
