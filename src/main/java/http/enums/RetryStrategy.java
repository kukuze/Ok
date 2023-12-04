package http.enums;

public enum RetryStrategy {
    THREE_ATTEMPTS(3, 100),
    TWO_ATTEMPTS(2, 100),
    NO_RETRY(1, 0);

    private final int attempts;
    private final long intervalMillis;

    RetryStrategy(int attempts, long intervalMillis) {
        this.attempts = attempts;
        this.intervalMillis = intervalMillis;
    }

    public int getAttempts() {
        return attempts;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }
}
