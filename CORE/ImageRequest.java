
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 图片下载工具
 * Created by myzcx on 2018/1/14.
 * ver:1.0
 */

public class ImageRequest {

    private static Activity context;
    private static ImageCallBack imageCallBack;

    //单例
    private static ImageRequest imageRequest;

    private ImageRequest() {
    }

    public static ImageRequest getInstance(Activity c) {
        if (imageRequest == null) {
            synchronized (ImageRequest.class) {
                if (imageRequest == null) {
                    imageRequest = new ImageRequest();
                    context = c;
                }
            }
        }
        return imageRequest;
    }

    public ImageRequest doPost(String url, ImageCallBack callBack) {

        this.imageCallBack = callBack;

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageCallBack.onResponse(e, null);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //得到从网上获取资源，转换成我们想要的类型
                byte[] Picture_bt = response.body().bytes();
                //使用BitmapFactory工厂，把字节数组转化为bitmap
                try {
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(Picture_bt, 0, Picture_bt.length);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageCallBack.onResponse(null, bitmap);
                        }
                    });
                } catch (final Exception e) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageCallBack.onResponse(e, null);
                        }
                    });
                }
            }
        });
        return imageRequest;
    }

    public interface ImageCallBack {
        void onResponse(Exception error, Bitmap bitmap);
    }
}

