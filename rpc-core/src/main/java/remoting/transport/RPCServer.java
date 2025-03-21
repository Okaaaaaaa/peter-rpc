package remoting.transport;

public interface RPCServer {
    void start(int port);
    void stop();
}
