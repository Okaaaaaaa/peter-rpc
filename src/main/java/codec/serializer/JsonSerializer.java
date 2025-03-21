package codec.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import common.RPCRequest;
import common.RPCResponse;

import java.util.Arrays;

public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        System.out.println("serialize收到对象："+object.toString());
        return JSONObject.toJSONBytes(object);
    }

    // bytes -> json串
    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        System.out.println("deserialize收到字节流："+ Arrays.toString(bytes));
        Object object = null;
        // 根据不同消息类型进行不同操作
        switch (messageType){
            case 0:
                // request中包括了：interfaceName、methodName、params、paramsTypes
                RPCRequest request = JSON.parseObject(bytes, RPCRequest.class);
                if(request.getParams() == null){
                    return request;
                }

                // 参数数组
                Object[] objects = new Object[request.getParams().length];
                for(int i=0;i<objects.length;i++){
                    Class<?> paramsType = request.getParamsTypes()[i];
                    // 对应位置的参数类型不匹配
                    if(!paramsType.isAssignableFrom(request.getParams()[i].getClass())){
                        // 如果不匹配，使用 JSONObject.toJavaObject 方法将 JSON 对象转换为预期的 Java 类型。
                        objects[i] = JSONObject.toJavaObject((JSONObject)request.getParams()[i],request.getParamsTypes()[i]);
                    }else{
                        objects[i] = request.getParams()[i];
                    }
                }
                request.setParams(objects);
                object = request;
                break;
            case 1:
                RPCResponse response = JSON.parseObject(bytes, RPCResponse.class);
                Class<?> dataType = response.getDataType();
                // 判断数据类型与预期类型是否一致
                if(!dataType.isAssignableFrom(response.getData().getClass())){
                    // 将数据类型强制转成dataType
                    response.setData(JSONObject.toJavaObject((JSONObject)response.getData(),dataType));
                }
                object = response;
                break;
            default:
                System.out.println("暂时不支持此种消息");
                throw new RuntimeException();
        }
        return object;
    }

    @Override
    public int getType() {
        return 1;
    }
}
