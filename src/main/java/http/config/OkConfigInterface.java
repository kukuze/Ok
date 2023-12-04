package http.config;

import http.enums.*;

import java.util.List;

/**
 * @author yjz
 * @date 2023/12/4$ 11:18$
 * @description:
 */
public interface OkConfigInterface {

    default ResponseFormat getResponseFormat() {
        return ResponseFormat.JSON;
    }

    default RetryStrategy getRetryStrategy() {
        return RetryStrategy.THREE_ATTEMPTS;
    }

    default SuccessCode getSuccessCode() {
        return SuccessCode.STANDARD_SUCCESS;
    }

    default ResponseDataField getResponseDataField() {
        return ResponseDataField.DATA;
    }

    default ResponseCodeField getResponseCodeField() {
        return ResponseCodeField.CODE;
    }

    default String getClientKey() {
        String codeField = this.getResponseCodeField().getFieldName();
        String dataField = this.getResponseDataField().getFieldName();
        int successCode = this.getSuccessCode().getCode();
        int attempts = this.getRetryStrategy().getAttempts();
        long intervalMillis = this.getRetryStrategy().getIntervalMillis();
        String typeName = this.getResponseFormat().getTypeName();
        return codeField + "-" + dataField + "-" + String.valueOf(successCode) + "-" + String.valueOf(attempts) + "-" + String.valueOf(intervalMillis) + "-" + typeName;
    }
}
