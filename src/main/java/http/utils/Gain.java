package http.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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

}
