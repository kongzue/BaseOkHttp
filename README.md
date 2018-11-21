# BaseOkHttp
OkHttp部分逻辑很蛋疼，在打通的Volley的情况下，对OkHttp进行了统一外部接口的二次封装，使用方式和BaseVolley (https://github.com/kongzue/BaseVolley) 完全一致

<a href="https://github.com/kongzue/BaseOkHttp/">
<img src="https://img.shields.io/badge/BaseOkHttp-2.1.5-green.svg" alt="BaseOkHttp">
</a>
<a href="https://bintray.com/myzchh/maven/BaseOkHttp/2.1.5/link">
<img src="https://img.shields.io/badge/Maven-2.1.5-blue.svg" alt="Maven">
</a>
<a href="http://www.apache.org/licenses/LICENSE-2.0">
<img src="https://img.shields.io/badge/License-Apache%202.0-red.svg" alt="License">
</a>
<a href="http://www.kongzue.com">
<img src="https://img.shields.io/badge/Homepage-Kongzue.com-brightgreen.svg" alt="Homepage">
</a>

## Maven仓库或Gradle的引用方式
Maven仓库：
```
<dependency>
  <groupId>com.kongzue.baseokhttp</groupId>
  <artifactId>baseokhttp</artifactId>
  <version>2.1.5</version>
  <type>pom</type>
</dependency>
```
Gradle：
在dependencies{}中添加引用：
```
implementation 'com.kongzue.baseokhttp:baseokhttp:2.1.5'
```

试用版可以前往 http://fir.im/BaseOkHttp 下载

## 前言
1) 相比OkHttp更大的灵活性，可选流水线式代码编写方式或模块化代码编写方式
2) 结束请求后自动回归主线程操作，不需要再做额外处理（注：从 2.1.0 版本起是否自动返回主线程由第一个参数决定，具体请参照更新日志）
3) 与我们的BaseVolley一致的请求方式标准，更换底层框架再也无需额外的代码
4) 提供统一返回监听器ResponseListener处理返回数据
5) 我们可能在加载网络数据前会调用一个例如 progressbarDialog 的加载进度对话框来表示正在加载数据，此时若将“请求成功”和“请求失败”单独放在两个回调函数中，会导致代码臃肿复杂，至少你必须在两个回调函数中都将 progressbarDialog.dismiss(); 掉，而我们使用统一返回监听器就可以避免代码臃肿的问题，更加简洁高效。
6) Https私有证书设置方式简单化，使用 setSSLInAssetsFileName 即可完成所有工作。

## 使用前注意
1) BaseOkHttp 在请求结束后，无论成功还是错误都会返回同一个新的监听器：ResponseListener，您可以在 ResponseListener 中直接判断参数 error(类型为 Exception) 是否为空指针，若 error == null 即请求成功。
2) 提供额外方法setSSLInAssetsFileName()设置Https请求证书。

