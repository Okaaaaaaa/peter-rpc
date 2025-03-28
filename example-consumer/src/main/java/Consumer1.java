import blog.entity.Blog;
import blog.service.BlogService;
import com.peter.extension.ExtensionLoader;
import com.peter.proxy.ClientProxy;
import com.peter.remoting.transport.netty.client.NettyRPCClient;
import com.peter.remoting.transport.RPCClient;
import com.peter.serializer.Serializer;
import user.entity.User;
import user.service.UserService;


public class Consumer1 {
    public static void main(String[] args) {
//        ExtensionLoader<Serializer> loader = ExtensionLoader.getExtensionLoader(Serializer.class);
//        Serializer serializer = loader.getExtension("json");
//        System.out.println("SPI加载的serializer:"+serializer.getClass().getName());
        RPCClient RPCClient = new NettyRPCClient();
        ClientProxy proxy = new ClientProxy(RPCClient);

        // 获取不同接口的代理类
        UserService userService = proxy.getProxy(UserService.class);
        BlogService blogService = proxy.getProxy(BlogService.class);

        // 调用代理类的方法
        User userById = userService.getUserById(10);
        System.out.println("调用getUserById方法，获得的返回值："+userById);
        System.out.println("\n==========================================\n");

        // 重复调用同一个方法、传入相同参数，检查是否确实选择同一个服务
        userById = userService.getUserById(10);
        System.out.println("调用getUserById方法，获得的返回值："+userById);
        System.out.println("\n==========================================\n");


        User user = User.builder().userName("张三").id(100).sex(true).build();
        Integer integer = userService.insertUser(user);
        System.out.println("调用insertUser方法，获得的返回值："+integer);
        System.out.println("\n==========================================\n");


        Blog blogById = blogService.getBlogById(10000);
        System.out.println("调用getBlogById方法，获得的返回值：" + blogById);
        System.out.println("\n==========================================\n");

    }
}
