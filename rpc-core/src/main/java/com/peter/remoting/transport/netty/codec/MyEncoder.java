package com.peter.remoting.transport.netty.codec;

import com.peter.remoting.constants.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import com.peter.serializer.Serializer;
import com.peter.remoting.dto.RPCRequest;
import com.peter.remoting.dto.RPCResponse;

@AllArgsConstructor
public class MyEncoder extends MessageToByteEncoder {
    private Serializer serializer;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf out) throws Exception {
        // 自定义消息格式：
        // 1. 消息类型（response/ request/ ping/ pong）
        // 2. 序列化方式 json/ object
        // 3. 消息长度
        // 4. 具体消息内容

        if(msg instanceof RPCRequest){
            out.writeShort(MessageType.REQUEST.getCode());
        }else if (msg instanceof RPCResponse){
            out.writeShort(MessageType.RESPONSE.getCode());
        }

        out.writeShort(serializer.getType());

        byte[] serializedMsg = serializer.serialize(msg);
        out.writeInt(serializedMsg.length);

        out.writeBytes(serializedMsg);
    }
}
