package codec.serializer;

import org.apache.dubbo.common.extension.SPI;

@SPI
public interface Serializer {
    // 自定义消息格式：
    // 1. 消息类型（response/ request/ ping/ pong）
    // 2. 序列化方式 json/ object
    // 3. 消息长度
    // 4. 具体消息内容


    // object -> 字节数组
    byte[] serialize(Object object);

    // 字节数组 -> object
    Object deserialize(byte[] bytes, int messageType);

    int getType();

    static Serializer getSerializerByCode(int code){
        switch (code){
            case 0:
                return new ObjectSerializer();
            case 1:
                return new JsonSerializer();
            case 2:
                return new KryoSerializer();
            default:
                return null;
        }
    }
}
