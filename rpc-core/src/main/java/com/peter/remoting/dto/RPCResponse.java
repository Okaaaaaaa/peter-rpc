package com.peter.remoting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RPCResponse implements Serializable {

    private String requestId;
    // 调用状态码
    private int code;
    // 调用状态描述
    private String message;
    // 数据类型
    private Class<?> dataType;
    // 具体数据
    private Object data;

    // 成功
    public static RPCResponse success(Object data, String requestId){
        return RPCResponse.builder().code(200).dataType(data.getClass()).data(data).build();
    }
    // 失败
    public static RPCResponse fail(){
        return RPCResponse.builder().code(500).message("服务器发生错误").build();
    }
}
