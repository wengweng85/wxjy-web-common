package com.insigma.mvc.component.appcontext;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 获取springbean工具类
 *
 * @author admin
 */
@Component
public class MyApplicationContextUtil implements ApplicationContextAware {

	/**声明一个静态变量保存*/
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext contex) throws BeansException {
        context = contex;
    }

    public static ApplicationContext getContext() {
    	 checkApplicationContext();
        return context;
    }

    public static Object getBean(ServletContext servletContext, Class beanname) {
        WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        Object bean = wac.getBean(beanname);
        return bean;
    }
    
    /**
     * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        checkApplicationContext();
        return (T) context.getBean(name);
    }
 
    /**
     * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        checkApplicationContext();
        return (T) context.getBeansOfType(clazz);
    }
 
    /**
     * 清除applicationContext静态变量.
     */
    public static void cleanApplicationContext() {
    	context = null;
    }
 
    private static void checkApplicationContext() {
        if (context == null) {
            throw new IllegalStateException("applicaitonContext未注入,请在applicationContext.xml中定义SpringContextHolder");
        }
    }

}
