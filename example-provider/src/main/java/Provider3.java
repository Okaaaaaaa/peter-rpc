import blog.service.BlogService;
import com.peter.remoting.transport.netty.server.NettyRPCServer;
import com.peter.remoting.transport.RPCServer;
import com.peter.provider.ServiceProvider;
import service.impl.BlogServiceImpl;
import service.impl.UserServiceImpl;
import user.service.UserService;

public class Provider3 {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 8901;
        UserService userService = new UserServiceImpl();
        BlogService blogService = new BlogServiceImpl();

//        // 手动注册
//        Map<String, Object> serviceProvider = new HashMap<>();
//        serviceProvider.put("version2.service.UserService", userService);
//        serviceProvider.put("version2.service.BlogService", blogService);

        // 自动注册
        ServiceProvider serviceProvider = new ServiceProvider(host,port);
        serviceProvider.register(userService);
        serviceProvider.register(blogService);

//        RPCServer RPCServer = new SocketRPCServer(serviceProvider);
//        RPCServer RPCServer = new ThreadPoolRPCServer(serviceProvider);
        RPCServer RPCServer = new NettyRPCServer(serviceProvider);
        RPCServer.start(port);
    }
}
