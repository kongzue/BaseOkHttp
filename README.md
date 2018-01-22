# BaseOkHttp
OkHttp部分逻辑很蛋疼，在打通的Volley的情况下，对OkHttp进行了统一外部接口的二次封装，使用方式和BaseVolley (https://github.com/kongzue/BaseVolley) 完全一致

### 版本
1.0

请求成功和错误的返回监听器为同一个新的监听器：ResponseListener，请在ResponseListener中直接判断Exception是否为空（null），若为空即请求成功。
提供额外方法setHeaders()添加请求头，提供额外方法setSSLInAssetsFileName()设置Https请求证书。

### 请注意
1) 本封装基于：compile 'com.squareup.okhttp3:okhttp:3.3.1'
2) 目录中的“CORE”为核心文件，要查看项目源代码请进入该目录即可，本目录下其他文件为演示项目工程文件。

### 原因
1) 相比OkHttp更大的灵活性，可选流水线式代码编写方式或模块化代码编写方式
2) 结束请求后自动回归主线程操作，不需要再做额外处理
3) 与我们的BaseVolley一致的请求方式标准，更换底层框架再也无需额外的代码
4) 提供统一返回监听器ResponseListener处理返回数据
5) 我们可能在加载网络数据前会调用一个例如 progressbarDialog 的加载进度对话框来表示正在加载数据，此时若将“请求成功”和“请求失败”单独放在两个回调函数中，会导致代码臃肿复杂，至少你必须在两个回调函数中都将 progressbarDialog.dismiss(); 掉，而我们使用统一返回监听器就可以避免代码臃肿的问题，更加简洁高效。

### 食用方法
```
//创建正在加载UI表示
ProgressbarDialog progressbarDialog = new ProgressbarDialog(this);
progressbarDialog.show();

//Http请求范例
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
                    public void onResponse(JSONObject main, Exception error) {
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
BaseVolley除了提供基础的 Get 以及 Post 请求外，还提供了图片下载工具和多文件上传工具，具体可以参考 ImageRequest 类和 MultiFileRequest 类
