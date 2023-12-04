package http.enums;

/**
 * @author yjz
 * @date 2023/12/4$ 14:49$
 * @description:
 */
public enum ResponseFormat {
    HTML("html"),
    JSON("json"),
    OTHER("other");
    private final String typeName;

    ResponseFormat(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
