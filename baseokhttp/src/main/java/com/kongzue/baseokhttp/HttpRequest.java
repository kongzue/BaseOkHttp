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
import baseokhttp3.OkHttpClient;
import baseokhttp3.RequestBody;

/**
 * BaseOkHttp
 * Created by myzcx on 2017/12/27.
 * ver:2.0
 */

public class HttpRequest {
    
    //是否开启调试模式
    public static boolean DEBUGMODE = false;
    
    //超时时长（单位：秒）
    public static int TIME_OUT_DURATION = 10;
    
    //默认服务器地址
    public static String serviceUrl = "";
    
    private static int GET_REQUEST = 1;
    private static int POST_REQUEST = 0;
    
    //Https请求需要传入Assets目录下的证书文件名称
    private static String SSLInAssetsFileName;
    
    //Https请求是否需要Hostname验证，请保证serviceUrl中即Hostname地址
    private static boolean httpsVerifyServiceUrl = false;
    
    //全局拦截器
    public static ResponseInterceptListener responseInterceptListener;
    
    //全局header
    public static Parameter overallHeader;
    
    private Parameter headers;
    
    private Context context;
    private ResponseListener listener;
    private OkHttpClient okHttpClient;
    
    //单例
    private HttpRequest httpRequest;
    
    private HttpRequest() {
    }
    
    //默认请求创建方法（不再推荐使用）
    @Deprecated
    public static HttpRequest getInstance(Context context) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = context;
            httpRequest.httpRequest = httpRequest;
            return httpRequest;
        }
    }
    
    //快速请求创建方法(POST请求)
    public static HttpRequest POST(Context context, String partUrl, Parameter parameter, ResponseListener listener) {
        return POST(context, partUrl, null, parameter, listener);
    }
    
    public static HttpRequest POST(Context context, String partUrl, Parameter headers, Parameter parameter, ResponseListener listener) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = context;
            httpRequest.headers = headers;
            httpRequest.listener = listener;
            httpRequest.httpRequest = httpRequest;
            httpRequest.doRequest(partUrl, parameter, POST_REQUEST);
            return httpRequest;
        }
    }
    
    //快速请求创建方法(GET请求)
    public static HttpRequest GET(Context context, String partUrl, Parameter parameter, ResponseListener listener) {
        return GET(context, partUrl, null, parameter, listener);
    }
    
    public static HttpRequest GET(Context context, String partUrl, Parameter headers, Parameter parameter, ResponseListener listener) {
        synchronized (HttpRequest.class) {
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.context = context;
            httpRequest.headers = headers;
            httpRequest.listener = listener;
            httpRequest.httpRequest = httpRequest;
            httpRequest.doRequest(partUrl, parameter, GET_REQUEST);
            return httpRequest;
        }
    }
    
    public Parameter getHeaders() {
        return headers;
    }
    
    public HttpRequest setHeaders(Parameter headers) {
        this.headers = headers;
        return this;
    }
    
    private String postUrl;
    private boolean isSending;
    
    private void doRequest(final String url, final Parameter parameter, int requestType) {
        try {
            
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
                Log.i(">>>", "参数:" + parameter.toParameterString());
                Log.i(">>>", "请求已发送 ->");
            }
            
            RequestBody requestBody = parameter.toOkHttpParameter();
            
            //创建请求
            baseokhttp3.Request request;
            baseokhttp3.Request.Builder builder = new baseokhttp3.Request.Builder();
            
            //请求类型处理
            if (requestType == GET_REQUEST) {
                builder.url(postUrl + "?" + parameter.toParameterString());
            } else {
                builder.url(postUrl);
                builder.post(requestBody);
            }
            
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
                        Log.e(">>>", "参数:" + parameter.toParameterString());
                        Log.e(">>>", "错误:" + e.toString());
                        Log.e(">>>", "=====================================");
                    }
                    //回到主线程处理
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (responseInterceptListener != null) {
                                    if (responseInterceptListener.onResponse(finalPostUrl, null, new NetworkErrorException())) {
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
                            if (responseInterceptListener.onResponse(finalPostUrl, null, new NetworkErrorException())) {
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
                        Log.i(">>>", "参数:" + parameter.toParameterString());
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
                                    if (responseInterceptListener.onResponse(finalPostUrl, strResponse, null)) {
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
                            if (responseInterceptListener.onResponse(finalPostUrl, strResponse, null)) {
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
    
    public static String getSSLInAssetsFileName() {
        return SSLInAssetsFileName;
    }
    
    public static void setSSLInAssetsFileName(String fileName) {
        SSLInAssetsFileName = fileName;
    }
    
    public static boolean isHttpsVerifyServiceUrl() {
        return httpsVerifyServiceUrl;
    }
    
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
                Log.i(">>>", entry.getKey() + ":" + entry.getValue());
            }
        }
        if (headers != null && !headers.entrySet().isEmpty()) {
            Log.i(">>>", "局部Header：");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                Log.i(">>>", entry.getKey() + ":" + entry.getValue());
            }
        }
    }
}