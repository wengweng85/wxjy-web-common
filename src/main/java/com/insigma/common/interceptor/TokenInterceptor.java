package com.insigma.common.interceptor;

import com.insigma.common.annotation.AddToken;
import com.insigma.common.annotation.ValidateToken;
import com.insigma.common.filter.CSRFTokenManager;
import com.insigma.common.util.EhCacheUtil;
import com.insigma.resolver.AppException;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * �ظ��ύtoken������
 *
 * @author xxx
 */
public class TokenInterceptor extends HandlerInterceptorAdapter {

    private Log log = LogFactory.getLog(TokenInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            AddToken addtoken = method.getAnnotation(AddToken.class);
            ValidateToken validatetoken = method.getAnnotation(ValidateToken.class);
            if (addtoken != null) {
                setToken(request, response);
            }
            if (validatetoken != null) {
                if (validToken(request)) {
                    return false;
                }
                CSRFTokenManager.removeTokenFromRequest(request, response);
            }
            return true;
        }
        return super.preHandle(request, response, handler);
    }

    /**
     * ��������
     *
     * @param request
     */
    public void setToken(HttpServletRequest request, HttpServletResponse response) throws AppException {
        String token = TokenUtil.createToken();
        request.setAttribute("csrf", token);
        request.getSession().setAttribute("csrftoken", token);
        /*Vector<String> csrftokenlist = (Vector<String>)request.getSession().getAttribute("csrftokenlist");
        if(csrftokenlist == null){
        	csrftokenlist = new Vector<String>();
        }
        csrftokenlist.add(token);
        request.getSession().setAttribute("csrftokenlist",csrftokenlist);*/
        EhCacheUtil.getManager().getCache("tokencache").put(new Element(token, ""));
    }


    /**
     * ��������
     *
     * @param request
     * @return true ���ظ��ύ false �����ظ��ύ
     * @throws AppException
     */
    public boolean validToken(HttpServletRequest request) throws AppException {
        //Vector<String> csrftokenlist = (Vector<String>)request.getSession().getAttribute("csrftokenlist");
        String clientToken = CSRFTokenManager.getTokenFromRequest(request);
        if (null == clientToken) {
            String info = "��ǰ����Ϊ�ظ��ύ����,ԭ��ͻ���tokenΪ��:clientToken:" + clientToken;
            log.error(info);
            return true;
        }
        /*if (null==csrftokenlist) {
            String info="��ǰ����Ϊ�ظ��ύ����,ԭ������tokenΪ��:serverToken:''   clientToken:"+clientToken;
        	log.error(info);
        	return true;
        }*/
        Element element = EhCacheUtil.getManager().getCache("tokencache").get(clientToken);
        if (element != null) {
            EhCacheUtil.getManager().getCache("tokencache").remove(clientToken);
            System.out.println("ʣ�໺������" + EhCacheUtil.getManager().getCache("tokencache").getSize());
            return false;
        }
        String info = "��ǰ����Ϊ�ظ��ύ����,ԭ��ͻ���������token��һ��: clientToken:" + clientToken;
        log.error(info);
        return true;
    	
        /*for (int i = 0; i < csrftokenlist.size(); i++) {
			if (csrftokenlist.get(i).toString().equals(clientToken)){
				csrftokenlist.remove(i);
		        request.getSession().setAttribute("csrftokenList",csrftokenlist);
				return false;
			}
		}
        String info="��ǰ����Ϊ�ظ��ύ����,ԭ��ͻ���������token��һ��: clientToken:"+clientToken;
    	log.error(info);
    	return true;*/
        
    	
        /*String serverToken=(String)request.getSession().getAttribute("csrftoken");
        String clientToken=CSRFTokenManager.getTokenFromRequest(request);
        if (null==clientToken){
        	String info="��ǰ����Ϊ�ظ��ύ����,ԭ��ͻ���tokenΪ��:serverToken:"+serverToken+"  clientToken:"+clientToken;
        	log.error(info);
        	return true;
        }
        if (null==serverToken) {
        	String info="��ǰ����Ϊ�ظ��ύ����,ԭ������tokenΪ��:serverToken:"+serverToken+"   clientToken:"+clientToken;
        	log.error(info);
        	return true;
        }
        if (!serverToken.equals(clientToken)) {
        	String info="��ǰ����Ϊ�ظ��ύ����,ԭ��ͻ���������token��һ��:serverToken:"+serverToken+"   clientToken:"+clientToken;
        	log.error(info);
        	return true;
        }
       
        return false;*/
    }
}
