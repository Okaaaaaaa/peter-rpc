package serializer;

import blog.entity.Blog;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import remoting.dto.RPCRequest;
import remoting.dto.RPCResponse;
import user.entity.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KryoSerializer implements Serializer{

    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(()->{
        Kryo kryo = new Kryo();
        kryo.register(RPCRequest.class);
        kryo.register(RPCResponse.class);
        // 注册基本类型
        kryo.register(String.class);
        kryo.register(Object[].class);   // 注册 Object 数组
        kryo.register(Class[].class);    // 注册 Class 数组
        kryo.register(Class.class);      // 注册 Class 类型
        // 如果 Object[] 中包含自定义类型，也需要注册
        // 例如，如果 params 中会传递 Integer：
        kryo.register(Integer.class);   // 注册 Integer 类型
        kryo.register(Boolean.class);
        kryo.register(User.class);
        kryo.register(Blog.class);
        return kryo;
    });

    // object -> bytes[]
    @Override
    public byte[] serialize(Object object) {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Output output = new Output(bos)){
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, object);
            output.flush();
            return bos.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    // bytes[] -> object
    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        try(ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            Input input = new Input(bis)){
            Kryo kryo = kryoThreadLocal.get();
            Class<?> clazz = null;
            switch (messageType){
                case 0:
                    clazz = RPCRequest.class;
                    break;
                case 1:
                    clazz = RPCResponse.class;
                    break;
                default:
                    throw new IllegalArgumentException("暂不支持该消息类型");
            }
            return kryo.readObject(input, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getType() {
        return 2;
    }
}
