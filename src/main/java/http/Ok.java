package http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class Ok {
    private static volatile OkHttpClient okHttpClient = null;
    public Map<String, String> headerMap;
    public Map<String, String> paramMap;
    private String url;
    private Request.Builder request;
    protected final static Logger log = LoggerFactory.getLogger(Ok.class);
 
    /**
     * 初始化okHttpClient，并且允许https访问
     */
    private Ok() {
        if (okHttpClient == null) {
            synchronized (Ok.class) {
                if (okHttpClient == null) {
                    TrustManager[] trustManagers = buildTrustManagers();
                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .sslSocketFactory(createSSLSocketFactory(trustManagers), (X509TrustManager) trustManagers[0])
                            .hostnameVerifier((hostName, session) -> true)
                            .retryOnConnectionFailure(true)
                            .addInterceptor(new RetryInterceptor())
                            .build();
                }
            }
        }
    }
 
 

 
 
    /**
     * 创建OkHttpUtils
     *
     * @return
     */
    public static Ok builder() {
        return new Ok();
    }
 
 
    /**
     * 添加url
     *
     * @param url
     * @return
     */
    public Ok url(String url) {
        this.url = url;
        return this;
    }
    /**
     * description:该方法的返回值不对应url顺序
     * author:yjz
     * @param urls
     * @return com.alibaba.fastjson.JSONArray
     */
    public  JSONArray urls(List<String>urls) throws InterruptedException {
        JSONArray res = new JSONArray();
        CountDownLatch latch = new CountDownLatch(urls.size());
        for (String url : urls) {
            request = new Request.Builder().get().url(url);
            Call call = okHttpClient.newCall(request.build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    latch.countDown();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String result = response.body().string();
                        JSONObject json =JSONObject.parseObject(result);
                        res.add(json);
                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        latch.await();
        return res;
    }
 
 
    /**
     * 添加参数
     * 
     * @param key   参数名
     * @param value 参数值
     * @return
     */
    public Ok addParam(String key, String value) {
        if (paramMap == null) {
            paramMap = new LinkedHashMap<>(16);
        }
        paramMap.put(key, value);
        return this;
    }
    public Ok addParamMap(Map map){
        if (paramMap == null) {
            paramMap = new LinkedHashMap<>(16);
        }
        paramMap.putAll(map);
        return this;
    }
    public Ok addHeaderMap(Map map){
        if (headerMap == null) {
            headerMap = new LinkedHashMap<>(16);
        }
        headerMap.putAll(map);
        return this;
    }
 
 
    /**
     * 添加请求头
     *
     * @param key   参数名
     * @param value 参数值
     * @return
     */
    public Ok addHeader(String key, String value) {
        if (headerMap == null) {
            headerMap = new LinkedHashMap<>(16);
        }
        headerMap.put(key, value);
        return this;
    }
 
 
    /**
     * 初始化get方法
     *
     * @return
     */
    public Ok get() {
        request = new Request.Builder().get();
        StringBuilder urlBuilder = new StringBuilder(url);
        if (paramMap != null) {
            urlBuilder.append("?");
            try {
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    urlBuilder.append(URLEncoder.encode(entry.getKey(), "utf-8")).
                            append("=").
                            append(URLEncoder.encode(entry.getValue(), "utf-8")).
                            append("&");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        request.url(urlBuilder.toString());
        return this;
    }
 
 
    /**
     * 初始化post方法
     *
     * @param isJsonPost true等于json的方式提交数据，类似postman里post方法的raw
     *                   false等于普通的表单提交
     * @return
     */
    public Ok post(boolean isJsonPost) {
        RequestBody requestBody;
        if (isJsonPost) {
            String json = "";
            if (paramMap != null) {
                json = JSON.toJSONString(paramMap);
            } 
            requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        } else {
            FormBody.Builder formBody = new FormBody.Builder();
            if (paramMap != null) {
                paramMap.forEach(formBody::add);
            }
            requestBody = formBody.build();
        }
        request = new Request.Builder().post(requestBody).url(url);
        return this;
    }
    /**
     * description:
     * author:yjz
     * @param jsonObject 需要以json格式发送的对象
     * @return http.Ok
     */
    public Ok post(String jsonObject) {
        RequestBody requestBody;
        requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject);
        request = new Request.Builder().post(requestBody).url(url);
        return this;
    }
 
 
    /**
     * 同步请求
     *
     * @return
     */
    public String sync() {
        setHeader(request);
        try {
            Response response = okHttpClient.newCall(request.build()).execute();
            assert response.body() != null;
            return response.body().string();
        } catch (Exception e) {
            return "";
        }
    }
 
    /**
     * 异步请求，带有接口回调
     *
     * @param callBack
     */
    public void async(ICallBack callBack) {
        setHeader(request);
        okHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onFailure(call, e.getMessage());
            }
 
 
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                callBack.onSuccessful(call, response.body().string());
            }
        });
    }
 
 
    /**
     * 为request添加请求头
     *
     * @param request
     */
    private void setHeader(Request.Builder request) {
        if (headerMap != null) {
            try {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
 
 
 
 
    /**
     * 生成安全套接字工厂，用于https请求的证书跳过
     *
     * @return
     */
    private static SSLSocketFactory createSSLSocketFactory(TrustManager[] trustAllCerts) {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssfFactory;
    }
 
 
    private static TrustManager[] buildTrustManagers() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }
 
 
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }
 
 
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }
 
 
    /**
     * 自定义一个接口回调
     */
    public interface ICallBack {
 
 
        void onSuccessful(Call call, String data);
 
 
        void onFailure(Call call, String errorMsg);
 
 
    }
}