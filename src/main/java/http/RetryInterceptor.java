package http;

import com.alibaba.fastjson.JSONObject;
import http.config.OkConfigInterface;
import http.enums.ResponseFormat;
import http.utils.OkResponseLog;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

import static http.utils.ParseResponse.*;

public class RetryInterceptor implements Interceptor {

    private int MAX_RETRY_COUNT; // 最大重试次数
    private long RETRY_DELAY_MILLIS; // 重试间隔时间
    private Integer SUCCESS_CODES; //什么响应值代表请求成功;
    private String CODE_FIELD;//响应值的状态码字段
    private String DATA_FiELD;//响应值的数据字段
    private String RESPONSE_FORMAT;//预期格式json,html
    private String CONFIG_STRING;//仅用作日志

    public RetryInterceptor(OkConfigInterface okConfigInterface) {
        MAX_RETRY_COUNT=okConfigInterface.getRetryStrategy().getAttempts();
        RETRY_DELAY_MILLIS=okConfigInterface.getRetryStrategy().getIntervalMillis();
        SUCCESS_CODES=okConfigInterface.getSuccessCode().getCode();
        CODE_FIELD=okConfigInterface.getResponseCodeField().getFieldName();
        DATA_FiELD=okConfigInterface.getResponseDataField().getFieldName();
        RESPONSE_FORMAT = okConfigInterface.getResponseFormat().getTypeName();
        CONFIG_STRING=okConfigInterface.getClientKey();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        JSONObject jsonObject;
        int retryCount = 0;
        while (retryCount < MAX_RETRY_COUNT) {
            try {
                response = chain.proceed(request);
                String responseString = response.peekBody(Long.MAX_VALUE).string();
                if(RESPONSE_FORMAT.equals(ResponseFormat.JSON.getTypeName())){
                    jsonObject=JSONObject.parseObject(responseString);
                    if (jsonObject.getInteger(CODE_FIELD).equals(SUCCESS_CODES)) {
                        OkResponseLog.logSuccess(request.method(), request.url().toString(), jsonObject.toJSONString(), getParamsInfo(request.body()), getHeadersInfo(request.headers()), getContentType(request),CONFIG_STRING);
                        return response;
                    } else {
                        //处理有响应但结果不符合。
                        Thread.sleep(RETRY_DELAY_MILLIS);
                        retryCount++;
                        if (retryCount != MAX_RETRY_COUNT) {
                            response.close();// 非最后一次重试失败需要关闭response
                        }else{
                            //最后一次返回去结果
                            OkResponseLog.logError(request.method(), request.url().toString(), jsonObject.toJSONString(), getParamsInfo(request.body()), getHeadersInfo(request.headers()), getContentType(request),CONFIG_STRING);
                            return response;
                        }
                    }
                }else {
                    OkResponseLog.logSuccess(request.method(), request.url().toString(), responseString, getParamsInfo(request.body()), getHeadersInfo(request.headers()), getContentType(request),CONFIG_STRING);
                    return response;
                }
            } catch (Exception e) {
                //当无响应会来到这
                retryCount++;
            }
        }
        OkResponseLog.logError(request.method(), request.url().toString(), "无响应或 JSON与HTML混用", getParamsInfo(request.body()), getHeadersInfo(request.headers()), getContentType(request),CONFIG_STRING);
        if (response==null){
            //当无响应时，中断这次请求，如果返回null，则会导致Ok框架爆空指针异常
            throw new IOException();
        }
        return response;
    }



}