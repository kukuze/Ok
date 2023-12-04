package http.enums;

public enum ResponseCodeField {
    CODE("code"),
    STATUS("status"),
    RESPONSE_CODE("responseCode");
    private final String fieldName;

    ResponseCodeField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
