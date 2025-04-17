package com.peter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Peter
 */

@AllArgsConstructor
@Getter
public enum SerializerTypeEnum {

    OBJECT((short) 1, "object"),
    JSON((short) 2, "json"),
    KRYO((short) 3, "kryo");


    private final short code;
    private final String name;


    public static String getName(short code){
        for(SerializerTypeEnum serializerTypeEnum : SerializerTypeEnum.values()){
            if(serializerTypeEnum.getCode() == code){
                return serializerTypeEnum.getName();
            }
        }
        return null;
    }

    public static int getCode(SerializerTypeEnum serializerTypeEnum){
        return serializerTypeEnum.getCode();
    }
}