## 一般请求
1) 快速使用：

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
                    Toast.makeText(me, "请求失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
```

2) 具体食用方法
```
//创建正在加载UI表示
ProgressbarDialog progressbarDialog = new ProgressbarDialog(this);
progressbarDialog.show();

//Http请求范例
HttpRequest.POST(this, "https://www.apiopen.top/femaleNameApi",
                 //自定义请求Header头部信息（选用，此参数可以为 null，或者直接忽略该参数也可）
                 new Parameter()
                         .add("Charset", "UTF-8")
                         .add("Content-Type", "application/json")
                         .add("Accept-Encoding", "gzip,deflate"),
                 //请求参数
                 new Parameter()
                         .add("key1", "value1")
                         .add("key2", "value3")
                         .add("key4", "value4"),
                 //请求回调
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
                 }
);
```

Parameter是有序参数，方便某些情况下对参数进行加密和校验。

## 关于多线程

BaseOkHttp 在请求时会处于异步线程，若您传入的上下文索引 context 为 Activity 类型，BaseOkHttp 会在请求结束后**自动**返回主线程。

若您传入的 context 为其他类型，请求结束后会在异步线程返回，如有特殊需要请自行处理。

## 增强型日志

BaseOkHttp 从 2.1.4 版本起支持增强型日志，参考如图：

![BaseOkHttp Logs](https://github.com/kongzue/Res/raw/master/app/src/main/res/mipmap-xxxhdpi/img_okhttp_logs.png)

1. 要开启请先设置：HttpRequest.DEBUGMODE 为 true；

在您使用 BaseOkHttp 时可以在 Logcat 的筛选中使用字符 “>>>” 对日志进行筛选（Logcat日志界面上方右侧的搜索输入框）。

您可以在 Android Studio 的 File -> Settings 的 Editor -> Color Scheme -> Android Logcat 中调整各类型的 log 颜色，我们推荐如下图方式设置颜色：

![Kongzue's log settings](https://github.com/kongzue/Res/raw/master/app/src/main/res/mipmap-xxxhdpi/baseframework_logsettings.png)

2. 对json进行自动格式化

使用输出日志内容是 json 字符串时，会自动格式化输出，方便查看。

## HTTPS 支持
1) 请将SSL证书文件放在assets目录中，例如“ssl.crt”；
2) 以附带SSL证书名的方式创建请求：
```
HttpRequest.setSSLInAssetsFileName("ssl.crt")
...
```
即可使用Https请求方式。

另外，可使用 HttpRequest.httpsVerifyServiceUrl=(boolean) 设置是否校验请求主机地址与设置的 HttpRequest.serviceUrl 一致；

## 多文件表单上传

1) 快速使用
```
//要上传文件，先创建一个文件的List，稍后作为.doPost(...)方法的参数发送
List<File> files = new ArrayList<>();
files.add(new File(xxx1));
files.add(new File(xxx2));
//开始发送请求
MultiFileRequest.POST(me, "url", files, new ResponseListener() {
    @Override
    public void onResponse(String response, Exception error) {
        if (error == null) {
            resultHttp.setText(response);
            Log.i(">>>", "onResponse: " + response);
        } else {
            resultHttp.setText("");
            Toast.makeText(me, "请求失败", Toast.LENGTH_SHORT);
        }
    }
});
```

额外的，可根据需求选用同名的方法：
```
//需要额外的参数
MultiFileRequest.POST(Activity a, String partUrl, Parameter parameter, List<File> files, ResponseListener listener)

//需要额外的Headers头
MultiFileRequest.POST(Activity a, String partUrl, Parameter headers, Parameter parameter, List<File> files, ResponseListener listener);

//默认情况下，MultiFileRequest发送的文件类型为 image/png ，需要额外的修改为上传文件类型：
MediaType MEDIA_TYPE = MediaType.parse("image/png");
MultiFileRequest.POST(Activity a, String partUrl, Parameter headers, Parameter parameter, List<File> files, ResponseListener listener, MediaType MEDIA_TYPE);
```

2) ~~多图片表单上传（已过时）~~

BaseOkHttp 除了提供基础的 Get 以及 Post 请求外，还提供了图片下载工具和多文件上传工具，具体可以参考 MultiFileRequest 类，使用方法亦很简单：
```
//要上传文件，先创建一个文件的List，稍后作为.doPost(...)方法的参数发送
List<File> files = new ArrayList<>();
files.add(new File(xxx1));
files.add(new File(xxx2));
MultiFileRequest multiFileRequest = MultiFileRequest.getInstance(me);
//需要额外携带参数的话，请使用setParameter(...)设置，以下是范例
multiFileRequest.setParameter(new Parameter()
                                      .add("key1", "value1")
                                      .add("key2", "value2")
                                      .add("key3", "value3")
                                      .add("key4", "value4")
);
//需要添加请求头的话，setHeaders(...)设置，以下是范例
multiFileRequest.setHeaders(new Parameter()
                                    .add("header1", "value1")
                                    .add("header2", "value2")
                                    .add("header3", "value3")
                                    .add("header4", "value4")
);
//上传范例（me = Activity.this）
multiFileRequest.getInstance(me).doPost("http://www.xxx.com/test", files, new ResponseListener() {
    @Override
    public void onResponse(String response, Exception error) {
        if (error == null) {
            //请求成功处理
        } else {
            //请求失败处理
            Toast.makeText(me, "网络错误，请重试", Toast.LENGTH_SHORT);
        }
    }
});
```

## 全局 Header 请求头
使用如下代码设置全局 Header 请求头：
```
HttpRequest.overallHeader = new Parameter()
        .add("Charset", "UTF-8")
        .add("Content-Type", "application/json")
        .add("Accept-Encoding", "gzip,deflate")
