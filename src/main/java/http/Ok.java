package http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import http.config.DefaultOkConfig;
import http.config.OkConfigInterface;
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
    private static final Logger log = LoggerFactory.getLogger(Ok.class);

    private static ConcurrentHashMap<String, OkHttpClient> clientMap = new ConcurrentHashMap<>();
    private Map<String, String> headerMap;
    private Map<String, String> paramMap;
    private List<String> urls;
    private List<Request.Builder> requests;
    private String clientKey;
    /**
     * 初始化okHttpClient，并且允许https访问
     */
    private Ok(OkConfigInterface okConfigInterface) {
        clientKey = okConfigInterface.getClientKey();
        //先get避免jdk1.8中computeIfAbsent获取值会加锁影响性能
        if(clientMap.get(clientKey)==null) {
            clientMap.computeIfAbsent(clientKey, key -> {
                TrustManager[] trustManagers = buildTrustManagers();
                return new OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .writeTimeout(20, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .sslSocketFactory(createSSLSocketFactory(trustManagers), (X509TrustManager) trustManagers[0])
                        .hostnameVerifier((hostName, session) -> true)
                        .retryOnConnectionFailure(true)
                        .addInterceptor(new RetryInterceptor(okConfigInterface))
                        .build();
            });
        }
    }


    /**
     * 创建OkHttpUtils
     *
     * @return
     */
    public static Ok builder(OkConfigInterface okConfigInterface) {
        return new Ok(okConfigInterface);
    }
    public static Ok builder() {
        return new Ok(DefaultOkConfig.getInstance());
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
     * @return
     */
    public Ok postForm() {
        if (requests == null) {
            requests = new ArrayList<Request.Builder>();
        }
        RequestBody requestBody;
        FormBody.Builder formBody = new FormBody.Builder();
        if (paramMap != null) {
            paramMap.forEach(formBody::add);
        }
        requestBody = formBody.build();
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            requests.add(new Request.Builder().post(requestBody).url(url));
        }
        return this;
    }

    public Ok postJson() {
        if (requests == null) {
            requests = new ArrayList<Request.Builder>();
        }
        RequestBody requestBody;
        requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(paramMap));
        for (int i = 0; i < urls.size(); i++) {
            requests.add(new Request.Builder().post(requestBody).url(urls.get(i)));
        }
        return this;
    }
    public Ok postJsonArray(JSONArray jsonArray) {
        if (requests == null) {
            requests = new ArrayList<Request.Builder>();
        }
        RequestBody requestBody;
        requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonArray.toJSONString());
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
     * description:本质与postform相同
     * author:yjz
     * @return http.Ok
     */
    public Ok postUrlEncoded() {
        return postForm();
    }


    /**
     * 同步请求
     *
     * @return 只添加一个url返回结果是jsonobject, 多url是jsonarray
     */
    public String sync() {
        setHeader(requests);
        List<String> res = null;
        try {
            if (requests.size() == 1) {
                Response response = clientMap.get(clientKey).newCall(requests.get(0).build()).execute();
                return response.body().string();
            } else {
                res = new ArrayList<>();
                Map<Integer, String> resultMap = new ConcurrentHashMap<>(); // 用于存储请求结果的Map
                CountDownLatch latch = new CountDownLatch(urls.size());
                for (int i = 0; i < requests.size(); i++) {
                    final Integer index = i;
                    Request.Builder request = requests.get(i);
                    Call call = clientMap.get(clientKey).newCall(request.build());
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            latch.countDown();
                        }
                        @Override
                        public void onResponse(Call call, Response response) {
                            try {
                                String result = response.body().string();
                                resultMap.put(index,result);
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
            return res.toString();
        } catch (IOException e) {
            return null;
        } catch (Exception e) {
            log.error("数据处理异常",e);
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
            clientMap.get(clientKey).newCall(request.build()).enqueue(new Callback() {
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
     * 异步请求，无接口回调
     *
     *
     */
    public void async() {
        setHeader(requests);
        for (Request.Builder request : requests) {
            clientMap.get(clientKey).newCall(request.build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }
                @Override
                public void onResponse(Call call, Response response){

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
                log.error("添加请求头异常",e);
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
            log.error("生成安全套接字工厂异常",e);
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