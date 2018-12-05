package com.kongzue.baseokhttp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.kongzue.baseokhttp.exceptions.NetworkErrorException;
import com.kongzue.baseokhttp.exceptions.TimeOutException;
import com.kongzue.baseokhttp.listener.ResponseInterceptListener;
import com.kongzue.baseokhttp.listener.ResponseListener;
import com.kongzue.baseokhttp.util.JsonFormat;
import com.kongzue.baseokhttp.util.Parameter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import baseokhttp3.Cache;
import baseokhttp3.Call;
import baseokhttp3.Callback;
import baseokhttp3.MediaType;
import baseokhttp3.OkHttpClient;
import baseokhttp3.RequestBody;

import static com.kongzue.baseokhttp.HttpRequest.DEBUGMODE;
import static com.kongzue.baseokhttp.HttpRequest.GET_REQUEST;
import static com.kongzue.baseokhttp.HttpRequest.POST_REQUEST;
import static com.kongzue.baseokhttp.HttpRequest.SSLInAssetsFileName;
import static com.kongzue.baseokhttp.HttpRequest.TIME_OUT_DURATION;
import static com.kongzue.baseokhttp.HttpRequest.httpsVerifyServiceUrl;
import static com.kongzue.baseokhttp.HttpRequest.overallHeader;
import static com.kongzue.baseokhttp.HttpRequest.overallParameter;
import static com.kongzue.baseokhttp.HttpRequest.parameterInterceptListener;
import static com.kongzue.baseokhttp.HttpRequest.responseInterceptListener;
import static com.kongzue.baseokhttp.HttpRequest.serviceUrl;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/11/26 13:35
 */
public class JsonRequest {
    
    //请求头（单次请求内）
    private Parameter headers;
    //请求参数
    private String parameter;
    
    private Context context;
    private ResponseListener listener;
    private OkHttpClient okHttpClient;
    
    //单例
    private JsonRequest jsonRequest;
    
    private JsonRequest() {
    }
    
    //快速请求创建方法(POST请求)
    public static JsonRequest POST(Context context, String partUrl, Parameter parameter, ResponseListener listener) {
        try {
            return POST(context, partUrl, parameter.toParameterJson(), listener);
        } catch (Exception e) {
            if (DEBUGMODE) {
                Log.e(">>>", "-------------------------------------");
                Log.e(">>>", "创建请求失败:" + listener + " 不是正确的json格式参数");
                Log.e(">>>", "=====================================");
            }
            return null;
        }
    }
    
    public static JsonRequest POST(Context context, String partUrl, JSONObject jsonParameter, ResponseListener listener) {
        return POST(context, partUrl,  jsonParameter.toString(), listener);
    }
    
    public static JsonRequest POST(Context context, String partUrl, String jsonParameter, ResponseListener listener) {
        try {
            return POST(context, partUrl,null, jsonParameter, listener);
        } catch (Exception e) {
            if (DEBUGMODE) {
                Log.e(">>>", "-------------------------------------");
                Log.e(">>>", "创建请求失败:" + listener + " 不是正确的json格式参数");
                Log.e(">>>", "=====================================");
            }
            return null;
        }
    }
    
    public static JsonRequest POST(Context context, String partUrl, Parameter headers, String parameter, ResponseListener listener) {
        synchronized (JsonRequest.class) {
            JsonRequest jsonRequest = new JsonRequest();
            jsonRequest.context = context;
            jsonRequest.headers = headers;
            jsonRequest.listener = listener;
            jsonRequest.parameter = parameter;
            jsonRequest.jsonRequest = jsonRequest;
            jsonRequest.doRequest(partUrl, POST_REQUEST);
            return jsonRequest;
        }
    }
    
    private String postUrl;
    private boolean isSending;
    
    private void doRequest(final String url, int requestType) {
        try {
            if (parameter == null) parameter = "";
            
            if (SSLInAssetsFileName == null || SSLInAssetsFileName.isEmpty()) {
                okHttpClient = new OkHttpClient();
            } else {
                okHttpClient = getOkHttpClient(context, context.getAssets().open(SSLInAssetsFileName));
            }
            
            postUrl = url;
            
            if (!postUrl.startsWith("http")) {
                postUrl = serviceUrl + postUrl;
            }
            
            if (DEBUGMODE) {
                Log.i(">>>", "-------------------------------------");
                Log.i(">>>", "创建请求:" + postUrl);
                Log.i(">>>", "参数:" + parameter);
                Log.i(">>>", "请求已发送 ->");
            }
            
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), parameter);
            
            //创建请求
            baseokhttp3.Request request;
            baseokhttp3.Request.Builder builder = new baseokhttp3.Request.Builder();
            
            //请求类型处理
            builder.url(postUrl);
            builder.post(requestBody);
            
