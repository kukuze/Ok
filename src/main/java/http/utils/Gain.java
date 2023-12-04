package http.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import http.config.OkConfigInterface;

import java.util.List;

/**
 * @author yjz
 * @date 2023/6/9$ 14:56$
 * @description:
 */
public class Gain {
    public static JSONObject getObject(String res){
        return JSONObject.parseObject(res).getJSONObject("data");
    }

    public static JSONArray getArray(String res){
        return JSONObject.parseObject(res).getJSONArray("data");
    }

    public static <T> T getEntity(String res, Class<T> clazz) {
        JSONObject jsonObject = JSON.parseObject(res).getJSONObject("data");
        return JSON.toJavaObject(jsonObject, clazz);
    }

    public static <T> List<T> getList(String res, Class<T> clazz) {
        JSONArray jsonArray = JSON.parseObject(res).getJSONArray("data");
        return JSON.parseArray(jsonArray.toJSONString(), clazz);
    }
    public static JSONObject getObject(String res, OkConfigInterface okConfigInterface){
        return JSONObject.parseObject(res).getJSONObject(okConfigInterface.getResponseDataField().getFieldName());
    }

    public static JSONArray getArray(String res, OkConfigInterface okConfigInterface){
        return JSONObject.parseObject(res).getJSONArray(okConfigInterface.getResponseDataField().getFieldName());
    }
    public static <T> T getEntity(String res, Class<T> clazz, OkConfigInterface okConfigInterface) {
        JSONObject jsonObject = JSON.parseObject(res).getJSONObject(okConfigInterface.getResponseDataField().getFieldName());
        return JSON.toJavaObject(jsonObject, clazz);
    }

    public static <T> List<T> getList(String res, Class<T> clazz, OkConfigInterface okConfigInterface) {
        JSONArray jsonArray = JSON.parseObject(res).getJSONArray(okConfigInterface.getResponseDataField().getFieldName());
        return JSON.parseArray(jsonArray.toJSONString(), clazz);
    }
}
