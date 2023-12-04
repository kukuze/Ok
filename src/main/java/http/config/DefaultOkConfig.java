package http.config;

import http.enums.*;

public class DefaultOkConfig implements OkConfigInterface {
    private static DefaultOkConfig instance;
    private DefaultOkConfig() {
    }

    public static DefaultOkConfig getInstance() {
        if (instance == null) {
            synchronized (DefaultOkConfig.class) {
                if (instance == null) {
                    instance = new DefaultOkConfig();
                }
            }
        }
        return instance;
    }

    @Override
    public RetryStrategy getRetryStrategy() {
        return RetryStrategy.TWO_ATTEMPTS;
    }

}