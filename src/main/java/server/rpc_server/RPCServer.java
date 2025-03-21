package server.rpc_server;

public interface RPCServer {
    void start(int port);
    void stop();
}
