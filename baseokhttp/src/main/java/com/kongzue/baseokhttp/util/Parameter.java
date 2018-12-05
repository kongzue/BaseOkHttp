package com.kongzue.baseokhttp.util;

/**
 * Created by myzcx on 2018/1/22.
 */

import org.json.JSONObject;

import java.util.TreeMap;

import baseokhttp3.FormBody;
import baseokhttp3.RequestBody;

public class Parameter extends TreeMap<String, String> {
    
    public Parameter add(String key, String value) {
        put(key, value);
        return this;
    }
    
    public String toParameterString() {
        String result = "";
        if (!entrySet().isEmpty()) {
            for (Entry<String, String> entry : entrySet()) {
                result = result + entry.getKey() + "=" + entry.getValue() + "&";
            }
            if (result.endsWith("&")) {
                result = result.substring(0, result.length() - 1);
            }
        }
        return result;
    }
    
    public RequestBody toOkHttpParameter() {
        RequestBody requestBody;
        
        FormBody.Builder builder = new FormBody.Builder();
        for (Entry<String, String> entry : entrySet()) {
            builder.add(entry.getKey() + "", entry.getValue() + "");
        }
        
        requestBody = builder.build();
        return requestBody;
    }
    
    public JSONObject toParameterJson() {
        JSONObject result = new JSONObject();
        try{
            if (!entrySet().isEmpty()) {
                for (Entry<String, String> entry : entrySet()) {
                    result.put( entry.getKey(),entry.getValue());
                }
            }
            return result;
        }catch (Exception e){
            return null;
        }
    }
}