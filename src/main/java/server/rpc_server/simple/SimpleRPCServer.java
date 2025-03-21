package server.rpc_server.simple;

import server.rpc_server.RPCServer;
import server.rpc_server.thread_pool.WorkThread;
import server.provider.ServiceProvider;

import java.net.ServerSocket;
import java.net.Socket;

public class SimpleRPCServer implements RPCServer {
    private ServiceProvider serviceProvider;

    public SimpleRPCServer(ServiceProvider serviceProvider){
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void start(int port) {
        try{
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("服务端已启动，端口为："+port);

            // BIO：来一个新连接就启动一个新线程
            while(true){
                Socket socket = serverSocket.accept();
                new Thread(new WorkThread(socket, serviceProvider)).start();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {

    }
}
