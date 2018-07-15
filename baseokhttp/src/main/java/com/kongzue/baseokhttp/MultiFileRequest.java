package com.kongzue.baseokhttp;

import android.app.Activity;
import android.util.Log;

import com.kongzue.baseokhttp.exceptions.NetworkErrorException;
import com.kongzue.baseokhttp.listener.ResponseListener;
import com.kongzue.baseokhttp.util.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import baseokhttp3.Call;
import baseokhttp3.Callback;
import baseokhttp3.MediaType;
import baseokhttp3.MultipartBody;
import baseokhttp3.OkHttpClient;
import baseokhttp3.Request;
import baseokhttp3.RequestBody;
import baseokhttp3.Response;

import static com.kongzue.baseokhttp.HttpRequest.DEBUGMODE;
import static com.kongzue.baseokhttp.HttpRequest.serviceUrl;

/**
 * 多文件上传
 * Created by myzcx on 2018/1/9.
 * ver:1.1
 */

public class MultiFileRequest {
    
    private Parameter headers;
    private static Activity context;
    
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private final OkHttpClient client = new OkHttpClient();
    private ResponseListener responseListener;
    private Parameter parameter;
    
    //单例
    private static MultiFileRequest multiFileRequest;
    
    private MultiFileRequest() {
    }
    
    //默认请求创建方法
    public static MultiFileRequest getInstance(Activity c) {
        synchronized (MultiFileRequest.class) {
            if (multiFileRequest == null) {
                multiFileRequest = new MultiFileRequest();
            }
            context = c;
        }
        return multiFileRequest;
    }
    
    public Parameter getParameter() {
        return parameter;
    }
    
    public MultiFileRequest setParameter(Parameter parameter) {
        this.parameter = parameter;
        return this;
    }
    
    public Parameter getHeaders() {
        return headers;
    }
    
    public MultiFileRequest setHeaders(Parameter headers) {
        this.headers = headers;
        return this;
    }
    
    private List<String> fileNames;
    
    public List<String> getFileName() {
        return fileNames;
    }
    
    public MultiFileRequest setFileName(List<String> fileNames) {
        this.fileNames = fileNames;
        return this;
    }
    
    private String postUrl;
    
    public void doPost(String url, List<File> files, final ResponseListener listener) {
        
        postUrl = url;
        if (!postUrl.startsWith("http")) {
            postUrl = serviceUrl + postUrl;
        }
        responseListener = listener;
        
        // mImgUrls为存放图片的url集合
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (int i = 0; i < files.size(); i++) {
            File f = files.get(i);
            if (f != null) {
                String name = "";
                if (fileNames == null) {
                    name = "img" + (i + 1);
                } else {
                    name = fileNames.get(i);
                }
                builder.addFormDataPart(name, f.getName(), RequestBody.create(MEDIA_TYPE_PNG, f));
                if (DEBUGMODE) Log.i(">>>", "添加了一张图片：" + "img" + (i + 1) + ":" + f.getName());
            }
        }
        
        if (parameter != null) {
            if (!parameter.entrySet().isEmpty()) {
                for (Map.Entry<String, String> entry : parameter.entrySet()) {
                    builder.addFormDataPart(entry.getKey(), entry.getValue());
                }
            }
        }
        
        MultipartBody requestBody = builder.build();
        
        //创建请求
        Request request;
        Request.Builder httpBuilder = new Request.Builder();
        //请求类型处理
        httpBuilder.url(postUrl);
        httpBuilder.post(requestBody);
        //请求头处理
        if (parameter != null) {
            if (!headers.entrySet().isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }
        }
        request = httpBuilder.build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if (DEBUGMODE)
                    Log.i(">>>", "上传失败:e.getLocalizedMessage() = " + e.getLocalizedMessage());
                //回到主线程处理
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onResponse(null, new NetworkErrorException());
                    }
                });
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                if (DEBUGMODE) Log.i(">>>", "上传成功：response = " + result);
                try {
                    //回到主线程处理
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                listener.onResponse(result, null);
                            } catch (Exception e) {
                                listener.onResponse(null, e);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    responseListener.onResponse(null, new Exception("Request:response not a Json string."));
                }
            }
        });
    }
}