            //请求头处理
            if (overallHeader != null && !overallHeader.entrySet().isEmpty()) {
                for (Map.Entry<String, String> entry : overallHeader.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            if (headers != null && !headers.entrySet().isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            request = builder.build();
            
            final String finalPostUrl = postUrl;
            
            //检查超时状态
            checkTimeOut();
            isSending = true;
            
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    if (!isSending) return;
                    isSending = false;
                    if (DEBUGMODE) {
                        Log.e(">>>", "请求失败:" + finalPostUrl);
                        Log.e(">>>", "参数:" + parameter);
                        Log.e(">>>", "错误:" + e.toString());
                        Log.e(">>>", "=====================================");
                    }
                    //回到主线程处理
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (responseInterceptListener != null) {
                                    if (responseInterceptListener.onResponse(context, finalPostUrl, null, new NetworkErrorException())) {
                                        if (listener != null)
                                            listener.onResponse(null, new NetworkErrorException());
                                    }
                                } else {
                                    if (listener != null)
                                        listener.onResponse(null, new NetworkErrorException());
                                }
                            }
                        });
                    } else {
                        if (responseInterceptListener != null) {
                            if (responseInterceptListener.onResponse(context, finalPostUrl, null, new NetworkErrorException())) {
                                if (listener != null)
                                    listener.onResponse(null, new NetworkErrorException());
                            }
                        } else {
                            if (listener != null)
                                listener.onResponse(null, new NetworkErrorException());
                        }
                    }
                    
                }
                
                @Override
                public void onResponse(Call call, baseokhttp3.Response response) throws IOException {
                    if (!isSending) return;
                    isSending = false;
                    final String strResponse = response.body().string();
                    if (DEBUGMODE) {
                        Log.i(">>>", "请求成功:" + finalPostUrl);
                        Log.i(">>>", "参数:" + parameter);
                        Log.i(">>>", "返回内容:");
                        if (!JsonFormat.formatJson(strResponse)) {
                            Log.i(">>>", strResponse);
                        }
                        Log.i(">>>", "=====================================");
                    }
                    
                    //回到主线程处理
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (responseInterceptListener != null) {
                                    if (responseInterceptListener.onResponse(context, finalPostUrl, strResponse, null)) {
                                        if (listener != null)
                                            listener.onResponse(strResponse, null);
                                    }
                                } else {
                                    if (listener != null) listener.onResponse(strResponse, null);
                                }
                            }
                        });
                    } else {
                        if (responseInterceptListener != null) {
                            if (responseInterceptListener.onResponse(context, finalPostUrl, strResponse, null)) {
                                if (listener != null) listener.onResponse(strResponse, null);
                            }
                        } else {
                            if (listener != null) listener.onResponse(strResponse, null);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    private Timer timer;
    
    private void checkTimeOut() {
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isSending && listener != null) {
                    isSending = false;
                    Log.e(">>>", "请求超时 ×");
                    Log.e(">>>", "=====================================");
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onResponse(null, new TimeOutException());
                            }
                        });
                    } else {
                        listener.onResponse(null, new TimeOutException());
                    }
                }
            }
        }, TIME_OUT_DURATION * 1000);
    }
    
    public OkHttpClient getOkHttpClient(Context context, InputStream... certificates) {
        if (okHttpClient == null) {
            File sdcache = context.getExternalCacheDir();
            int cacheSize = 10 * 1024 * 1024;
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            if (DEBUGMODE)
                                Log.i("<<<", "hostnameVerifier: " + hostname);
                            if (httpsVerifyServiceUrl) {
                                if (serviceUrl.contains(hostname)) {
                                    return true;
                                } else {
                                    return false;
                                }
                            } else {
                                return true;
                            }
                        }
                    })
                    .cache(new Cache(sdcache.getAbsoluteFile(), cacheSize));
            if (certificates != null) {
                builder.sslSocketFactory(getSSLSocketFactory(certificates));
            }
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }
    
    private static SSLSocketFactory getSSLSocketFactory(InputStream... certificates) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                
                try {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e) {
                }
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Deprecated
    public static String getSSLInAssetsFileName() {
        return SSLInAssetsFileName;
    }
    
    @Deprecated
    public static void setSSLInAssetsFileName(String fileName) {
        SSLInAssetsFileName = fileName;
    }
    
    
    @Deprecated
    public static boolean isHttpsVerifyServiceUrl() {
        return httpsVerifyServiceUrl;
    }
    
    @Deprecated
    public static void setHttpsVerifyServiceUrl(boolean httpsVerifyServiceUrl) {
        HttpRequest.httpsVerifyServiceUrl = httpsVerifyServiceUrl;
    }
    
    @Deprecated
    public static void setResponseInterceptListener(ResponseInterceptListener responseInterceptListener) {
        HttpRequest.responseInterceptListener = responseInterceptListener;
    }
    
    //打印所有全局header
    public static void printAllOverallHeaders() {
        if (!DEBUGMODE) return;
        if (overallHeader != null && !overallHeader.entrySet().isEmpty()) {
            Log.i(">>>", "全局Header：");
            for (Map.Entry<String, String> entry : overallHeader.entrySet()) {
                Log.i(">>>", entry.getKey() + ":" + entry.getValue());
            }
        }
    }
    
    //打印所有header
    public void printAllHeaders() {
        if (!DEBUGMODE) return;
        if (overallHeader != null && !overallHeader.entrySet().isEmpty()) {
            Log.i(">>>", "全局Header：");
            for (Map.Entry<String, String> entry : overallHeader.entrySet()) {
                Log.i(">>>>>>", entry.getKey() + ":" + entry.getValue());
            }
        }
        if (headers != null && !headers.entrySet().isEmpty()) {
            Log.i(">>>", "局部Header：");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                Log.i(">>>>>>", entry.getKey() + ":" + entry.getValue());
            }
        }
    }
}
