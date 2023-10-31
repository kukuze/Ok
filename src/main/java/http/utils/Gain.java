package http.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author yjz
 * @date 2023/6/9$ 14:56$
 * @description:
 */
public class Gain {
    public static JSONObject getO(String res){
        return JSONObject.parseObject(res).getJSONObject("data");
    }

    public static JSONArray getA(String res){
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

}
