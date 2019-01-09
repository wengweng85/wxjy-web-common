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
 * Http����������</br>
 * ����HttpClient 4.5.x�汾
 *
 * @author comven
 */
public class HttpHelper {


	private static Log log = LogFactory.getLog(HttpHelper.class);
    private static final String DEFAULT_CHARSET = "UTF-8";// Ĭ���������
    private static final int DEFAULT_SOCKET_TIMEOUT = 60000;// Ĭ�ϵȴ���Ӧʱ��(����)
    private static final int DEFAULT_RETRY_TIMES = 0;// Ĭ��ִ�����ԵĴ���

    public static JsonConfig jsonConfig;

    static{
        jsonConfig = new JsonConfig();
        jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
        jsonConfig.setJsonPropertyFilter( new PropertyFilter(){
            public boolean apply(Object source/* ���Ե�ӵ���� */ , String name /*��������*/ , Object value/* ����ֵ */ ){
                //����token
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
     * ����һ��Ĭ�ϵĿɹرյ�HttpClient
     *
     * @return
     */
    public static CloseableHttpClient createHttpClient() {
        return createHttpClient(DEFAULT_RETRY_TIMES, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * ����һ���ɹرյ�HttpClient
     *
     * @param socketTimeout �����ȡ���ݵĳ�ʱʱ��
     * @return
     */
    public static CloseableHttpClient createHttpClient(int socketTimeout) {
        return createHttpClient(DEFAULT_RETRY_TIMES, socketTimeout);
    }

    /**
     * ����һ���ɹرյ�HttpClient
     *
     * @param socketTimeout �����ȡ���ݵĳ�ʱʱ��
     * @param retryTimes    ���Դ�����С�ڵ���0��ʾ������
     * @return
     */
    public static CloseableHttpClient createHttpClient(int retryTimes, int socketTimeout) {
        Builder builder = RequestConfig.custom();
        builder.setConnectTimeout(5000);// �������ӳ�ʱʱ�䣬��λ����
        builder.setConnectionRequestTimeout(1000);// ���ô�connect Manager��ȡConnection ��ʱʱ�䣬��λ���롣����������¼ӵ����ԣ���ΪĿǰ�汾�ǿ��Թ������ӳصġ�
        if (socketTimeout >= 0) {
            builder.setSocketTimeout(socketTimeout);// �����ȡ���ݵĳ�ʱʱ�䣬��λ���롣 �������һ���ӿڣ�����ʱ�����޷��������ݣ���ֱ�ӷ����˴ε��á�
        }
        RequestConfig defaultRequestConfig = builder.setCookieSpec(CookieSpecs.STANDARD_STRICT).setExpectContinueEnabled(true).setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST)).setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
        // ����HTTPS֧��
        enableSSL();
        // ��������Scheme
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", socketFactory).build();
        // ����ConnectionManager�����Connection������Ϣ
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        if (retryTimes > 0) {
            setRetryHandler(httpClientBuilder, retryTimes);
        }
        CloseableHttpClient httpClient = httpClientBuilder.setConnectionManager(connectionManager).setDefaultRequestConfig(defaultRequestConfig).build();
        return httpClient;
    }

    /**
     * ִ��GET����
     *
     * @param url    Զ��URL��ַ
     * @return HttpResult
     * @throws IOException
     */
    public static HttpResult executeGet(String url,boolean isencrpty ) throws Exception {
        CloseableHttpClient httpClient = createHttpClient(DEFAULT_SOCKET_TIMEOUT);
        return executeGet(httpClient, url,  null, null, DEFAULT_CHARSET, true,isencrpty);
    }


    /**
     * ִ��GET����
     *
     * @param url           Զ��URL��ַ
     * @param charset       ����ı��룬Ĭ��UTF-8
     * @param socketTimeout ��ʱʱ�䣨���룩
     * @return HttpResult
     * @throws IOException
     */
    public static HttpResult executeGet(String url,  String charset, int socketTimeout,boolean isencrpty ) throws Exception {
        CloseableHttpClient httpClient = createHttpClient(socketTimeout);
        return executeGet(httpClient, url, null, null, charset, true,isencrpty);
    }


    /**
     * ִ��HttpGet����
     *
     * @param httpClient      HttpClient�ͻ���ʵ��������null���Զ�����һ��
     * @param url             �����Զ�̵�ַ
     * @param referer         referer��Ϣ���ɴ�null
     * @param cookie          cookies��Ϣ���ɴ�null
     * @param charset         ������룬Ĭ��UTF8
     * @param closeHttpClient ִ������������Ƿ�ر�HttpClient�ͻ���ʵ��
     * @return HttpResult
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static HttpResult executeGet(CloseableHttpClient httpClient, String url,  String referer, String cookie, String charset, boolean closeHttpClient,boolean isencrpty) throws Exception {
        CloseableHttpResponse httpResponse = null;
        try {
            charset = getCharset(charset);
            httpResponse = executeGetResponse(httpClient, url, referer, cookie);
            //Http����״̬��
            Integer statusCode = httpResponse.getStatusLine().getStatusCode();
            if(statusCode==HttpStatus.SC_OK){
                String content = getResult(httpResponse, charset,isencrpty);
                return new HttpResult(statusCode, content);
            }else{
                throw new Exception("ϵͳ�����쳣��"+statusCode+",�쳣��Ϣ="+httpResponse.getStatusLine().getReasonPhrase());
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
     * @param httpClient httpclient����
     * @param url        ִ��GET��URL��ַ
     * @param referer    referer��ַ
     * @param cookie     cookie��Ϣ
     * @return CloseableHttpResponse
     * @throws IOException
     */
    public static CloseableHttpResponse executeGetResponse(CloseableHttpClient httpClient, String url,  String referer, String cookie) throws IOException {
        if (httpClient == null) {
            httpClient = createHttpClient();
        }
        log.info("get����url:" + url);
        HttpGet get = new HttpGet(urlSign(url));
        if (SUserUtil.getCurrentUser() != null) {
            get.setHeader("Authorization", "Bearer " + SUserUtil.getCurrentUser().getToken());
        }
        return httpClient.execute(get);
    }


    /**
     * �򵥷�ʽִ��POST����
     *
     * @param url       Զ��URL��ַ
     * @param paramsObj post�Ĳ�����֧��map<String,String>,JSON,XML
     * @return HttpResult
     * @throws IOException
     */
    public static HttpResult executePost(String url, Object paramsObj, boolean isencrpty) throws Exception {
        CloseableHttpClient httpClient = createHttpClient(DEFAULT_SOCKET_TIMEOUT);
        return executePost(httpClient, url, paramsObj, null, null, DEFAULT_CHARSET, true,isencrpty);
    }

    /**
     * ִ��HttpPost����
     *
     * @param httpClient      HttpClient�ͻ���ʵ��������null���Զ�����һ��
     * @param url             �����Զ�̵�ַ
     * @param paramsObj       �ύ�Ĳ�����Ϣ��Ŀǰ֧��Map,��String(JSON\xml)
     * @param referer         referer��Ϣ���ɴ�null
     * @param cookie          cookies��Ϣ���ɴ�null
     * @param charset         ������룬Ĭ��UTF8
     * @param closeHttpClient ִ������������Ƿ�ر�HttpClient�ͻ���ʵ��
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     */
    public static HttpResult executePost(CloseableHttpClient httpClient, String url, Object paramsObj,  String referer, String cookie, String charset, boolean closeHttpClient,boolean isencrpty) throws Exception {
        CloseableHttpResponse httpResponse = null;
        try {
            charset = getCharset(charset);
            httpResponse = executePostResponse(httpClient, url, paramsObj, referer, cookie, charset,isencrpty);
            //Http����״̬��
            Integer statusCode = httpResponse.getStatusLine().getStatusCode();
            if(statusCode.equals(HttpStatus.SC_OK)){
                String content = getResult(httpResponse, charset,isencrpty);
                return new HttpResult(statusCode, content);
            }else{
                throw new Exception("�����ַ"+url+"ʧ��,http״̬"+ statusCode+"��ϸ��� "+ httpResponse.getStatusLine().getReasonPhrase());
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
     * @param httpClient HttpClient����
     * @param url        ����������ַ
     * @param paramsObj  ������Ϣ
     * @param referer    ��Դ��ַ
     * @param cookie     cookie��Ϣ
     * @param charset    ͨ�ű���
     * @return CloseableHttpResponse
     * @throws IOException
     */
    private static CloseableHttpResponse executePostResponse(CloseableHttpClient httpClient, String url, Object paramsObj,  String referer, String cookie, String charset,boolean isencrpty) throws Exception {
    	log.info("post����url:" + url);
    	if (httpClient == null) {
            httpClient = createHttpClient();
        }
        HttpPost post = new HttpPost(urlSign(url));
        if (SUserUtil.getCurrentUser() != null) {
            post.setHeader("Authorization", "Bearer " + SUserUtil.getCurrentUser().getToken());
        }
        // ���ò���
        HttpEntity httpEntity = getEntity(paramsObj, charset,isencrpty);
        if (httpEntity != null) {
            post.setEntity(httpEntity);
        }
        return httpClient.execute(post);
    }

    /**
     * ִ���ļ��ϴ�
     *
     * @param remoteFileUrl Զ�̽����ļ��ĵ�ַ
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
     * ִ���ļ��ϴ�
     *
     * @param remoteFileUrl Զ�̽����ļ��ĵ�ַ
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
     * ִ���ļ��ϴ�
     *
     * @param url
     * @param
     * @param localFile
     * @param file_name
     * @param file_bus_type
     * @param file_bus_id
     * @param userid
     * @param charset         ������룬Ĭ��UTF-8
     * @param closeHttpClient ִ������������Ƿ�ر�HttpClient�ͻ���ʵ��
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
            // ���ļ�ת����������FileBody
            FileBody fileBody = new FileBody(localFile);
            //form����
            StringBody file_name_body = new StringBody(URLEncoder.encode(file_name, "UTF-8"), ContentType.APPLICATION_FORM_URLENCODED);
            StringBody file_bus_type_body = new StringBody(file_bus_type, ContentType.APPLICATION_FORM_URLENCODED);
            StringBody file_bus_id_body = new StringBody(file_bus_id, ContentType.APPLICATION_FORM_URLENCODED);
            StringBody userid_body = new StringBody(userid, ContentType.APPLICATION_FORM_URLENCODED);
            // �����������ģʽ���У���ֹ�ļ������롣
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
     * ִ���ļ��ϴ�
     *
     * @param url
     * @param
     * @param localFile
     * @param file_name
     * @param file_bus_type
     * @param file_bus_id
     * @param userid
     * @param charset         ������룬Ĭ��UTF-8
     * @param closeHttpClient ִ������������Ƿ�ر�HttpClient�ͻ���ʵ��
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
            // ���ļ�ת����������FileBody
            FileBody fileBody = new FileBody(localFile);
            //form����
            StringBody file_name_body = new StringBody(URLEncoder.encode(file_name, "UTF-8"), ContentType.APPLICATION_FORM_URLENCODED);
            StringBody file_bus_type_body = new StringBody(file_bus_type, ContentType.APPLICATION_FORM_URLENCODED);
            StringBody file_bus_id_body = new StringBody(file_bus_id, ContentType.APPLICATION_FORM_URLENCODED);
            StringBody userid_body = new StringBody(userid, ContentType.APPLICATION_FORM_URLENCODED);
            if(desc == null){
                desc = "";
            }
            StringBody desc_body = new StringBody(URLEncoder.encode(desc, "UTF-8"), ContentType.APPLICATION_FORM_URLENCODED);
            // �����������ģʽ���У���ֹ�ļ������롣
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
     * ִ���ļ��ϴ�(�Զ���������ʽ)
     *
     * @param httpClient      HttpClient�ͻ���ʵ��������null���Զ�����һ��
     * @param url   Զ�̽����ļ��ĵ�ַ
     * @param localFilePath   �����ļ���ַ
     * @param charset         ������룬Ĭ��UTF-8
     * @param closeHttpClient ִ������������Ƿ�ر�HttpClient�ͻ���ʵ��
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
            // ���ļ�ת����������FileBody
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
     * ִ���ļ�����
     *
     * @param url
     * @param
     * @param localdir ���ش洢�ļ�·��
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
            log.info("get����url:" + url);
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
                // ע�����������OutputStream.write(buff)�Ļ���ͼƬ��ʧ��
            }
            // ���ļ����������
            fout.flush();
            EntityUtils.consume(entity);
            return file;
        } finally {
            // �رյͲ�����
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
     * ��ȡresponse header��Content-Disposition�е�filenameֵ
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
     * ��ȡ����ļ���
     *
     * @return
     */
    public static String getRandomFileName() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * ִ���ļ�����
     *
     * @param httpClient      HttpClient�ͻ���ʵ��������null���Զ�����һ��
     * @param url   Զ�������ļ���ַ
     * @param localFilePath   ���ش洢�ļ���ַ
     * @param charset         ������룬Ĭ��UTF-8
     * @param closeHttpClient ִ������������Ƿ�ر�HttpClient�ͻ���ʵ��
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
            log.info("get����url:" + url);
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
                // ע�����������OutputStream.write(buff)�Ļ���ͼƬ��ʧ��
            }
            // ���ļ����������
            fout.flush();
            EntityUtils.consume(entity);
            return true;
        } finally {
            // �رյͲ�����
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
     * ���ݲ�����ȡ�����Entity
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
            log.info("��ǰδ���������Ϣ���޷�����HttpEntity");
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
     * ����ת����json��ʽ
     *
     * @param t
     * @return
     * @throws Exception
     */
    public static String parseToJson(Object t,boolean isencrpty) throws Exception {
        StringBuffer sb=null;
        JSONObject jsonobject= JSONObject.fromObject(t,jsonConfig);
        sb=new StringBuffer(jsonobject.toString());
        log.info("�������:" +   sb.toString());
        if(isencrpty){
        	 //��ȡ���ܼ�ǩ������
            RSAUtils.EncryptDataStruct encryptDataStruct=RSAUtils.encryptByAesAndRsaPublickey(sb.toString());
            String data=JSONObject.fromObject(encryptDataStruct).toString();
            log.info("�������,���ܺ�:" +data);
            return data;
        }else{
           return sb.toString();
        }
    }
    
   
    /**
     * ����ת����url k=v��ʽ
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
            log.info("�������:" + param);
            return param;
        } else {
            return "";
        }
    }

    /**
     * ����class
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
     * �ӽ���л�ȡ��String����
     *
     * @param httpResponse http�������
     * @param charset      ������Ϣ
     * @param isencrpty    �Ƿ�ӽ���
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
        EntityUtils.consume(entity);// �ر�Ӧ�ùرյ���Դ���ʵ����ͷ���Դ ;Ҳ���԰ѵײ�������ر���
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
     * ת���������
     *
     * @param charset ������Ϣ
     * @return String
     */
    private static String getCharset(String charset) {
        return charset == null ? DEFAULT_CHARSET : charset;
    }

    /**
     * ��map���Ͳ���ת��ΪNameValuePair���Ϸ�ʽ
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
     * ����SSL֧��
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

    // HTTPS��վһ�������ʹ���˰�ȫϵ���ϵ͵�SHA-1ǩ����������������ڵ���SSL֮ǰ��Ҫ��д��֤������ȡ�����SSL��
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
     * Ϊhttpclient����������Ϣ
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
                    // ���������Ϊ���ݵȵģ���ô������
                    // Retry if the request is considered idempotent
                    return true;
                }
                return false;
            }
        };
        httpClientBuilder.setRetryHandler(myRetryHandler);
    }

    /**
     * ִ���ļ��ϴ�
     *
     * @param remoteFileUrl Զ�̽����ļ��ĵ�ַ
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
     * ִ���ļ��ϴ�(�����һ��)
     *
     * @param url
     * @param
     * @param localFile
     * @param file_name
     * @param file_bus_type
     * @param file_bus_id
     * @param userid
     * @param charset         ������룬Ĭ��UTF-8
     * @param closeHttpClient ִ������������Ƿ�ر�HttpClient�ͻ���ʵ��
     * @return
     * @throws IOException
     */
    public static HttpResult executeUploadFileForProvince(String url,  File localFile, String file_name, String file_bus_type, String file_bus_id, String userid,String fileRandomFlag, String charset, boolean closeHttpClient, String desc,boolean isencrpty) throws Exception {
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = createHttpClient();
            HttpPost httpPost = new HttpPost(url);
            // ���ļ�ת����������FileBody
            FileBody fileBody = new FileBody(localFile);
            //form����
            StringBody file_name_body = new StringBody(URLEncoder.encode(file_name, "UTF-8"), ContentType.APPLICATION_FORM_URLENCODED);
            StringBody file_bus_type_body = new StringBody(file_bus_type, ContentType.APPLICATION_FORM_URLENCODED);
            StringBody file_bus_id_body = new StringBody(file_bus_id, ContentType.APPLICATION_FORM_URLENCODED);
            StringBody userid_body = new StringBody(userid, ContentType.APPLICATION_FORM_URLENCODED);
            StringBody fileRandomFlag_body = new StringBody(fileRandomFlag, ContentType.APPLICATION_FORM_URLENCODED);
            if(desc == null){
                desc = "";
            }
            StringBody desc_body = new StringBody(URLEncoder.encode(desc, "UTF-8"), ContentType.APPLICATION_FORM_URLENCODED);
            // �����������ģʽ���У���ֹ�ļ������롣
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
     * �����ַ����ǩ��
     * @param url
     * @return
     */
    public static String urlSign(String url){
        if(url.indexOf("?")!=-1){
             url+= "&"+SignUtils.signature();
        }else{
             url+= "?"+SignUtils.signature();
        }
        log.info("�����ַǩ��:" + url);
        return url;
    }
}
