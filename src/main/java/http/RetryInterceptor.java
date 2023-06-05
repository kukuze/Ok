package http;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RetryInterceptor implements Interceptor {
    protected final static Logger log = LoggerFactory.getLogger(RetryInterceptor.class);

    private static final int MAX_RETRY_COUNT = 3; // 最大重试次数
    private static final int RETRY_DELAY_MILLIS = 100; // 重试间隔时间
    private static final List<Integer> codes = new ArrayList<>();

    static {
        codes.add(200);
        codes.add(201);
        codes.add(202);
        codes.add(203);
        codes.add(205);
        codes.add(206);
    }

    @Override
    public Response intercept(Chain chain) {
        Request request = chain.request();
        Response response = null;
        int retryCount = 0;
        while (retryCount < MAX_RETRY_COUNT) {
            try {
                response = chain.proceed(request);
                String contentType = response.header("Content-Type");
                if (codes.contains(response.code())&& contentType != null && contentType.contains("application/json")) {
                    if(responseCodeEq200(response)) {
                        log.info(request.url() + ":success");
                        return response;
                    }
                } else if (codes.contains(response.code())&& contentType != null && contentType.contains("text/html")) {
                    log.info(request.url() + ":success");
                    return response;
                } else {
                    Thread.sleep(RETRY_DELAY_MILLIS);
                    retryCount++;
                    if (retryCount != MAX_RETRY_COUNT) {
                        response.close();// 非最后一次重试失败需要关闭response
                    }
                }
            } catch (Exception e) {
                log.error("正在进行第" + Integer.valueOf(retryCount + 1) + "次重试");
                retryCount++;
                log.warn(request.url() + "请求失败", e);
            }
        }
        JSONObject jsonObject = null;
        try {
            String responseBody = response.peekBody(Long.MAX_VALUE).string();
            jsonObject = JSONObject.parseObject(responseBody);
        } catch (Exception e) {

        }
        log.error("\n====== REQUEST FAILURE DETAILS ======\nMethod: {}\nURL: {}\nResponse: {}\nParams: {}\nHeaders: {}\nContent-Type: {}\nPlease check /logs/today/error.log for more details\n======================================",
                request.method(),request.url(), jsonObject, getParamsInfo(request.body()), getHeadersInfo(request.headers()),getContentType(request));
        return response;
    }

    public boolean responseCodeEq200(Response response) {
        boolean flag = false; // 默认值为false
        try {
            String responseBody = response.peekBody(Long.MAX_VALUE).string();
            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            //如果响应值没有code字段则默认设置为200
            String code = Optional.ofNullable(jsonObject.getString("code")).orElse("200");
            flag = "200".equals(code);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return flag;
        }
    }

    private String getParamsInfo(RequestBody requestBody) {
        if (requestBody instanceof FormBody) {
            FormBody formBody = (FormBody) requestBody;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < formBody.size(); i++) {
                sb.append(formBody.name(i))
                        .append("=")
                        .append(formBody.value(i))
                        .append("&");
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1); // 移除最后一个 "&" 符号
            }
            return sb.toString();
        }else {
            try {
                final Buffer buffer = new Buffer();
                if (requestBody != null) {
                    requestBody.writeTo(buffer);
                    return buffer.readUtf8();
                } else {
                    return "";
                }
            } catch (Exception e) {
                return "Did not work";
            }
        }
    }
    private String getHeadersInfo(Headers headers) {
        StringBuilder headersInfo = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            headersInfo.append(headers.name(i)).append("=").append(headers.value(i)).append("&");
        }
        if (headersInfo.length() > 0) {
            headersInfo.setLength(headersInfo.length() - 1); // 移除最后一个 "&" 符号
        }
        return headersInfo.toString();
    }
    private String getContentType(Request request) {
        if (request.body() != null && request.body().contentType() != null) {
            return request.body().contentType().toString();
        } else {
            return "Unknown";
        }
    }

}
