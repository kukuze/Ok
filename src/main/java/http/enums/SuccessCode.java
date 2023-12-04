package http.enums;

public enum SuccessCode {
    STANDARD_SUCCESS(200), // 标准HTTP成功响应
    CUSTOM_SUCCESS_1(1),   // 自定义成功响应1
    CUSTOM_SUCCESS_2(2);   // 自定义成功响应2


    private final int code;

    SuccessCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
