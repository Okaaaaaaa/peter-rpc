package remoting.transport.netty.codec;

import remoting.constants.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import serializer.Serializer;

import java.util.List;

public class MyDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        // 自定义消息格式：
        // 1. 消息类型（response/ request/ ping/ pong）
        // 2. 序列化方式 json/ object
        // 3. 消息长度
        // 4. 具体消息内容

        short messageType = in.readShort();
        // 现在还只支持request与response请求
        if(messageType != MessageType.REQUEST.getCode() &&
                messageType != MessageType.RESPONSE.getCode()){
            System.out.println("暂不支持此种数据");
            return;
        }

        short serializerType = in.readShort();
        // 获取对应的序列化器
        Serializer serializer = Serializer.getSerializerByCode(serializerType);
        if(serializer == null){
            throw new RuntimeException("不存在对应的序列化器");
        }else{
            System.out.println("当前序列化器："+serializer.getClass().getName());
        }

        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);

        // 用serializer的deserialize方法，将bytes -> object
        Object deserialize = serializer.deserialize(bytes, messageType);

        // 写入输出流out中
        out.add(deserialize);
    }
}
