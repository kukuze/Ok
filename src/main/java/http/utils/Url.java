package http.utils;

import java.net.URLEncoder;
import java.util.Map;

/**
 * @author yjz
 * @date 2023/5/12$ 13:15$
 * @description:
 */
public class Url {
    /**
     * description:remark=%E8%BD%B4%E6%89%BF%E6%95%85%E9%9A%9C&username=%E6%9D%8E%E6%9F%90%E6%9F%90
     * author:yjz
     * @param map
     * @return java.lang.String
     */
    public static String mapToEncodedUrl(Map<String, String> map) {
        StringBuffer suffix = new StringBuffer();
        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                suffix.append(URLEncoder.encode(entry.getKey(), "utf-8")).
                        append("=").
                        append(URLEncoder.encode(entry.getValue(), "utf-8")).
                        append("&");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return suffix.deleteCharAt(suffix.length() - 1).toString();
    }
}
