package com.insigma.common.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.insigma.common.util.SUserUtil;
import com.insigma.mvc.model.SUser;

/**
 * 通用登录相关session Interceptor过滤器
 * @author admin
 *
 */
public class SessionInterceptor extends HandlerInterceptorAdapter {

	private static final Log log = LogFactory.getLog(SessionInterceptor.class);

	private NamedThreadLocal<Long> startTimeThreadLocal = new NamedThreadLocal<Long>("StopWatch-StartTime");
	
	private static int OVERTIME = 500;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		long beginTime = System.currentTimeMillis();//1、开始时间
		startTimeThreadLocal.set(beginTime);//线程绑定变量（该数据只有当前请求的线程可见）
		log.debug("-----------------preHandle--------------------");
    	if (handler instanceof HandlerMethod) {
			request.setAttribute("contextpath", request.getContextPath());
			Subject subject = SecurityUtils.getSubject();  
			if(subject.isAuthenticated()){
				//将当前登录信息设置到threadlocal中
				SUserUtil.setCurrentUser ((SUser)subject.getSession().getAttribute(SUserUtil.SHIRO_CURRENT_USER_INFO));  
				return true;
			}
            return true;
        } else {
            return super.preHandle(request, response, handler);
        }
    }

	/**
	 * This implementation is empty.
	 */
	@Override
	public void afterCompletion( HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		long endTime = System.currentTimeMillis();//结束时间
		long beginTime=startTimeThreadLocal.get();//开始时间
		log.debug("-----------------afterCompletion--------------------");
		if((endTime-beginTime)>OVERTIME){
			  log.info("请求地址:"+request.getRequestURL().toString()+" --- 请求耗时比较大,耗时为"+(endTime-beginTime)+"毫秒");
		}
		SUserUtil.remove();
	}
}
