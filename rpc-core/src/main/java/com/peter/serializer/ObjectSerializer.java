package com.peter.serializer;

import java.io.*;

public class ObjectSerializer implements Serializer {

    // 序列化：object -> byte[]
    @Override
    public byte[] serialize(Object object) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try{
            // bos用作底层输出流。它允许将字节数据写入内存中，之后可以通过调用 toByteArray() 方法获取这些字节。
            // 所有通过 oos 写入的数据将被存储在 bos 中。
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();

            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    // 反序列化：byte[] -> object
    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object object = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            object = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

    @Override
    public int getType() {
        return 1;
    }
}
