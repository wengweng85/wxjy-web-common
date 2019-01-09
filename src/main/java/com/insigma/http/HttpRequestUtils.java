package com.insigma.http;

import com.github.pagehelper.PageInfo;
import com.insigma.dto.SysCode;
import com.insigma.json.JsonDateValueProcessor;
import com.insigma.resolver.AppException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * httprequest工具类
 *
 * @param <T>
 * @author xxx
 */
public class HttpRequestUtils<T> {


	private static Logger logger = LoggerFactory.getLogger(HttpRequestUtils.class);
	
    public JsonConfig jsonConfig;
    
    private String gateway_base_url;

    //是否加解密
    private boolean isencrpty;
    
    public HttpRequestUtils(String gateway_base_url,boolean isencrpty) {
        jsonConfig = new JsonConfig();
        jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
        this.gateway_base_url = gateway_base_url;
        this.isencrpty=isencrpty;
    }

    /**
     * 对象转换成jsonobject
     *
     * @param t
     * @return
     */
    private JSONObject toJsonObject(T t) {
        JSONObject jsonParam = JSONObject.fromObject(t, jsonConfig);
        return jsonParam;
    }

    /**
     * jsonobject转换成对象
     *
     * @param t
     * @return
     */
    private T tobean(JSONObject jsonobject, T t) {
        return (T) JSONObject.toBean(jsonobject, t.getClass());
    }

    /**
     * jsonobject转换成对象数组
     *
     * @param t
     * @return
     */
    public List<T> toList(JSONArray jsonarray, T t) {
        return (List<T>) JSONArray.toList(jsonarray, t.getClass());
    }

    /**
     * jsonobject转换成对象数组
     *
     * @return
     */
    public List<T> toList(JSONArray jsonarray, Class c) {
        return (List<T>) JSONArray.toList(jsonarray, c);
    }

    /**
     * 发送get请求 返回json数组
     *
     * @param url 路径
     * @param map   请求数据对象
     * @return
     */
    public JSONArray httpPostReturnArray(String url, HashMap map) throws AppException {
        return httpPost(url, map).getJSONArray("obj");
    }

    /**
     * 发送get请求 返回json数组
     *
     * @param url 路径
     * @param t   请求数据对象
     * @return
     */
    public JSONArray httpPostReturnArray(String url, T t) throws AppException {
        return httpPost(url, t).getJSONArray("obj");
    }

    /**
     *
     * @param url
     * @param t
     * @return
     * @throws AppException
     */
    public PageInfo<T> httpPostReturnPage(String url, T t) throws AppException {
        JSONObject jsonObject = httpPost(url, t);
        PageInfo<T> pageInfo = (PageInfo<T>) JSONObject.toBean(jsonObject.getJSONObject("obj"), PageInfo.class);
        pageInfo.setList(toList(jsonObject.getJSONObject("obj").getJSONArray("list"), t.getClass()));
        return pageInfo;
    }

    /**
     *
     * @param url
     * @param t
     * @param c
     * @return
     * @throws AppException
     */
    public PageInfo<T> httpPostReturnPage(String url, T t, Class c) throws AppException {
        JSONObject jsonObject = httpPost(url, t);
        PageInfo<T> pageInfo = (PageInfo<T>) JSONObject.toBean(jsonObject.getJSONObject("obj"), PageInfo.class);
        pageInfo.setList(toList(jsonObject.getJSONObject("obj").getJSONArray("list"), c));
        return pageInfo;
    }

    /**
     * 发送post请求 返回json对象
     *
     * @param url 路径
     * @return
     */
    public JSONObject httpPostReturnObject(String url) throws AppException {
        return httpPost(url, new HashMap()).getJSONObject("obj");
    }

    /**
     * 发送post请求 返回json对象
     *
     * @param url 路径
     * @param t   请求数据对象
     * @return
     */
    public JSONObject httpPostReturnObject(String url, T t) throws AppException {
        return httpPost(url, t).getJSONObject("obj");
    }

    /**
     * 返回对象list
     *
     * @param url
     * @param t
     * @return
     * @throws AppException
     */
    public List<T> httpPostReturnList(String url, T t) throws AppException {
        return toList(httpPostReturnArray(url, t), t);
    }

    /**
     * 返回对象list
     *
     * @param url
     * @return
     * @throws AppException
     */
    public List<T> httpPostReturnList(String url, Class c) throws AppException {
        return toList(httpPostReturnArray(url, new HashMap()), c);
    }

    /**
     * 返回对象list
     *
     * @param url
     * @return
     * @throws AppException
     */
    public List<T> httpPostReturnList(String url, HashMap map, Class c) throws AppException {
        return toList(httpPostReturnArray(url, map), c);
    }

    /**
     * 发送get请求 返回json对象
     *
     * @param url 路径
     * @return
     */
    public T httpPostObject(String url, T t, Class c) throws AppException {
        return (T) JSONObject.toBean(httpPost(url, t).getJSONObject("obj"), c);
    }


