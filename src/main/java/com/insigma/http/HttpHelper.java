package com.insigma.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;

import com.insigma.common.rsa.RSAUtils;
import com.insigma.common.rsa.SignUtils;
import com.insigma.common.util.SUserUtil;
import com.insigma.json.JsonDateValueProcessor;

/**
 * Http辅助工具类</br>
 * 依赖HttpClient 4.5.x版本
 *
 * @author comven
 */
public class HttpHelper {


	private static Log log = LogFactory.getLog(HttpHelper.class);
    private static final String DEFAULT_CHARSET = "UTF-8";// 默认请求编码
    private static final int DEFAULT_SOCKET_TIMEOUT = 60000;// 默认等待响应时间(毫秒)
    private static final int DEFAULT_RETRY_TIMES = 0;// 默认执行重试的次数

    public static JsonConfig jsonConfig;

    static{
        jsonConfig = new JsonConfig();
        jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
        jsonConfig.setJsonPropertyFilter( new PropertyFilter(){
            public boolean apply(Object source/* 属性的拥有者 */ , String name /*属性名字*/ , Object value/* 属性值 */ ){
                //过滤token
                if(name.equals("token")){
                    return true;
                }
                if(value instanceof List){
                    List<Object> list=(List<Object> )value;
                    if(list.size()==0){
                        return true;
                    }
                }
                return null==value||value.equals("");
            }
        });
    }

    /**
     * 创建一个默认的可关闭的HttpClient
     *
     * @return
     */
    public static CloseableHttpClient createHttpClient() {
        return createHttpClient(DEFAULT_RETRY_TIMES, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * 创建一个可关闭的HttpClient
     *
     * @param socketTimeout 请求获取数据的超时时间
     * @return
     */
    public static CloseableHttpClient createHttpClient(int socketTimeout) {
        return createHttpClient(DEFAULT_RETRY_TIMES, socketTimeout);
    }

    /**
     * 创建一个可关闭的HttpClient
     *
     * @param socketTimeout 请求获取数据的超时时间
     * @param retryTimes    重试次数，小于等于0表示不重试
     * @return
     */
    public static CloseableHttpClient createHttpClient(int retryTimes, int socketTimeout) {
        Builder builder = RequestConfig.custom();
        builder.setConnectTimeout(5000);// 设置连接超时时间，单位毫秒
        builder.setConnectionRequestTimeout(1000);// 设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
        if (socketTimeout >= 0) {
            builder.setSocketTimeout(socketTimeout);// 请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
        }
        RequestConfig defaultRequestConfig = builder.setCookieSpec(CookieSpecs.STANDARD_STRICT).setExpectContinueEnabled(true).setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST)).setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
        // 开启HTTPS支持
        enableSSL();
        // 创建可用Scheme
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", socketFactory).build();
        // 创建ConnectionManager，添加Connection配置信息
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        if (retryTimes > 0) {
            setRetryHandler(httpClientBuilder, retryTimes);
        }
        CloseableHttpClient httpClient = httpClientBuilder.setConnectionManager(connectionManager).setDefaultRequestConfig(defaultRequestConfig).build();
        return httpClient;
    }

    /**
     * 执行GET请求
     *
     * @param url    远程URL地址
     * @return HttpResult
     * @throws IOException
     */
    public static HttpResult executeGet(String url,boolean isencrpty ) throws Exception {
        CloseableHttpClient httpClient = createHttpClient(DEFAULT_SOCKET_TIMEOUT);
        return executeGet(httpClient, url,  null, null, DEFAULT_CHARSET, true,isencrpty);
    }


    /**
     * 执行GET请求
     *
     * @param url           远程URL地址
     * @param charset       请求的编码，默认UTF-8
     * @param socketTimeout 超时时间（毫秒）
     * @return HttpResult
     * @throws IOException
     */
    public static HttpResult executeGet(String url,  String charset, int socketTimeout,boolean isencrpty ) throws Exception {
        CloseableHttpClient httpClient = createHttpClient(socketTimeout);
        return executeGet(httpClient, url, null, null, charset, true,isencrpty);
    }


