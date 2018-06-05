package com.kongzue.baseokhttp.listener;

import org.json.JSONObject;

/**
 * Created by myzcx on 2017/12/27.
 */

public interface ResponseListener {
    void onResponse(String response, Exception error);
}
