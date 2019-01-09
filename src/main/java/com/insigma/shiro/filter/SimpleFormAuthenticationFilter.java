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
 * �Զ���authcУ����
 * @author xxx
 *
 */
public class SimpleFormAuthenticationFilter extends FormAuthenticationFilter {

	@Override
    protected boolean onAccessDenied(ServletRequest servletrequest, ServletResponse servletresponse) throws Exception {
		  
        HttpServletRequest request = (HttpServletRequest) servletrequest;  
        HttpServletResponse response = (HttpServletResponse) servletresponse;  
  
        Subject subject = getSubject(request, response);  
  
        //���û�е�¼
        if (subject.getPrincipal() == null) {  
        	//�����ajax����
        	if (request.getHeader("accept").indexOf("application/json") > -1 || (request.getHeader("X-Requested-With")!= null && request.getHeader("X-Requested-With").indexOf("XMLHttpRequest") > -1)) { 
        		PrintWriter writer = response.getWriter();
                AjaxReturnMsg dto = new AjaxReturnMsg();
                Map<String, Object> map=new HashMap(4);
                //����δ��¼���¼ʱ�����,�����µ�¼!
                map.put("statuscode", "session expired");
                map.put("redirecturl", "/plogin/to");
                dto.setObj(map);
                writer.write(JSONObject.fromObject(dto).toString());
                writer.flush();
            } else {  
                saveRequestAndRedirectToLogin(request, response);  
            } 
        }else{
        	//�����ajax����
        	if (request.getHeader("accept").indexOf("application/json") > -1 || (request.getHeader("X-Requested-With")!= null && request.getHeader("X-Requested-With").indexOf("XMLHttpRequest") > -1)) { 
        		PrintWriter writer = response.getWriter();
                AjaxReturnMsg dto = new AjaxReturnMsg();
                Map<String, Object> map=new HashMap(4);
                map.put("statuscode", "unauthorized");//��û���㹻��Ȩ��ִ�иò���
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
