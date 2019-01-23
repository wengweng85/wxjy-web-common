package com.insigma.shiro.realm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.pagehelper.StringUtil;
import com.insigma.common.util.MD5Util;
import com.insigma.http.HttpRequestUtils;
import com.insigma.mvc.model.SPermission;
import com.insigma.mvc.model.SRole;
import com.insigma.mvc.model.SUser;
import com.insigma.shiro.cache.RedisCache;
import com.insigma.shiro.token.CustomUsernamePasswordToken;

public class WebLoginShiroRealmForPxjd extends AuthorizingRealm  {
	
	//http������
	@Resource
	private HttpRequestUtils httpRequestUtils;
	
	
	@Autowired
    private RedisCache<String, Set<String>> redisCache;
	

	 /**
     * ��֤
     */
	@Override
	public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException,UnknownAccountException,LockedAccountException {
		CustomUsernamePasswordToken token = (CustomUsernamePasswordToken) authcToken;
		SimpleAuthenticationInfo authenticationInfo=null;
		//�Ƿ������֤��
		if(token.getIsvercode().equals("1")){
	        //ȡ���û������У����
	        String userInputValidCode =token.getVerifycode();

	        //ȡ����ʵ����ȷУ����
	        String realRightValidCode = (String) SecurityUtils.getSubject().getSession().getAttribute("session_validator_code");
	        
	        //���У����
	        SecurityUtils.getSubject().getSession().removeAttribute("session_validator_code");
	        
	        if (null == userInputValidCode || !userInputValidCode.equalsIgnoreCase(realRightValidCode)) {
	            throw new AuthenticationException("��֤�벻��ȷ");
	        }
		}

		try {	
			// ���ýӿ�
			String url =  "/getUserAndGroupInfo/"+ token.getUsername()+"/"+MD5Util.MD5Encode(String.valueOf(token.getPassword()));
			SUser suser =(SUser)httpRequestUtils.httpGetObject(url,SUser.class);
			if (suser == null) {
	            throw new UnknownAccountException();//û�ҵ��ʺ�
	        }
	
	        if (suser.getEnabled().equals("0") ) {
	            throw new LockedAccountException(); //�ʺ�����
	        }
	    	authenticationInfo = new SimpleAuthenticationInfo(
	    	suser.getUsername(), 
	    	suser.getPassword(), 
	        getName() ); //realm name
	    	setSession(SUserUtil.SHIRO_CURRENT_USER_INFO,suser);
	    	SUserUtil.setCurrentUser(suser);
	    	
	    	//�û�Ȩ��
			List<SPermission> spermlist=SUserUtil.filterPersmList(httpRequestUtils.httpGetReturnList("/api/findPermissionStr/"+token.getUsername(), SPermission.class));
	    	setSession(SUserUtil.SHIRO_CURRENT_PERM_LIST_INFO,spermlist);
	        //������
	    	clearCachedAuthorizationInfo(authenticationInfo.getPrincipals());
	        return authenticationInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * ��Ȩ
	 */
	@Override
	public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		String loginname = (String) principals.getPrimaryPrincipal();
		try{
			if (StringUtil.isNotEmpty(loginname)) {
	            SimpleAuthorizationInfo authenticationInfo = new SimpleAuthorizationInfo();
 	           //�û���ɫ
 	           List<SRole> rolelist=  httpRequestUtils.httpGetReturnList("/api/findRolesStr/"+loginname, SRole.class);
 	            if(rolelist!=null){
 	            	Set<String> roleset=new HashSet<String>();
 	 	            Iterator iterator_role=rolelist.iterator();
 	 	            while(iterator_role.hasNext()){
 	 	            	SRole  srole=(SRole) iterator_role.next();
 	 	            	roleset.add(srole.getRolecode());
 		            }
 	 	           authenticationInfo.setRoles(roleset);
 	            }
 	           
 	            //�û�Ȩ��
	            List<SPermission> permlist=  httpRequestUtils.httpGetReturnList("/api/findPermissionStr/"+loginname, SPermission.class);
	            if(permlist!=null){
	            	Set<String> set=new HashSet<String>();
	  	            Iterator iterator=permlist.iterator();
	  	            while(iterator.hasNext()){
	  	            	SPermission  spermission=(SPermission) iterator.next();
	  	            	set.add(spermission.getPermcode());
	  	            }
	   	            authenticationInfo.setStringPermissions(set);
	            }
	            return authenticationInfo;
	        }else{
	            return null;
	        }
		}catch(Exception e){
			e.printStackTrace();
		}
        return null;
	}
	
	
	/**
	 * ������
	 * @param principal
	 */
    public void clearCachedAuthorizationInfo(String principal) {
        System.out.println("�����û���Ȩ��Ϣ����");
    	SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
        super.clearCachedAuthorizationInfo(principals);
        super.clearCache(principals);
        super.clearCachedAuthenticationInfo(principals);
    }
    


	/** 
     * ��һЩ���ݷŵ�ShiroSession��,�Ա��������ط�ʹ�� 
     * @key  ����Controller,ʹ��ʱֱ����HttpSession.getAttribute(key)�Ϳ���ȡ��
     */  
    private void setSession(Object key, Object value){  
        Subject subject = SecurityUtils.getSubject();  
        if(null != subject){  
            Session session = subject.getSession();  
            if(null != session){  
                session.setAttribute(key, value);  
            }  
        }  
    }

}
