package com.kongzue.baseokhttp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.kongzue.baseokhttp.listener.ResponseListener;
import com.kongzue.baseokhttp.request.HttpRequest;
import com.kongzue.baseokhttp.util.Parameter;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private MainActivity me = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Http请求范例
        HttpRequest.getInstance(me)
                //自定义请求Header头部信息
                .setHeaders(new Parameter()
                        .add("Charset", "UTF-8")
                        .add("Content-Type", "application/json")
                        .add("Accept-Encoding", "gzip,deflate")
                )
                //发送请求
                .postRequest("http://www.xxx.com/test", new Parameter()
                                .add("key1", "value1")
                                .add("key2", "value3")
                                .add("key4", "value4"),
                        new ResponseListener() {
                            @Override
                            public void onResponse(JSONObject main, Exception error) {
                                if (error == null) {
                                    //请求成功处理
                                } else {
                                    //请求失败处理
                                    Toast.makeText(me, "网络错误，请重试", Toast.LENGTH_SHORT);
                                }
                            }
                        });

        //Https请求范例：
//        HttpRequest.getInstance(me, "ssl.crt")
//                //自定义请求Header头部信息
//                .setHeaders(new Parameter()
//                        .add("Charset", "UTF-8")
//                        .add("Content-Type", "application/json")
//                        .add("Accept-Encoding", "gzip,deflate")
//                )
//                //发送请求
//                .postRequest("http://www.xxx.com/test", new Parameter()
//                                .add("key1", "value1")
//                                .add("key2", "value3")
//                                .add("key4", "value4"),
//                        new ResponseListener() {
//                            @Override
//                            public void onResponse(JSONObject main, Exception error) {
//                                if (error == null) {
//                                    //请求成功处理
//                                } else {
//                                    //请求失败处理
//                                    Toast.makeText(me, "网络错误，请重试", Toast.LENGTH_SHORT);
//                                }
//                            }
//                        });

    }
}
