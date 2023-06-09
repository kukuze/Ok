package http.utils;

import com.alibaba.fastjson.JSONObject;

/**
 * @author yjz
 * @date 2023/6/9$ 14:23$
 * @description:
 */
public class Valid {
    public static boolean vJsonArray(String res){
        if(res==null) {
            return false;
        }
        JSONObject jsonObject = JSONObject.parseObject(res);
        if(jsonObject.getJSONArray("data")==null||jsonObject.getJSONArray("data").size()==0) {
            return false;
        }
        return true;
    }
    public static boolean vJsonObject(String res){
        if(res==null) {
            return false;
        }
        JSONObject jsonObject = JSONObject.parseObject(res);
        if(jsonObject.getJSONObject("data")==null) {
            return false;
        }
        return true;
    }

}