    /**
     * 执行HttpGet请求
     *
     * @param httpClient      HttpClient客户端实例，传入null会自动创建一个
     * @param url             请求的远程地址
     * @param referer         referer信息，可传null
     * @param cookie          cookies信息，可传null
     * @param charset         请求编码，默认UTF8
     * @param closeHttpClient 执行请求结束后是否关闭HttpClient客户端实例
     * @return HttpResult
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static HttpResult executeGet(CloseableHttpClient httpClient, String url,  String referer, String cookie, String charset, boolean closeHttpClient,boolean isencrpty) throws Exception {
        CloseableHttpResponse httpResponse = null;
        try {
            charset = getCharset(charset);
            httpResponse = executeGetResponse(httpClient, url, referer, cookie);
            //Http请求状态码
            Integer statusCode = httpResponse.getStatusLine().getStatusCode();
            if(statusCode==HttpStatus.SC_OK){
                String content = getResult(httpResponse, charset,isencrpty);
                return new HttpResult(statusCode, content);
            }else{
                throw new Exception("系统请求异常："+statusCode+",异常信息="+httpResponse.getStatusLine().getReasonPhrase());
            }
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (closeHttpClient && httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param httpClient httpclient对象
     * @param url        执行GET的URL地址
     * @param referer    referer地址
     * @param cookie     cookie信息
     * @return CloseableHttpResponse
     * @throws IOException
     */
    public static CloseableHttpResponse executeGetResponse(CloseableHttpClient httpClient, String url,  String referer, String cookie) throws IOException {
        if (httpClient == null) {
            httpClient = createHttpClient();
        }
        log.info("get请求url:" + url);
        HttpGet get = new HttpGet(urlSign(url));
        if (SUserUtil.getCurrentUser() != null) {
            get.setHeader("Authorization", "Bearer " + SUserUtil.getCurrentUser().getToken());
        }
        return httpClient.execute(get);
    }


    /**
     * 简单方式执行POST请求
     *
     * @param url       远程URL地址
     * @param paramsObj post的参数，支持map<String,String>,JSON,XML
     * @return HttpResult
     * @throws IOException
     */
    public static HttpResult executePost(String url, Object paramsObj, boolean isencrpty) throws Exception {
        CloseableHttpClient httpClient = createHttpClient(DEFAULT_SOCKET_TIMEOUT);
        return executePost(httpClient, url, paramsObj, null, null, DEFAULT_CHARSET, true,isencrpty);
    }

