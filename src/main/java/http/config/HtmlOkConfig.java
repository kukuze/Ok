package http.config;

import http.enums.ResponseFormat;
import http.enums.RetryStrategy;

public class HtmlOkConfig implements OkConfigInterface {
    private static HtmlOkConfig instance;
    private HtmlOkConfig() {
    }

    public static HtmlOkConfig getInstance() {
        if (instance == null) {
            synchronized (HtmlOkConfig.class) {
                if (instance == null) {
                    instance = new HtmlOkConfig();
                }
            }
        }
        return instance;
    }

    @Override
    public RetryStrategy getRetryStrategy() {
        return RetryStrategy.NO_RETRY;
    }

    @Override
    public ResponseFormat getResponseFormat() {
        return ResponseFormat.HTML;
    }
}