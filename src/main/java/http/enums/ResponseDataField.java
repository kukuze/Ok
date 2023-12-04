package http.enums;

public enum ResponseDataField {
    DATA("data"),
    RESULT("result"),
    PAYLOAD("payload");
    private final String fieldName;

    ResponseDataField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
