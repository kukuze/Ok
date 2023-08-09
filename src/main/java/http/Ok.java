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
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static http.utils.Url.mapToEncodedUrl;


public class Ok {
    protected final static Logger log = LoggerFactory.getLogger(Ok.class);
    private static volatile OkHttpClient okHttpClient = null;
    private Map<String, String> headerMap;
    private Map<String, String> paramMap;
    private List<String> urls;
    private List<Request.Builder> requests;
    private JSONArray res;


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
        if (this.urls == null) {
            this.urls = new ArrayList<>();
        }
        this.urls.add(url);
        return this;
    }

    public Ok urls(List<String> urls) {
        if (this.urls == null) {
            this.urls = new ArrayList<>();
        }
        this.urls.addAll(urls);
        return this;
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

    public Ok addParamMap(Map map) {
        if (paramMap == null) {
            paramMap = new LinkedHashMap<>(16);
        }
        paramMap.putAll(map);
        return this;
    }

    public Ok addHeaderMap(Map map) {
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
        if (requests == null) {
            requests = new ArrayList<Request.Builder>();
        }
        StringBuffer suffix = new StringBuffer();
        if (paramMap != null&&paramMap.size()!=0) {
            suffix.append("?");
            suffix.append(mapToEncodedUrl(paramMap));
        }
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            Request.Builder request = new Request.Builder().get();
            request.url(url + suffix);
            requests.add(request);
        }
        return this;
    }


    /**
     * 构建通过post的格式发送form的request
     *
     * @param formData
     * @return
     */
    public Ok postForm(Map<String, String> formData) {
        if (requests == null) {
            requests = new ArrayList<Request.Builder>();
        }
        RequestBody requestBody;
        FormBody.Builder formBody = new FormBody.Builder();
        if (formData != null) {
            formData.forEach(formBody::add);
        }
        requestBody = formBody.build();
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            requests.add(new Request.Builder().post(requestBody).url(url));
        }
        return this;
    }

    /**
     * description:构建通过post的格式发送json的request
     * author:yjz
     *
     * @param jsonString 需要以json格式发送的对象
     * @return http.Ok
     */
    public Ok postJson(String jsonString) {
        if (requests == null) {
            requests = new ArrayList<Request.Builder>();
        }
        RequestBody requestBody;
        requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonString);
        for (int i = 0; i < urls.size(); i++) {
            requests.add(new Request.Builder().post(requestBody).url(urls.get(i)));
        }
        return this;
    }

    public Ok postJson(Map<String, String> json) {
        if (requests == null) {
            requests = new ArrayList<Request.Builder>();
        }
        RequestBody requestBody;
        requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(json));
        for (int i = 0; i < urls.size(); i++) {
            requests.add(new Request.Builder().post(requestBody).url(urls.get(i)));
        }
        return this;
    }
    public Ok post() {
        if (requests == null) {
            requests = new ArrayList<Request.Builder>();
        }
        RequestBody requestBody;
        requestBody = RequestBody.create(null,"");
        for (int i = 0; i < urls.size(); i++) {
            requests.add(new Request.Builder().post(requestBody).url(urls.get(i)));
        }
        return this;
    }

    /**
     * description:构建通过post的格式发送x-www-form-urlencoded的request
     * author:yjz
     *
     * @param xWwwFormUrlEncoded 需要以x-www-form-urlencoded格式发送的对象
     * @return http.Ok
     */
    public Ok postUrlEncoded(Map<String, String> xWwwFormUrlEncoded) {
        if (requests == null) {
            requests = new ArrayList<Request.Builder>();
        }
        RequestBody requestBody;
        String suffix = null;
        if (paramMap != null && paramMap.size() != 0) {
            suffix = mapToEncodedUrl(xWwwFormUrlEncoded);
        }
        requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), suffix);
        for (int i = 0; i < urls.size(); i++) {
            requests.add(new Request.Builder().post(requestBody).url(urls.get(i)));
        }
        return this;
    }


    /**
     * 同步请求
     *
     * @return 只添加一个url返回结果是jsonobject, 多url是jsonarray
     */
    public String sync() {
        setHeader(requests);
        try {
            if (requests.size() == 1) {
                Response response = okHttpClient.newCall(requests.get(0).build()).execute();
                return response.body().string();
            } else {
                if (res == null) {
                    res = new JSONArray();
                }
                Map<Integer, JSONObject> resultMap = new ConcurrentHashMap<>(); // 用于存储请求结果的Map
                CountDownLatch latch = new CountDownLatch(urls.size());
                for (int i = 0; i < requests.size(); i++) {
                    final Integer index = i;
                    Request.Builder request = requests.get(i);
                    Call call = okHttpClient.newCall(request.build());
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            latch.countDown();
                        }

                        @Override
                        public void onResponse(Call call, Response response) {
                            try {
                                String result = response.body().string();
                                JSONObject json = JSONObject.parseObject(result);
                                resultMap.put(index,json);
                            } catch (Exception e) {
                                log.error("异常",e);
                            }finally {
                                latch.countDown();
                            }
                        }
                    });
                }
                latch.await();
                for (int i = 0; i <urls.size(); i++) {
                    res.add(resultMap.get(i));
                }
            }
            return res.toJSONString();
        } catch (Exception e) {
            log.error("异常",e);
            return null;
        }
    }

    /**
     * 异步请求，带有接口回调
     *
     * @param callBack
     */
    public void async(ICallBack callBack) {
        setHeader(requests);
        for (Request.Builder request : requests) {
            okHttpClient.newCall(request.build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callBack.onFailure(call, e.getMessage());
                }


                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    callBack.onSuccessful(call, response.body().string());
                }
            });
        }

    }


    /**
     * 为requests添加请求头
     *
     * @param requests
     */
    private void setHeader(List<Request.Builder> requests) {
        if (headerMap != null) {
            try {
                for (int i = 0; i < requests.size(); i++) {
                    Request.Builder request = requests.get(i);
                    for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                        request.addHeader(entry.getKey(), entry.getValue());
                    }
                }
            } catch (Exception e) {
                log.error("异常",e);
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
            log.error(e.getMessage());
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