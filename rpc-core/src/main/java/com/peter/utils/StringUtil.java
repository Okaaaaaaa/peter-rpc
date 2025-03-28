package com.peter.utils;

public class StringUtil {
    public static boolean isBlank(String s){
        if(s == null || s.isEmpty()){
            return true;
        }
        // 逐位判断
        for(int i=0;i<s.length();i++){
            // 不为空格
            if(!Character.isWhitespace(s.charAt(i))){
                return false;
            }
        }
        return true;
    }
}
