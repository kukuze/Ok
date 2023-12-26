package http.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yjz
 * @date 2023/12/4$ 15:43$
 * @description:
 */
public class OkResponseLog {
    private static final Logger log = LoggerFactory.getLogger(OkResponseLog.class);

    public static void logSuccess(String method, String url, String response, String params, String headers, String contentType,String config) {
        if(!log.isInfoEnabled()) {
            return;
        }
        JSONObject logJson = new JSONObject();
        logJson.put("Method",method);
        logJson.put("Url",url);
        logJson.put("Response",response);
        logJson.put("Params",params);
        logJson.put("Headers",headers);
        logJson.put("Content-Type",contentType);
        logJson.put("Config",config);
        log.info(JSON.toJSONString(logJson, SerializerFeature.PrettyFormat));

    }
    public static void logError(String method, String url, String response, String params, String headers, String contentType,String config) {
        if(!log.isErrorEnabled()) {
            return;
        }
        JSONObject logJson = new JSONObject();
        logJson.put("Method",method);
        logJson.put("Url",url);
        logJson.put("Response",response);
        logJson.put("Params",params);
        logJson.put("Headers",headers);
        logJson.put("Content-Type",contentType);
        logJson.put("Config",config);
        log.error(JSON.toJSONString(logJson, SerializerFeature.PrettyFormat));
    }


}
