import blog.entity.Blog;
import blog.service.BlogService;
import proxy.ClientProxy;
import org.apache.dubbo.common.extension.ExtensionLoader;
import remoting.transport.RPCClient;
import remoting.transport.netty.client.NettyRPCClient;
import serializer.Serializer;
import user.entity.User;
import user.service.UserService;


public class Consumer2 {
    public static void main(String[] args) {
//        ExtensionLoader<Serializer> loader = ExtensionLoader.getExtensionLoader(Serializer.class);
//        Serializer serializer = loader.getExtension("json");
//        System.out.println("SPI加载的serializer:"+serializer.getClass().getName());
        RPCClient RPCClient = new NettyRPCClient();
        ClientProxy proxy = new ClientProxy(RPCClient);

        ExtensionLoader<Serializer> loader = ExtensionLoader.getExtensionLoader(Serializer.class);
        Serializer serializer = loader.getExtension("json");
        System.out.println("序列化器名称"+serializer.getClass().getName());

        // 获取不同接口的代理类
        UserService userService = proxy.getProxy(UserService.class);
        BlogService blogService = proxy.getProxy(BlogService.class);


        User user = User.builder().userName("张三").id(100).sex(true).build();
        Integer integer = userService.insertUser(user);
        System.out.println("调用insertUser方法，获得的返回值："+integer);
        System.out.println("\n==========================================\n");


        Blog blogById = blogService.getBlogById(10000);
        System.out.println("调用getBlogById方法，获得的返回值：" + blogById);
        System.out.println("\n==========================================\n");

    }
}