    /**
     * 执行HttpPost请求
     *
     * @param httpClient      HttpClient客户端实例，传入null会自动创建一个
     * @param url             请求的远程地址
     * @param paramsObj       提交的参数信息，目前支持Map,和String(JSON\xml)
     * @param referer         referer信息，可传null
     * @param cookie          cookies信息，可传null
     * @param charset         请求编码，默认UTF8
     * @param closeHttpClient 执行请求结束后是否关闭HttpClient客户端实例
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     */
    public static HttpResult executePost(CloseableHttpClient httpClient, String url, Object paramsObj,  String referer, String cookie, String charset, boolean closeHttpClient,boolean isencrpty) throws Exception {
        CloseableHttpResponse httpResponse = null;
        try {
            charset = getCharset(charset);
            httpResponse = executePostResponse(httpClient, url, paramsObj, referer, cookie, charset,isencrpty);
            //Http请求状态码
            Integer statusCode = httpResponse.getStatusLine().getStatusCode();
            if(statusCode.equals(HttpStatus.SC_OK)){
                String content = getResult(httpResponse, charset,isencrpty);
                return new HttpResult(statusCode, content);
            }else{
                throw new Exception("请求地址"+url+"失败,http状态"+ statusCode+"详细情况 "+ httpResponse.getStatusLine().getReasonPhrase());
            }
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            if (closeHttpClient && httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    /**
     * @param httpClient HttpClient对象
     * @param url        请求的网络地址
     * @param paramsObj  参数信息
     * @param referer    来源地址
     * @param cookie     cookie信息
     * @param charset    通信编码
     * @return CloseableHttpResponse
     * @throws IOException
     */
    private static CloseableHttpResponse executePostResponse(CloseableHttpClient httpClient, String url, Object paramsObj,  String referer, String cookie, String charset,boolean isencrpty) throws Exception {
    	log.info("post请求url:" + url);
    	if (httpClient == null) {
            httpClient = createHttpClient();
        }
        HttpPost post = new HttpPost(urlSign(url));
        if (SUserUtil.getCurrentUser() != null) {
            post.setHeader("Authorization", "Bearer " + SUserUtil.getCurrentUser().getToken());
        }
        // 设置参数
        HttpEntity httpEntity = getEntity(paramsObj, charset,isencrpty);
        if (httpEntity != null) {
            post.setEntity(httpEntity);
        }
        return httpClient.execute(post);
    }

    /**
     * 执行文件上传
     *
     * @param remoteFileUrl 远程接收文件的地址
     * @param
     * @param localFile
     * @param file_name
     * @param file_bus_type
     * @param file_bus_id
     * @param userid
     * @return
     * @throws IOException
     */
    public static HttpResult executeUploadFile(String remoteFileUrl,  File localFile, String file_name, String file_bus_type, String file_bus_id, String userid,boolean isencrpty) throws Exception {
        return executeUploadFile(remoteFileUrl, localFile, file_name, file_bus_type, file_bus_id, userid, DEFAULT_CHARSET, isencrpty);
    }

    /**
     * 执行文件上传
     *
     * @param remoteFileUrl 远程接收文件的地址
     * @param
     * @param localFile
     * @param file_name
     * @param file_bus_type
     * @param file_bus_id
     * @param userid
     * @return
     * @throws IOException
     */
    public static HttpResult executeUploadFile(String remoteFileUrl,  File localFile, String file_name, String file_bus_type, String file_bus_id, String userid, String desc,boolean isencrpty) throws Exception {
        return executeUploadFile(remoteFileUrl, localFile, file_name, file_bus_type, file_bus_id, userid, DEFAULT_CHARSET, true, desc,isencrpty);
    }

    /**
     * 执行文件上传
     *
     * @param url
     * @param
     * @param localFile
     * @param file_name
     * @param file_bus_type
     * @param file_bus_id
     * @param userid
     * @param charset         请求编码，默认UTF-8
     * @param closeHttpClient 执行请求结束后是否关闭HttpClient客户端实例
     * @return
     * @throws IOException
     */
    public static HttpResult executeUploadFile(String url,  File localFile, String file_name, String file_bus_type, String file_bus_id, String userid, String charset, boolean closeHttpClient,boolean isencrpty) throws Exception {
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = createHttpClient();
            HttpPost httpPost = new HttpPost(urlSign(url));
            if (SUserUtil.getCurrentUser() != null) {
                httpPost.setHeader("Authorization", "Bearer " + SUserUtil.getCurrentUser().getToken());
            }
            // 把文件转换成流对象FileBody
            FileBody fileBody = new FileBody(localFile);
            //form参数
            StringBody file_name_body = new StringBody(URLEncoder.encode(file_name, "UTF-8"), ContentType.APPLICATION_FORM_URLENCODED);
            StringBody file_bus_type_body = new StringBody(file_bus_type, ContentType.APPLICATION_FORM_URLENCODED);
            StringBody file_bus_id_body = new StringBody(file_bus_id, ContentType.APPLICATION_FORM_URLENCODED);
            StringBody userid_body = new StringBody(userid, ContentType.APPLICATION_FORM_URLENCODED);
            // 以浏览器兼容模式运行，防止文件名乱码。
            HttpEntity reqEntity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addPart("uploadFile", fileBody)
                    .addPart("file_name", file_name_body)
                    .addPart("file_bus_type", file_bus_type_body)
                    .addPart("file_bus_id", file_bus_id_body)
                    .addPart("userid", userid_body).setCharset(CharsetUtils.get("GBK")).build();
            httpPost.setEntity(reqEntity);
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            charset = getCharset(charset);
            String content = getResult(httpResponse, charset,isencrpty);
            return new HttpResult(statusCode, content);
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (closeHttpClient && httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 执行文件上传
     *
     * @param url
     * @param
     * @param localFile
     * @param file_name
     * @param file_bus_type
     * @param file_bus_id
     * @param userid
     * @param charset         请求编码，默认UTF-8
     * @param closeHttpClient 执行请求结束后是否关闭HttpClient客户端实例
     * @return
     * @throws IOException
     */
    public static HttpResult executeUploadFile(String url,  File localFile, String file_name, String file_bus_type, String file_bus_id, String userid, String charset, boolean closeHttpClient, String desc,boolean isencrpty) throws Exception {
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = createHttpClient();
            HttpPost httpPost = new HttpPost(urlSign(url));
            if (SUserUtil.getCurrentUser() != null) {
            	httpPost.setHeader("Authorization", "Bearer " + SUserUtil.getCurrentUser().getToken());
            }
            // 把文件转换成流对象FileBody
            FileBody fileBody = new FileBody(localFile);
            //form参数
            StringBody file_name_body = new StringBody(URLEncoder.encode(file_name, "UTF-8"), ContentType.APPLICATION_FORM_URLENCODED);
            StringBody file_bus_type_body = new StringBody(file_bus_type, ContentType.APPLICATION_FORM_URLENCODED);
            StringBody file_bus_id_body = new StringBody(file_bus_id, ContentType.APPLICATION_FORM_URLENCODED);
            StringBody userid_body = new StringBody(userid, ContentType.APPLICATION_FORM_URLENCODED);
            if(desc == null){
                desc = "";
            }
            StringBody desc_body = new StringBody(URLEncoder.encode(desc, "UTF-8"), ContentType.APPLICATION_FORM_URLENCODED);
            // 以浏览器兼容模式运行，防止文件名乱码。
            HttpEntity reqEntity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addPart("uploadFile", fileBody)
                    .addPart("file_name", file_name_body)
                    .addPart("file_bus_type", file_bus_type_body)
                    .addPart("file_bus_id", file_bus_id_body)
                    .addPart("userid", userid_body)
                    .addPart("desc", desc_body).setCharset(CharsetUtils.get("UTF-8")).build();
            httpPost.setEntity(reqEntity);
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String content = getResult(httpResponse, charset,isencrpty);
            return new HttpResult(statusCode, content);
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (closeHttpClient && httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 执行文件上传(以二进制流方式)
     *
     * @param httpClient      HttpClient客户端实例，传入null会自动创建一个
     * @param url   远程接收文件的地址
     * @param localFilePath   本地文件地址
     * @param charset         请求编码，默认UTF-8
     * @param closeHttpClient 执行请求结束后是否关闭HttpClient客户端实例
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static HttpResult executeUploadFileStream(CloseableHttpClient httpClient, String url, String localFilePath, String charset, boolean closeHttpClient,boolean isencrpty) throws ClientProtocolException, Exception {
        CloseableHttpResponse httpResponse = null;
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        try {
            if (httpClient == null) {
                httpClient = createHttpClient();
            }
            // 把文件转换成流对象FileBody
            File localFile = new File(localFilePath);
            fis = new FileInputStream(localFile);
            byte[] tmpBytes = new byte[1024];
            byte[] resultBytes = null;
            baos = new ByteArrayOutputStream();
            int len;
            while ((len = fis.read(tmpBytes, 0, 1024)) != -1) {
                baos.write(tmpBytes, 0, len);
            }
            resultBytes = baos.toByteArray();
            ByteArrayEntity byteArrayEntity = new ByteArrayEntity(resultBytes, ContentType.APPLICATION_OCTET_STREAM);
            HttpPost httpPost = new HttpPost(urlSign(url));
            if (SUserUtil.getCurrentUser() != null) {
            	httpPost.setHeader("Authorization", "Bearer " + SUserUtil.getCurrentUser().getToken());
            }
            httpPost.setEntity(byteArrayEntity);
            httpResponse = httpClient.execute(httpPost);
            Integer statusCode = httpResponse.getStatusLine().getStatusCode();
            String content = getResult(httpResponse, charset,isencrpty);
            return new HttpResult(statusCode, content);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                }
            }
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (Exception e) {
                }
            }
            if (closeHttpClient && httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 执行文件下载
     *
     * @param url
     * @param
     * @param localdir 本地存储文件路径
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static File executeDownloadFile(String url,  String localdir) throws ClientProtocolException, IOException {
        CloseableHttpResponse response = null;
        InputStream in;
        FileOutputStream fout = null;
        CloseableHttpClient httpClient = null;
        try {
            HttpGet httpget = new HttpGet(urlSign(url));
            log.info("get请求url:" + url);
            if (SUserUtil.getCurrentUser() != null) {
                httpget.setHeader("Authorization", "Bearer " + SUserUtil.getCurrentUser().getToken());
            }
            httpClient = createHttpClient();
            response = httpClient.execute(httpget);
           
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            in = entity.getContent();
            String filename = getFileName(response);
            File file = new File(localdir, filename);
            fout = new FileOutputStream(file);
            int l;
            byte[] tmp = new byte[1024];
            while ((l = in.read(tmp)) != -1) {
                fout.write(tmp, 0, l);
                // 注意这里如果用OutputStream.write(buff)的话，图片会失真
            }
            // 将文件输出到本地
            fout.flush();
            EntityUtils.consume(entity);
            return file;
        } finally {
            // 关闭低层流。
            if (fout != null) {
                try {
                    fout.close();
                } catch (Exception e) {
                }
            }
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                }
            }
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 获取response header中Content-Disposition中的filename值
     *
     * @param response
     * @return
     */
    public static String getFileName(HttpResponse response) {
        org.apache.http.Header contentHeader = response.getFirstHeader("Content-Disposition");
        String filename = null;
        if (contentHeader != null) {
            org.apache.http.HeaderElement[] values = contentHeader.getElements();
            if (values.length == 1) {
                NameValuePair param = values[0].getParameterByName("filename");
                if (param != null) {
                    try {
                        filename = URLDecoder.decode(param.getValue(), "utf-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            filename = getRandomFileName();
        }
        return filename;
    }

    /**
     * 获取随机文件名
     *
     * @return
     */
    public static String getRandomFileName() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 执行文件下载
     *
     * @param httpClient      HttpClient客户端实例，传入null会自动创建一个
     * @param url   远程下载文件地址
     * @param localFilePath   本地存储文件地址
     * @param charset         请求编码，默认UTF-8
     * @param closeHttpClient 执行请求结束后是否关闭HttpClient客户端实例
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static boolean executeDownloadFile(CloseableHttpClient httpClient, String url, String localFilePath, String charset, boolean closeHttpClient) throws ClientProtocolException, IOException {
        CloseableHttpResponse response = null;
        InputStream in = null;
        FileOutputStream fout = null;
        try {
            HttpGet httpget = new HttpGet(urlSign(url));
            response = httpClient.execute(httpget);
            log.info("get请求url:" + url);
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return false;
            }
            in = entity.getContent();
            File file = new File(localFilePath);
            fout = new FileOutputStream(file);
            int l;
            byte[] tmp = new byte[1024];
            while ((l = in.read(tmp)) != -1) {
                fout.write(tmp, 0, l);
                // 注意这里如果用OutputStream.write(buff)的话，图片会失真
            }
            // 将文件输出到本地
            fout.flush();
            EntityUtils.consume(entity);
            return true;
        } finally {
            // 关闭低层流。
            if (fout != null) {
                try {
                    fout.close();
                } catch (Exception e) {
                }
            }
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                }
            }
            if (closeHttpClient && httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 根据参数获取请求的Entity
     *
     * @param paramsObj
     * @param charset
     * @param isencrpty
     * @return
     * @throws UnsupportedEncodingException
     */
    private static HttpEntity getEntity(Object paramsObj, String charset,boolean isencrpty) throws Exception {
    	ContentType contenttype=ContentType.APPLICATION_JSON;
    	if (paramsObj == null) {
            log.info("当前未传入参数信息，无法生成HttpEntity");
            return null;
        }
    	if(contenttype.equals(ContentType.APPLICATION_JSON)){
    		StringEntity httpEntity=null;
    		httpEntity = new StringEntity(parseToJson(paramsObj,isencrpty), charset);
    		httpEntity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            return httpEntity;
    	}else{
            StringEntity httpEntity = new StringEntity(parseURLPair(paramsObj), charset);
            httpEntity.setContentType(ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
            return httpEntity;
        }
    }

    /**
     * 对象转换成json格式
     *
     * @param t
     * @return
     * @throws Exception
     */
    public static String parseToJson(Object t,boolean isencrpty) throws Exception {
        StringBuffer sb=null;
        JSONObject jsonobject= JSONObject.fromObject(t,jsonConfig);
        sb=new StringBuffer(jsonobject.toString());
        log.info("请求参数:" +   sb.toString());
        if(isencrpty){
        	 //获取加密及签名数据
            RSAUtils.EncryptDataStruct encryptDataStruct=RSAUtils.encryptByAesAndRsaPublickey(sb.toString());
            String data=JSONObject.fromObject(encryptDataStruct).toString();
            log.info("请求参数,加密后:" +data);
            return data;
        }else{
           return sb.toString();
        }
    }
    
   
    /**
     * 对象转换成url k=v方式
     *
     * @param object
     * @return
     * @throws Exception
     */
    public static String parseURLPair(Object object) throws Exception {
        StringBuffer sb = new StringBuffer();
        parseClass(object, object.getClass(), sb);
        if (sb.length() > 0) {
            String param = sb.deleteCharAt(sb.length() - 1).toString();
            log.info("请求参数:" + param);
            return param;
        } else {
            return "";
        }
    }

    /**
     * 解析class
     *
     * @param c
     * @param sb
     */
    public static void parseClass(Object t, Class c, StringBuffer sb) throws Exception {
        Field[] fields = c.getDeclaredFields();
        Map<String, Object> map = new TreeMap<>();
        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            if ("serialVersionUID".equals(name) || "token".equals(name)) {
                continue;
            }
            Object value = field.get(t);
            if (value != null) {
                if (field.getType().equals(Date.class)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                    map.put(name, sdf.format(value));
                } else {
                    map.put(name, value);
                }
            }
        }
        Set<Entry<String, Object>> set = map.entrySet();
        Iterator<Entry<String, Object>> it = set.iterator();
        while (it.hasNext()) {
            Entry<String, Object> e = it.next();
            sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
        }

        if (c.getSuperclass() != Object.class) {
            parseClass(t, c.getSuperclass(), sb);
        }
    }
    /**
     * 从结果中获取出String数据
     *
     * @param httpResponse http结果对象
     * @param charset      编码信息
     * @param isencrpty    是否加解密
     * @return String
     * @throws ParseException
     * @throws IOException
     */
    private static String getResult(CloseableHttpResponse httpResponse, String charset,boolean isencrpty) throws Exception {
        String result = null;
        if (httpResponse == null) {
            return result;
        }
        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) {
            return result;
        }
        result = EntityUtils.toString(entity, charset);
        EntityUtils.consume(entity);// 关闭应该关闭的资源，适当的释放资源 ;也可以把底层的流给关闭了
        if(isencrpty){
            try{
                return RSAUtils.decryptByAesAndRsaPublickey(result);
            }catch (Exception e){
                return result;
            }
        }else{
        	return result;
        }
    }

    /**
     * 转化请求编码
     *
     * @param charset 编码信息
     * @return String
     */
    private static String getCharset(String charset) {
        return charset == null ? DEFAULT_CHARSET : charset;
    }

    /**
     * 将map类型参数转化为NameValuePair集合方式
     *
     * @param paramsMap
     * @return
     */
    private static List<NameValuePair> getNameValuePairs(Map<String, String> paramsMap) {
        List<NameValuePair> list = new ArrayList<>();
        if (paramsMap == null || paramsMap.isEmpty()) {
            return list;
        }
        for (Entry<String, String> entry : paramsMap.entrySet()) {
            list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    /**
     * 开启SSL支持
     */
    private static void enableSSL() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{manager}, null);
            socketFactory = new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SSLConnectionSocketFactory socketFactory;

    // HTTPS网站一般情况下使用了安全系数较低的SHA-1签名，因此首先我们在调用SSL之前需要重写验证方法，取消检测SSL。
    private static TrustManager manager = new X509TrustManager() {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //

        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //

        }
    };

    /**
     * 为httpclient设置重试信息
     *
     * @param httpClientBuilder
     * @param retryTimes
     */
    private static void setRetryHandler(HttpClientBuilder httpClientBuilder, final int retryTimes) {
        HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount >= retryTimes) {
                    // Do not retry if over max retry count
                    return false;
                }
                if (exception instanceof InterruptedIOException) {
                    // Timeout
                    return false;
                }
                if (exception instanceof UnknownHostException) {
                    // Unknown host
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {
                    // Connection refused
                    return false;
                }
                if (exception instanceof SSLException) {
                    // SSL handshake exception
                    return false;
                }
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                if (idempotent) {
                    // 如果请求被认为是幂等的，那么就重试
                    // Retry if the request is considered idempotent
                    return true;
                }
                return false;
            }
        };
        httpClientBuilder.setRetryHandler(myRetryHandler);
    }

    /**
     * 执行文件上传
     *
     * @param remoteFileUrl 远程接收文件的地址
     * @param
     * @param localFile
     * @param file_name
     * @param file_bus_type
     * @param file_bus_id
     * @param userid
     * @return
     * @throws IOException
     */
    public static HttpResult executeUploadFileProvince(String remoteFileUrl,  File localFile, String file_name, String file_bus_type, String file_bus_id, String userid,String fileRandomFlag, String desc,boolean isencrpty) throws Exception {
        return executeUploadFileForProvince(remoteFileUrl, localFile, file_name, file_bus_type, file_bus_id, userid,fileRandomFlag, DEFAULT_CHARSET, true, desc,isencrpty);
    }

    /**
     * 执行文件上传(最多跑一次)
     *
     * @param url
     * @param
     * @param localFile
     * @param file_name
     * @param file_bus_type
     * @param file_bus_id
     * @param userid
     * @param charset         请求编码，默认UTF-8
     * @param closeHttpClient 执行请求结束后是否关闭HttpClient客户端实例
     * @return
     * @throws IOException
     */
    public static HttpResult executeUploadFileForProvince(String url,  File localFile, String file_name, String file_bus_type, String file_bus_id, String userid,String fileRandomFlag, String charset, boolean closeHttpClient, String desc,boolean isencrpty) throws Exception {
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = createHttpClient();
            HttpPost httpPost = new HttpPost(url);
            // 把文件转换成流对象FileBody
            FileBody fileBody = new FileBody(localFile);
            //form参数
            StringBody file_name_body = new StringBody(URLEncoder.encode(file_name, "UTF-8"), ContentType.APPLICATION_FORM_URLENCODED);
            StringBody file_bus_type_body = new StringBody(file_bus_type, ContentType.APPLICATION_FORM_URLENCODED);
            StringBody file_bus_id_body = new StringBody(file_bus_id, ContentType.APPLICATION_FORM_URLENCODED);
            StringBody userid_body = new StringBody(userid, ContentType.APPLICATION_FORM_URLENCODED);
            StringBody fileRandomFlag_body = new StringBody(fileRandomFlag, ContentType.APPLICATION_FORM_URLENCODED);
            if(desc == null){
                desc = "";
            }
            StringBody desc_body = new StringBody(URLEncoder.encode(desc, "UTF-8"), ContentType.APPLICATION_FORM_URLENCODED);
            // 以浏览器兼容模式运行，防止文件名乱码。
            HttpEntity reqEntity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addPart("uploadFile", fileBody)
                    .addPart("file_name", file_name_body)
                    .addPart("file_bus_type", file_bus_type_body)
                    .addPart("file_bus_id", file_bus_id_body)
                    .addPart("userid", userid_body)
                    .addPart("fileRandomFlag", fileRandomFlag_body)
                    .addPart("desc", desc_body).setCharset(CharsetUtils.get("UTF-8")).build();
            httpPost.setEntity(reqEntity);
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String content =  getResult(httpResponse, charset,isencrpty);
            return new HttpResult(statusCode, content);
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (closeHttpClient && httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 请求地址数字签名
     * @param url
     * @return
     */
    public static String urlSign(String url){
        if(url.indexOf("?")!=-1){
             url+= "&"+SignUtils.signature();
        }else{
             url+= "?"+SignUtils.signature();
        }
        log.info("请求地址签名:" + url);
        return url;
    }
}
