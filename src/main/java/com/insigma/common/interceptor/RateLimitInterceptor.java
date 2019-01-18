package com.insigma.common.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.google.common.util.concurrent.RateLimiter;

/**
 * 基于令牌桶的限流过滤器
 *
 * @author xxx
 */
public class RateLimitInterceptor extends HandlerInterceptorAdapter {

    Log log=LogFactory.getLog(RateLimitInterceptor.class);

    private RateLimiter globalRateLimiter;

    public RateLimitInterceptor(int rate) {
        if (rate > 0)
            globalRateLimiter = RateLimiter.create(rate);
        else
            throw new RuntimeException("rate must greater than zero");
    }

    public void setRate(int rate){
        globalRateLimiter.setRate(rate);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	log.debug(request.getRequestURL().toString());
    	if (!globalRateLimiter.tryAcquire()) {
            log.info(request.getRequestURI()+"请求超过限流器速率");
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }


}