    /**
     * post请求
     *
     * @param url
     * @return
     * @throws AppException
     */
    public JSONObject httpPost(String url) throws AppException {
        try {
            HttpResult httpresult = HttpHelper.executePost(gateway_base_url + url, new HashMap(), isencrpty);
            return parseHttpResult(httpresult, url);
        } catch (Exception e) {
            throw new AppException(e);
        }
    }


    /**
     * post请求
     *
     * @param url 地址
     * @param map 对象
     * @return
     * @throws AppException
     */
    public JSONObject httpPost(String url, HashMap map) throws AppException {
        try {
            HttpResult httpresult = HttpHelper.executePost(gateway_base_url + url, map,isencrpty);
            return parseHttpResult(httpresult, url);
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    /**
     * post请求
     *
     * @param url 地址
     * @param t   对象
     * @return
     * @throws AppException
     */
    public JSONObject httpPost(String url, T t) throws AppException {
        try {
            HttpResult httpresult = HttpHelper.executePost(gateway_base_url + url, t,isencrpty);
            return parseHttpResult(httpresult, url);
        } catch (Exception e) {
            throw new AppException(e);
        }
    }


    /**
     * 发送文件
     *
     * @param url
     * @return
     * @throws AppException
     */
    public JSONObject httpUploadFile(String url, File file, String file_name, String file_bus_type, String file_bus_id, String userid) throws AppException {
        try {
            HttpResult httpresult = HttpHelper.executeUploadFile(gateway_base_url + url, file, file_name, file_bus_type, file_bus_id, userid, isencrpty);
            return parseHttpResult(httpresult, url);
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    /**
     * 发送文件（附加文件描述）
     *
     * @param url
     * @param file
     * @param file_name
     * @param file_bus_type
     * @param file_bus_id
     * @param userid
     * @param desc
     * @return
     * @throws AppException
     */
    public JSONObject httpUploadFile(String url, File file, String file_name, String file_bus_type, String file_bus_id, String userid, String desc) throws Exception {
        try {
            HttpResult httpresult = HttpHelper.executeUploadFile(gateway_base_url + url,  file, file_name, file_bus_type, file_bus_id, userid, desc, isencrpty);
            return parseHttpResult(httpresult, url);
        } catch (IOException e) {
            throw new AppException(e);
        }
    }

    //最多跑一次文件上传专用
    public JSONObject httpUploadFile_ForProvince(String url, File file, String file_name, String file_bus_type, String file_bus_id, String userid, String desc) throws Exception {
        try {
            HttpResult httpresult = HttpHelper.executeUploadFile(gateway_base_url + url,  file, file_name, file_bus_type, file_bus_id, userid, desc, isencrpty);
            return parseHttpResult(httpresult, url);
        } catch (IOException e) {
            throw new AppException(e);
        }
    }


    /**
     * 解析返回的httpresult
     *
     * @param httpresult
     * @param url
     * @throws AppException
     */
    private JSONObject parseHttpResult(HttpResult httpresult, String url) throws AppException {
        if (httpresult.getStatusCode() == HttpStatus.SC_OK) {
        	JSONObject jsonResult = JSONObject.fromObject(httpresult.getContent());
            /**是否成功*/
            int syscode = jsonResult.getInt ("syscode");
            if (!(syscode==SysCode.SYS_CODE_200.getCode())) {
               //如果发现token为空或者token失效,重新登录
            	if (syscode==SysCode.SYS_TOKEN_EMPTY.getCode() || syscode==SysCode.SYS_TOKEN_ERROR.getCode()) {
                	try{
                		Subject subject = SecurityUtils.getSubject(); 
                		if(subject!=null){
                    		if(subject.isAuthenticated()){
                    			subject.logout();
                    		}
                    	}
                	}catch(Exception e){
                		  e.printStackTrace();
                	}
                    throw new AppException("接口(" + url + ") 调用失败:状态码" + syscode + "，token为空或超时,请登录");
                } else {
                    throw new AppException("接口(" + url + ") 调用失败:状态码" + syscode);
                }
            }
            return jsonResult;
        } else {
        	 logger.info(httpresult.getStatusCode()+":"+httpresult.getContent());
             throw new AppException("接口(" + url + ") 调用失败,http状态码:" + httpresult.getStatusCode());
        }
    }

    //最多跑一次文件上传专用
    public JSONObject httpUploadFile_ForProvince(String url, File file, String file_name, String file_bus_type, String file_bus_id, String userid,String fileRandomFlag, String desc) throws Exception {
        try {
            HttpResult httpresult = HttpHelper.executeUploadFileProvince(gateway_base_url + url,  file, file_name, file_bus_type, file_bus_id, userid,fileRandomFlag,desc,isencrpty);
            return parseHttpResult(httpresult, url);
        } catch (IOException e) {
            throw new AppException(e);
        }
    }
  
    /**
     * 文件下载
     *
     * @param url
     * @return
     * @throws AppException
     */
    public File httpDownLoadFile(String url, String localdir) throws AppException {
        File file;
        try {
            file = HttpHelper.executeDownloadFile(gateway_base_url + url,  localdir);
        } catch (IOException e) {
            throw new AppException(e);
        }
        return file;
    }
}
