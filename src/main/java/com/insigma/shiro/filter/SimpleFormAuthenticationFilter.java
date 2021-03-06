package com.insigma.shiro.filter;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import com.insigma.dto.AjaxReturnMsg;


/**
 * 自定义authc校验器
 * @author xxx
 *
 */
public class SimpleFormAuthenticationFilter extends FormAuthenticationFilter {

	@Override
    protected boolean onAccessDenied(ServletRequest servletrequest, ServletResponse servletresponse) throws Exception {
		  
        HttpServletRequest request = (HttpServletRequest) servletrequest;  
        HttpServletResponse response = (HttpServletResponse) servletresponse;  
  
        Subject subject = getSubject(request, response);  
  
        //如果没有登录
        if (subject.getPrincipal() == null) {  
        	//如果是ajax请求
        	if (request.getHeader("accept").indexOf("application/json") > -1 || (request.getHeader("X-Requested-With")!= null && request.getHeader("X-Requested-With").indexOf("XMLHttpRequest") > -1)) { 
        		PrintWriter writer = response.getWriter();
                AjaxReturnMsg dto = new AjaxReturnMsg();
                Map<String, Object> map=new HashMap(4);
                //您尚未登录或登录时间过长,请重新登录!
                map.put("statuscode", "session expired");
                map.put("redirecturl", "/plogin/to");
                dto.setObj(map);
                writer.write(JSONObject.fromObject(dto).toString());
                writer.flush();
            } else {  
                saveRequestAndRedirectToLogin(request, response);  
            } 
        }else{
        	//如果是ajax请求
        	if (request.getHeader("accept").indexOf("application/json") > -1 || (request.getHeader("X-Requested-With")!= null && request.getHeader("X-Requested-With").indexOf("XMLHttpRequest") > -1)) { 
        		PrintWriter writer = response.getWriter();
                AjaxReturnMsg dto = new AjaxReturnMsg();
                Map<String, Object> map=new HashMap(4);
                map.put("statuscode", "unauthorized");//您没有足够的权限执行该操作
                map.put("redirecturl", "/");
                dto.setObj(map);
                writer.write(JSONObject.fromObject(dto).toString());
                writer.flush();
            } else {  
                saveRequestAndRedirectToLogin(request, response);  
            } 
        }
        return false;
    } 
}