;
```

## 全局返回拦截器
使用如下代码可以设置全局返回数据监听拦截器，return true 可返回请求继续处理，return false 即拦截掉不会继续返回原请求进行处理；
```
HttpRequest.responseInterceptListener = new ResponseInterceptListener() {
    @Override
    public boolean onResponse(String url, String response, Exception error) {
        if (error != null) {
            return true;
        } else {
            Log.i("!!!", "onResponse: " + response);
            return true;
        }
    }
};
```

## 额外设置
从 2.0.3 版本起可通过以下属性开启全局打印请求日志信息：
```
HttpRequest.DEBUGMODE = true;
```

从 2.0.4 版本起可以通过以下代码设置全局请求服务器地址，其效果是会自动在所有请求地址前添加 serviceUrl 以减少重复代码量：
```
HttpRequest.serviceUrl = "http://www.xxx.com";
```
对于请求地址以 “http” 开头的，不进行添加 serviceUrl 的处理。

从 2.1.4 版本起，可设置超时时间（默认10，单位：秒）：
```
HttpRequest.TIME_OUT_DURATION = 10;
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

本项目中使用的网络请求底层框架为square.okHttp3(https://github.com/square/okhttp )，感谢其为开源做出的贡献：

相关协议如下：
```
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
v2.1.5：
- 全局拦截返回器设置方式允许直接设置，不再推荐使用set方法设置；
- 新增全局 header 添加方式；
- 新增全局 header 日志打印、所有 header的日志打印方式；

v2.1.4：
- 修复了一些 bug；
- HttpRequest 新增带 headers 参数的 POST 和 GET 方法；
- 全新的 DEBUGMODE 日志；

v2.1.3：
- 修复了一些bug；

v2.1.2：
- MultiFileRequest 多文件上传方式更新（具体请参照文档）；
- Context 的存储方式修改；

v2.1.1：
- 修复bug；

v2.1.0：
- 完整移植集成 okHttp 源代码及 okio，以解决可能和其他框架产生的 okHttp 版本冲突问题；

唯一需要注意的是所有引用本库中 okio 包的现在请引用 baseokio 包，引用 okhttp3 包的请改为引用 baseokhttp3；

- HttpRequest.POST 支持 Context 类型，在传入 Context 类型时 Response 不会返回主线程操作；

v2.0.9：
- 可使用 HttpRequest.setSSLInAssetsFileName(String assetsFileName) 设置 Https 请求 SSL 证书文件，可使用 HttpRequest.httpsVerifyServiceUrl=(boolean) 设置是否校验请求主机地址与设置的 HttpRequest.serviceUrl 一致；
- 修复bug；

v2.0.8：
- 新增全局拦截器 ResponseInterceptListener，可进行全局请求拦截操作
- 现在可以通过 setSSLInAssetsFileName(String fileName) 直接设置 Https 证书目录；
- 现在可以通过 HttpRequest.httpsVerifyServiceUrl 决定是否在 Https 证书请求时验证 Hostname；
- 修复多请求并发时可能出现的日志混乱问题；

v2.0.7：
- 修复bug；

v2.0.6:
- 集成 square.okHttp3，无需额外引用okhttp3包；

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
