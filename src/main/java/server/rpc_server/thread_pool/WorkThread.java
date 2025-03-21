package server.rpc_server.thread_pool;

import lombok.AllArgsConstructor;
import common.RPCRequest;
import common.RPCResponse;
import server.provider.ServiceProvider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;

@AllArgsConstructor
public class WorkThread implements Runnable{
    private Socket socket;

    private ServiceProvider serviceProvider;

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            // 从ois中读取客户端调用的方法
            RPCRequest request = (RPCRequest) ois.readObject();

            // 调用server对应的方法，获得结果
            RPCResponse response = getResponse(request);

            // 将结果写入oos中，返回给客户端
            oos.writeObject(response);
            oos.flush();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 调用server中client所指定的方法
     * @param request
     * @return
     */
    private RPCResponse getResponse(RPCRequest request){
        // 获取接口名
        String interfaceName = request.getInterfaceName();
        // 从map中查找，获取对应的服务实例
        Object service = serviceProvider.getService(interfaceName);
        try{
            Method method = service.getClass().getMethod(request.getMethodName(),request.getParamsTypes());
            Object invoke = method.invoke(service, request.getParams());
            return RPCResponse.success(invoke);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("方法执行错误");
            return RPCResponse.fail();
        }
    }

}
