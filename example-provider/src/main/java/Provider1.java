import blog.service.BlogService;
import remoting.transport.netty.server.NettyRPCServer;
import remoting.transport.RPCServer;
import provider.ServiceProvider;
import service.impl.UserServiceImpl;
import service.impl.BlogServiceImpl;
import user.service.UserService;

public class Provider1 {
    public static void main(String[] args) {
        UserService userService = new UserServiceImpl();
        BlogService blogService = new BlogServiceImpl();

//        // 手动注册
//        Map<String, Object> serviceProvider = new HashMap<>();
//        serviceProvider.put("version2.service.UserService", userService);
//        serviceProvider.put("version2.service.BlogService", blogService);

        // 自动注册
        ServiceProvider serviceProvider = new ServiceProvider("127.0.0.1",8899);
        serviceProvider.register(userService);
        serviceProvider.register(blogService);

//        RPCServer RPCServer = new SocketRPCServer(serviceProvider);
//        RPCServer RPCServer = new ThreadPoolRPCServer(serviceProvider);
        RPCServer RPCServer = new NettyRPCServer(serviceProvider);
        RPCServer.start(8899);
    }
}
