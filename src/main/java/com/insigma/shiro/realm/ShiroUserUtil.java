package com.insigma.shiro.realm;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;

import com.insigma.mvc.model.SUser;

public class ShiroUserUtil {
	
	/**
     * ���µ�ǰ�û�Ȩ��,ϵͳ�ڶ��û�����Ȩ���޸ĺ����ͨ�����ô˷�ʽ��̬�޸ĵ�ǰ�û�Ȩ��
     */
   public static void updateCurrentUserPerms(SUser suser){
		Subject subject = SecurityUtils.getSubject(); 
		RealmSecurityManager rsm = (RealmSecurityManager) SecurityUtils.getSecurityManager();  
		WebLoginShiroRealm shiroRealm = (WebLoginShiroRealm)rsm.getRealms().iterator().next();  
		String realmName = subject.getPrincipals().getRealmNames().iterator().next(); 
		//��һ������Ϊ�û���,�ڶ�������ΪrealmName
		SimplePrincipalCollection principals = new SimplePrincipalCollection(suser.getUsername(),realmName);
		subject.runAs(principals); 
		shiroRealm.getAuthorizationCache().remove(subject.getPrincipals()); 
		subject.releaseRunAs();
   }

}
