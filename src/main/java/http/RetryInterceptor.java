package http;

import com.alibaba.fastjson.JSONObject;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.Optional;

public class RetryInterceptor implements Interceptor {
    protected final static Logger log = LoggerFactory.getLogger(RetryInterceptor.class);

    private static final int MAX_RETRY_COUNT = 3; // 最大重试次数
    private static final int RETRY_DELAY_MILLIS = 100; // 重试间隔时间

    @Override
    public Response intercept(Chain chain) {
        Request request = chain.request();
        Response response = null;
        int retryCount = 0;
        while (retryCount < MAX_RETRY_COUNT) {
            try {
                response = chain.proceed(request);
                if (200 == response.code() && ResponseCodeEq200(response)) {
                    log.info(request.url()+":success");
                    return response;
                } else {
                    Thread.sleep(RETRY_DELAY_MILLIS);
                    retryCount++;
                    if (retryCount != MAX_RETRY_COUNT)
                        response.close();// 非最后一次重试失败需要关闭response
                }
            }catch (Exception e){
                log.error("正在进行第"+Integer.valueOf(retryCount+1)+"次重试");
                retryCount++;
                log.warn(request.url()+"请求失败",e);
            }
        }
        JSONObject jsonObject = null;
        try {
            String responseBody = response.peekBody(Long.MAX_VALUE).string();
            jsonObject = JSONObject.parseObject(responseBody);
        } catch (Exception e) {

        }
        log.error("请求" + request.url() + "尝试全部失败" + jsonObject);
        log.error("Please check the log for details of request failure /logs/today/error.log");
        return response;
    }

    public boolean ResponseCodeEq200(Response response) {
        boolean flag = false; // 默认值为false
        try {
            String responseBody = response.peekBody(Long.MAX_VALUE).string();
            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            //如果响应值没有code字段则默认设置为200
            String code = Optional.ofNullable(jsonObject.getString("code")).orElse("200");
            flag = code.equals("200");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return flag;
        }
    }
}
