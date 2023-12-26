package http.utils;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yjz
 * @date 2023/12/26$ 19:09$
 * @description:
 */
public class ParseResponse {
    private static final Logger log = LoggerFactory.getLogger(ParseResponse.class);
    public static String getParamsInfo(RequestBody requestBody) {
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
        } else {
            try {
                final Buffer buffer = new Buffer();
                if (requestBody != null) {
                    requestBody.writeTo(buffer);
                    return buffer.readUtf8();
                } else {
                    return "";
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                return "Did not work";
            }
        }
    }

    public static String getHeadersInfo(Headers headers) {
        StringBuilder headersInfo = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            headersInfo.append(headers.name(i)).append("=").append(headers.value(i)).append("&");
        }
        if (headersInfo.length() > 0) {
            headersInfo.setLength(headersInfo.length() - 1); // 移除最后一个 "&" 符号
        }
        return headersInfo.toString();
    }

    public static String getContentType(Request request) {
        if (request.body() != null && request.body().contentType() != null) {
            return request.body().contentType().toString();
        } else {
            return "Unknown";
        }
    }
}
