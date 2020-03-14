package cn.xanderye.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * http请求工具
 * @author XanderYe
 * @date 2020/2/4
 */
public class HttpUtil {
    /**
     * 是否使用fiddler代理
     */
    private static boolean useFiddler = false;

    /**
     * socket连接超时
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 15000;
    /**
     * 请求超时
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
    /**
     * 默认编码
     */
    private static final String CHARSET = "UTF-8";
    /**
     * 默认请求头
     */
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36";

    private static CloseableHttpClient httpClient;

    private static CookieStore cookieStore;

    // 静态代码块初始化配置
    static {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        cookieStore = new BasicCookieStore();
        httpClient = custom().setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(config).build();
    }


    /**
     * 创建httpClientBuilder
     * @return org.apache.http.impl.client.HttpClientBuilder
     * @author XanderYe
     * @date 2020/2/14
     */
    private static HttpClientBuilder custom() {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        if (useFiddler) {
            return httpClientBuilder
                    // 忽略证书
                    .setSSLSocketFactory(ignoreCertificates())
                    // 使用代理
                    .setProxy(new HttpHost("127.0.0.1", 8888));
        }
        return httpClientBuilder;
    }

    public static String doGet(String url, Map<String, Object> params) {
        return doGet(url, null, params);
    }

    public static String doGetWithCookie(String url, Map<String, Object> headers, Map<String, Object> cookies, Map<String, Object> params) {
        StringBuilder stringBuilder = new StringBuilder();
        if (cookies != null && cookies.size() > 0) {
            for (Map.Entry<String, Object> entry : cookies.entrySet()) {
                stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
            }
        }
        if (headers == null) {
            headers = new HashMap<>(16);
        }
        headers.put("Cookie", stringBuilder.toString());
        return doGet(url, headers, params);
    }

    public static String doPost(String url, Map<String, Object> params) {
        return doPost(url, null, params);
    }

    public static String doPostWithCookie(String url, Map<String, Object> headers, Map<String, Object> cookies, Map<String, Object> params) {
        StringBuilder stringBuilder = new StringBuilder();
        if (cookies != null && cookies.size() > 0) {
            for (Map.Entry<String, Object> entry : cookies.entrySet()) {
                stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
            }
        }
        if (headers == null) {
            headers = new HashMap<>(16);
        }
        headers.put("Cookie", stringBuilder.toString());
        return doPost(url, headers, params);
    }

    /**
     * get请求基础方法
     * @param url
     * @param headers
     * @param params
     * @return java.lang.String
     * @author XanderYe
     * @date 2020/2/4
     */
    public static String doGet(String url, Map<String, Object> headers, Map<String, Object> params) {
        // 清空上次cookie
        cookieStore.clear();
        // 拼接参数
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> pairs = new ArrayList<>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String value = (entry.getValue()).toString();
                if (value != null) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
            try {
                // 将请求参数和url进行拼接
                url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, CHARSET));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", DEFAULT_USER_AGENT);
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpGet.setHeader(entry.getKey(), (entry.getValue()).toString());
            }
        }
        CloseableHttpResponse response = null;
        HttpEntity resultEntity = null;
        try {
            HttpClientContext httpClientContext = new HttpClientContext();
            response = httpClient.execute(httpGet, httpClientContext);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                resultEntity = response.getEntity();
                if (resultEntity != null) {
                    return EntityUtils.toString(resultEntity, CHARSET);
                }
            } else {
                throw new RuntimeException("error status code :" + statusCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultEntity != null) {
                    EntityUtils.consume(resultEntity);
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * post请求基础方法
     * @param url
     * @param headers
     * @param params
     * @return java.lang.String
     * @author XanderYe
     * @date 2020/2/4
     */
    public static String doPost(String url, Map<String, Object> headers, Map<String, Object> params) {
        // 清空上次cookie
        cookieStore.clear();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", DEFAULT_USER_AGENT);
        // 拼接参数
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> pairs = new ArrayList<>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String value = (entry.getValue()).toString();
                if (value != null) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(pairs, CHARSET));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), (entry.getValue()).toString());
            }
        }
        CloseableHttpResponse response = null;
        HttpEntity resultEntity = null;
        try {
            HttpClientContext httpClientContext = new HttpClientContext();
            response = httpClient.execute(httpPost, httpClientContext);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                resultEntity = response.getEntity();
                if (resultEntity != null) {
                    return EntityUtils.toString(resultEntity, CHARSET);
                }
            } else {
                throw new RuntimeException("error status code :" + statusCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultEntity != null) {
                    EntityUtils.consume(resultEntity);
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * POST提交JSON格式
     * @param url
     * @param headers
     * @param json
     * @return java.lang.String
     * @author XanderYe
     * @date 2020/2/12
     */
    public static String doPost(String url, Map<String, Object> headers, String json) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", DEFAULT_USER_AGENT);
        // 拼接参数
        if (json != null && !"".equals(json)) {
            StringEntity requestEntity = new StringEntity(json,CHARSET);
            requestEntity.setContentEncoding(CHARSET);
            requestEntity.setContentType("application/json");
            httpPost.setEntity(requestEntity);
        }
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), (entry.getValue()).toString());
            }
        }
        CloseableHttpResponse response = null;
        HttpEntity resultEntity = null;
        try {
            HttpClientContext httpClientContext = new HttpClientContext();
            response = httpClient.execute(httpPost, httpClientContext);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                resultEntity = response.getEntity();
                if (resultEntity != null) {
                    return EntityUtils.toString(resultEntity, CHARSET);
                }
            } else {
                throw new RuntimeException("error status code :" + statusCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultEntity != null) {
                    EntityUtils.consume(resultEntity);
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 下载
     * @param url
     * @param headers
     * @param params
     * @return byte[]
     * @author XanderYe
     * @date 2020-03-14
     */
    public static byte[] download(String url, Map<String, Object> headers, Map<String, Object> params) {
        // 清空上次cookie
        cookieStore.clear();
        // 拼接参数
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> pairs = new ArrayList<>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String value = (entry.getValue()).toString();
                if (value != null) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
            try {
                // 将请求参数和url进行拼接
                url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, CHARSET));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", DEFAULT_USER_AGENT);
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpGet.setHeader(entry.getKey(), (entry.getValue()).toString());
            }
        }
        CloseableHttpResponse response = null;
        HttpEntity resultEntity = null;
        try {
            HttpClientContext httpClientContext = new HttpClientContext();
            response = httpClient.execute(httpGet, httpClientContext);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                resultEntity = response.getEntity();
                if (resultEntity != null) {
                    return EntityUtils.toByteArray(resultEntity);
                }
            } else {
                throw new RuntimeException("error status code :" + statusCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultEntity != null) {
                    EntityUtils.consume(resultEntity);
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 忽略证数配置
     * @param
     * @return org.apache.http.conn.ssl.SSLConnectionSocketFactory
     * @author XanderYe
     * @date 2020/2/14
     */
    private static SSLConnectionSocketFactory ignoreCertificates() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build();
            return new SSLConnectionSocketFactory(sslContext);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取请求的cookie
     * @param
     * @return java.util.Map<java.lang.String,java.lang.String>
     * @author XanderYe
     * @date 2020/2/4
     */
    public static Map<String, String> getCookies() {
        List<Cookie> basicCookies = cookieStore.getCookies();
        if (basicCookies.size() > 0) {
            Map<String, String> cookies = new HashMap<>(16);
            for (Cookie cookie : basicCookies) {
                cookies.put(cookie.getName(), cookie.getValue());
            }
            return cookies;
        }
        return null;
    }
}
