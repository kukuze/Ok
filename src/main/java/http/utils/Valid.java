package http.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import http.config.OkConfigInterface;

/**
 * @author yjz
 * @date 2023/6/9$ 14:23$
 * @description:
 */
public class Valid {
    // Verify if a string can be parsed into a JSONObject with a non-empty "data" JSONArray
    public static boolean vJsonArray(String res){
        if(res == null || res.isEmpty()) {
            return false;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(res);
            JSONArray dataArray = jsonObject.getJSONArray("data");
            return dataArray != null && !dataArray.isEmpty();
        } catch (JSONException ex) {
            return false;
        }
    }

    // Verify if a string can be parsed into a JSONObject with a "data" JSONObject
    public static boolean vJsonObject(String res){
        if(res == null || res.isEmpty()) {
            return false;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(res);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            return dataObject != null;
        } catch (JSONException ex) {
            return false;
        }
    }

    // Similar to vJsonArray but with a dynamic field name from OkConfigInterface
    public static boolean vJsonArray(String res, OkConfigInterface okConfigInterface){
        if(res == null || res.isEmpty()) {
            return false;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(res);
            JSONArray dataArray = jsonObject.getJSONArray(okConfigInterface.getResponseDataField().getFieldName());
            return dataArray != null && !dataArray.isEmpty();
        } catch (JSONException ex) {
            return false;
        }
    }

    // Similar to vJsonObject but with a dynamic field name from OkConfigInterface
    public static boolean vJsonObject(String res, OkConfigInterface okConfigInterface){
        if(res == null || res.isEmpty()) {
            return false;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(res);
            JSONObject dataObject = jsonObject.getJSONObject(okConfigInterface.getResponseDataField().getFieldName());
            return dataObject != null;
        } catch (JSONException ex) {
            return false;
        }
    }


}
