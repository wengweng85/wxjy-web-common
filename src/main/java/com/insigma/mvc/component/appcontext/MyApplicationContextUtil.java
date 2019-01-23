package com.insigma.mvc.component.appcontext;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * ��ȡspringbean������
 *
 * @author admin
 */
@Component
public class MyApplicationContextUtil implements ApplicationContextAware {

	/**����һ����̬��������*/
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
     * �Ӿ�̬����ApplicationContext��ȡ��Bean, �Զ�ת��Ϊ����ֵ���������.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        checkApplicationContext();
        return (T) context.getBean(name);
    }
 
    /**
     * �Ӿ�̬����ApplicationContext��ȡ��Bean, �Զ�ת��Ϊ����ֵ���������.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        checkApplicationContext();
        return (T) context.getBeansOfType(clazz);
    }
 
    /**
     * ���applicationContext��̬����.
     */
    public static void cleanApplicationContext() {
    	context = null;
    }
 
    private static void checkApplicationContext() {
        if (context == null) {
            throw new IllegalStateException("applicaitonContextδע��,����applicationContext.xml�ж���SpringContextHolder");
        }
    }

}
