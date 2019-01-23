package com.insigma.resolver;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.insigma.dto.AjaxReturnMsg;

/**
 *  全局异常处理类
 * @author admin
 *
 */
public class MyCustomSimpleMappingExceptionResolver  extends  SimpleMappingExceptionResolver {
	
	Log log=LogFactory.getLog(MyCustomSimpleMappingExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
    	//log.error(e.getMessage());
        e.printStackTrace();
        //判断是不是ajax异步请求
        if (!(request.getHeader("accept").indexOf("application/json") > -1 || (request.getHeader("X-Requested-With")!= null && request.getHeader("X-Requested-With").indexOf("XMLHttpRequest") > -1))) {
            String viewName = determineViewName(e, request);
            if (null != viewName) {// JSP格式返回
            	// 如果不是异步请求
                // Apply HTTP status code for error views, if specified.
                // Only apply it if we're processing a top-level request.
                Integer statusCode = determineStatusCode(request, viewName);
                if (statusCode != null) {
                    applyStatusCodeIfPossible(request, response, statusCode);
                }
                return getModelAndView(viewName, e, request);
            }else{
            	  viewName = "error/500";
            	  return getModelAndView(viewName, e, request);
            }
        } 
       //是ajax请求
        else {
            try {
                PrintWriter writer = response.getWriter();
                AjaxReturnMsg<String> dto = new AjaxReturnMsg<String>();
                dto.setSuccess(false);
                dto.setMessage(e.getMessage());
                writer.write(JSONObject.fromObject(dto).toString());
                writer.flush();
               // writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }
}
