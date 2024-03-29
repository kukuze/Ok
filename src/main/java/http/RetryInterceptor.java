package http;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import http.config.OkConfigInterface;
import http.enums.ResponseFormat;
import http.utils.OkResponseLog;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;

import static http.utils.ParseResponse.*;

public class RetryInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(RetryInterceptor.class);

    private int MAX_RETRY_COUNT; // 最大重试次数
    private long RETRY_DELAY_MILLIS; // 重试间隔时间
    private Integer SUCCESS_CODES; //什么响应值代表请求成功;
    private String CODE_FIELD;//响应值的状态码字段
    private String DATA_FiELD;//响应值的数据字段
    private String RESPONSE_FORMAT;//预期格式json,html
    private String CONFIG_STRING;//仅用作日志


    public RetryInterceptor(OkConfigInterface okConfigInterface) {
        MAX_RETRY_COUNT = okConfigInterface.getRetryStrategy().getAttempts();
        RETRY_DELAY_MILLIS = okConfigInterface.getRetryStrategy().getIntervalMillis();
        SUCCESS_CODES = okConfigInterface.getSuccessCode().getCode();
        CODE_FIELD = okConfigInterface.getResponseCodeField().getFieldName();
        DATA_FiELD = okConfigInterface.getResponseDataField().getFieldName();
        RESPONSE_FORMAT = okConfigInterface.getResponseFormat().getTypeName();
        CONFIG_STRING = okConfigInterface.getClientKey();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        boolean isNoResponse = false;
        Request request = chain.request();
        Response response = null;
        String responseString = null;
        JSONObject jsonObject=null;
        int retryCount = 0;
        boolean isJson = false;
        while (retryCount < MAX_RETRY_COUNT) {
            try {
                response = chain.proceed(request);
                responseString = response.peekBody(Long.MAX_VALUE).string();
                try {
                    jsonObject = JSONObject.parseObject(responseString);
                    isJson = true;
                } catch (JSONException ignored) {
                }
                if (RESPONSE_FORMAT.equals(ResponseFormat.JSON.getTypeName())) {
                    if (isJson&&SUCCESS_CODES.equals(jsonObject.getInteger(CODE_FIELD))) {
                        OkResponseLog.logSuccess(request.method(), request.url().toString(), responseString, getParamsInfo(request.body()), getHeadersInfo(request.headers()), getContentType(request), CONFIG_STRING);
                        return response;
                    } else if(isJson){
                        if (retryCount == MAX_RETRY_COUNT-1) {
                            OkResponseLog.logError(request.method(), request.url().toString(), "响应码与预期不符"+responseString, getParamsInfo(request.body()), getHeadersInfo(request.headers()), getContentType(request), CONFIG_STRING);
                            return response;
                        }
                    }else{
                        if (retryCount == MAX_RETRY_COUNT-1) {
                            OkResponseLog.logError(request.method(), request.url().toString(), "响应格式与预期不符"+responseString, getParamsInfo(request.body()), getHeadersInfo(request.headers()), getContentType(request), CONFIG_STRING);
                            return response;
                        }
                    }
                    Thread.sleep(RETRY_DELAY_MILLIS);
                    response.close();
                } else {
                    // 期待其他格式
                    if (isJson) {
                        if (retryCount == MAX_RETRY_COUNT-1) {
                            OkResponseLog.logError(request.method(), request.url().toString(), "响应格式与预期不符|"+responseString, getParamsInfo(request.body()), getHeadersInfo(request.headers()), getContentType(request), CONFIG_STRING);
                            return response;
                        }
                    } else {
                        // 响应其他
                        OkResponseLog.logSuccess(request.method(), request.url().toString(), responseString, getParamsInfo(request.body()), getHeadersInfo(request.headers()), getContentType(request), CONFIG_STRING);
                        return response;
                    }
                    response.close();
                }
            }catch (ConnectException e) {
                //无法建立连接
                isNoResponse = true;
            } catch (Exception e) {
                log.error("An error has occurred",e);
            } finally {
                retryCount++;
            }
        }
        if (isNoResponse) {
            OkResponseLog.logError(request.method(), request.url().toString(), "无响应", getParamsInfo(request.body()), getHeadersInfo(request.headers()), getContentType(request), CONFIG_STRING);
        }
        if (response == null) {
            //当无响应时，中断这次请求，如果返回null，则会导致Ok框架爆空指针异常
            throw new IOException();
        }
        return response;
    }
}