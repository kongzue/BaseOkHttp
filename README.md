# BaseOkHttp
OkHttp部分逻辑很蛋疼，在打通的Volley的情况下，对OkHttp进行了统一外部接口的二次封装，使用方式和BaseVolley (https://github.com/kongzue/BaseVolley) 完全一致

<a href="https://github.com/kongzue/BaseOkHttp/">
<img src="https://img.shields.io/badge/BaseOkHttp-2.0.5-green.svg" alt="BaseOkHttp">
</a>
<a href="https://bintray.com/myzchh/maven/BaseOkHttp/2.0.5/link">
<img src="https://img.shields.io/badge/Maven-2.0.5-blue.svg" alt="Maven">
</a>
<a href="http://www.apache.org/licenses/LICENSE-2.0">
<img src="https://img.shields.io/badge/License-Apache%202.0-red.svg" alt="License">
</a>
<a href="http://www.kongzue.com">
<img src="https://img.shields.io/badge/Homepage-Kongzue.com-brightgreen.svg" alt="Homepage">
</a>

### Maven仓库或Gradle的引用方式
Maven仓库：
```
<dependency>
  <groupId>com.kongzue.baseokhttp</groupId>
  <artifactId>baseokhttp</artifactId>
  <version>2.0.5</version>
  <type>pom</type>
</dependency>
```
Gradle：
在dependencies{}中添加引用：
```
implementation 'com.kongzue.baseokhttp:baseokhttp:2.0.5'
implementation 'com.squareup.okhttp3:okhttp:3.3.1'
```

试用版可以前往 http://fir.im/BaseOkHttp 下载

### 前言
1) 相比OkHttp更大的灵活性，可选流水线式代码编写方式或模块化代码编写方式
2) 结束请求后自动回归主线程操作，不需要再做额外处理
3) 与我们的BaseVolley一致的请求方式标准，更换底层框架再也无需额外的代码
4) 提供统一返回监听器ResponseListener处理返回数据
5) 我们可能在加载网络数据前会调用一个例如 progressbarDialog 的加载进度对话框来表示正在加载数据，此时若将“请求成功”和“请求失败”单独放在两个回调函数中，会导致代码臃肿复杂，至少你必须在两个回调函数中都将 progressbarDialog.dismiss(); 掉，而我们使用统一返回监听器就可以避免代码臃肿的问题，更加简洁高效。

### 请注意
请求成功和错误的返回监听器为同一个新的监听器：ResponseListener，请在ResponseListener中直接判断Exception是否为空（null），若为空即请求成功。

提供额外方法setHeaders()添加请求头，提供额外方法setSSLInAssetsFileName()设置Https请求证书。

### 设置
从 2.0.3 版本起可通过以下属性开启全局打印请求日志信息：
```
HttpRequest.DEBUGMODE = true;
```

从 2.0.4 版本起可以通过以下代码设置全局请求服务器地址，其效果是会自动在所有请求地址前添加 serviceUrl 以减少重复代码量：
```
HttpRequest.serviceUrl = "http://www.xxx.com";
```
对于请求地址以 “http” 开头的，不进行添加 serviceUrl 的处理。

### 快速使用
```
HttpRequest.POST(me, "https://www.apiopen.top/femaleNameApi", new Parameter()
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
```

### 食用方法
```
//创建正在加载UI表示
ProgressbarDialog progressbarDialog = new ProgressbarDialog(this);
progressbarDialog.show();

//Http请求范例（me = Activity.this）
HttpRequest.getInstance(me)
        //自定义请求Header头部信息（选用）
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
                    public void onResponse(String response, Exception error) {
                        //关闭进度对话框
                        progressbarDialog.dismiss();

                        //处理返回数据逻辑
                        if (error == null) {
                            //请求成功处理
                        } else {
                            //请求失败处理
                            Toast.makeText(me, "网络错误，请重试", Toast.LENGTH_SHORT);
                        }
                    }
                });
```
POST请求可以使用HttpRequest.getInstance(context).postRequest(...);方法；

GET请求可以使用HttpRequest.getInstance(context).getRequest(...);方法进行。

Parameter是有序参数，方便某些情况下对参数进行加密和校验。

### HTTPS
1) 请将SSL证书文件放在assets目录中，例如“ssl.crt”；
2) 以附带SSL证书名的方式创建请求：
```
HttpRequest.getInstance(me,"ssl.crt")
...
```
即可使用Https请求方式。

### 其他
BaseVolley除了提供基础的 Get 以及 Post 请求外，还提供了图片下载工具和多文件上传工具，具体可以参考 MultiFileRequest 类，使用方法亦很简单：
```
List<File> files = new ArrayList<>();
files.add(new File(xxx1));
files.add(new File(xxx2));

//上传范例（me = Activity.this）
multiFileRequest.getInstance(me).doPost("http://www.xxx.com/test", files, new ResponseListener() {
    @Override
    public void onResponse(String response, Exception error) {
        progressbarDialog.dismiss();
        if (error == null) {
            //请求成功处理
        } else {
            //请求失败处理
            Toast.makeText(me, "网络错误，请重试", Toast.LENGTH_SHORT);
        }
    }
});
```

## 开源协议
```
   Copyright Kongzue BaseOkHttp

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```

## 更新日志：
v2.0.5:
- 对于请求地址中已经包含“http”开头的地址不再主动添加 HttpRequest.serviceUrl ，以便于可能出现的突发外站请求；

v2.0.4:
- 提供参数“HttpRequest.serviceUrl = (String)”以设置是否设置全局服务器地址，其效果是会自动在所有请求地址前添加 serviceUrl 以减少重复代码量；

v2.0.3:
- 提供参数“HttpRequest.DEBUGMODE = (boolean)”以设置是否全局打印测试数据；

v2.0.2:
- 提供快速使用方式；
- 全局返回参数改为String；

v2.0.1:
- 修复bug；

v2.0.0:
- 修复bug & 封装；
