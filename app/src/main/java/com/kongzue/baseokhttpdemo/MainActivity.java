package com.kongzue.baseokhttpdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kongzue.baseokhttp.HttpRequest;
import com.kongzue.baseokhttp.listener.ResponseInterceptListener;
import com.kongzue.baseokhttp.listener.ResponseListener;
import com.kongzue.baseokhttp.util.Parameter;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    
    public MainActivity me;
    
    private Button btnHttp;
    private TextView resultHttp;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btnHttp = findViewById(R.id.btn_http);
        resultHttp = findViewById(R.id.result_http);
        
        me = this;
        
        btnHttp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultHttp.setText("正在请求...");
                HttpRequest.serviceUrl = "https://www.apiopen.top";
                HttpRequest.setResponseInterceptListener(new ResponseInterceptListener() {
                    @Override
                    public boolean onResponse(String url, String response, Exception error) {
                        if (error!=null){
                            return true;
                        }else{
                            Log.i("!!!", "onResponse: "+response);
                            return true;
                        }
                    }
                });
                HttpRequest.POST(me, "/femaleNameApi", new Parameter()
                                         .add("page", "1")
                        , new ResponseListener() {
                            @Override
                            public void onResponse(String response, Exception error) {
                                if (error == null) {
                                    resultHttp.setText(response);
                                } else {
                                    resultHttp.setText("");
                                    Toast.makeText(me, "请求失败", Toast.LENGTH_SHORT);
                                }
                            }
                        });
            }
        });
    }
}
