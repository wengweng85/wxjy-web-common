package com.insigma.shiro.realm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cas.CasAuthenticationException;
import org.apache.shiro.cas.CasRealm;
import org.apache.shiro.cas.CasToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.util.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.Saml11TicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;

import com.insigma.common.util.SUserUtil;
import com.insigma.common.util.StringUtil;
import com.insigma.http.HttpRequestUtils;
import com.insigma.mvc.UriConstraints;
import com.insigma.mvc.model.SPermission;
import com.insigma.mvc.model.SRole;
import com.insigma.mvc.model.SUser;
import com.insigma.resolver.AppException;

public class MyCasRealm extends CasRealm {

	Log log=LogFactory.getLog(WebLoginShiroRealm.class);
	//http工具类
	@Resource
	private HttpRequestUtils httpRequestUtils;


	 /**
     * 认证
     */
	@Override
    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
		//调用父类的方法，然后授权用户
		CasToken casToken = (CasToken)authcToken;
		if(casToken==null){
			return null;
		}
		String ticket=(String)casToken.getCredentials();
		log.info("ticket->"+ticket);
		if(!StringUtils.hasText(ticket)){
			return null;
		}
		TicketValidator ticketValidator = ensureTicketValidator();
		try {
        	// contact CAS server to validate service ticket
        	Assertion casAssertion = ticketValidator.validate(ticket, getCasService());
        	// get principal, user id and attributes
        	AttributePrincipal casPrincipal = casAssertion.getPrincipal();
        	String userId = casPrincipal.getName();
 
        	Map<String, Object> attributes = casPrincipal.getAttributes();
        	// refresh authentication token (user id + remember me)
        	casToken.setUserId(userId);
        	String rememberMeAttributeName = getRememberMeAttributeName();
        	String rememberMeStringValue = (String)attributes.get(rememberMeAttributeName);
        	boolean isRemembered = rememberMeStringValue != null && Boolean.parseBoolean(rememberMeStringValue);
        	if (isRemembered) {
            	casToken.setRememberMe(true);
        	}
        	// create simple authentication info
        	List<Object> principals = CollectionUtils.asList(userId, attributes);
        	PrincipalCollection principalCollection = new SimplePrincipalCollection(principals, getName());
        	
        	SUser suser=new SUser();
			suser.setUsername(userId); 
			suser.setToken((String)attributes.get("token"));
			suser.setName(userId);
	
			HashMap  map=new HashMap();
			map.put("username", userId);
			//用户权限
			List<SPermission> spermlist=httpRequestUtils.httpPostReturnList(UriConstraints.API_PERMISSIONS ,map, SPermission.class);
			List<SPermission> permlist=SUserUtil.filterPersmList(spermlist);
			setSession(SUserUtil.SHIRO_CURRENT_USER_INFO,suser);
			setSession(SUserUtil.SHIRO_CURRENT_PERM_LIST_INFO,permlist);
        	return new SimpleAuthenticationInfo(principalCollection, ticket);
    	} catch (TicketValidationException e) { 
        	throw new CasAuthenticationException("Unable to validate ticket [" + ticket + "]", e);
    	} catch(AppException ex){
    		throw new CasAuthenticationException(ex);
    	}
	}

	/**
	 * 授权
	 */
	@Override
	public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		String username = (String) principals.getPrimaryPrincipal();
		System.out.println("username->"+username);
		try{
			if (StringUtil.isNotEmpty(username)) {
				HashMap map=new HashMap();
				map.put("username", username);
				SimpleAuthorizationInfo authenticationInfo = new SimpleAuthorizationInfo();
				//用户角色
				List<SRole> rolelist=  httpRequestUtils.httpPostReturnList(UriConstraints.API_ROLES,map, SRole.class);
				if(rolelist!=null){
					Set<String> roleset=new HashSet<String>();
					Iterator iterator_role=rolelist.iterator();
					while(iterator_role.hasNext()){
						SRole  srole=(SRole) iterator_role.next();
						roleset.add(srole.getRolecode());
					}
					authenticationInfo.setRoles(roleset);
				}

				//用户权限
				List<SPermission> permlist=  httpRequestUtils.httpPostReturnList(UriConstraints.API_PERMISSIONS,map, SPermission.class);
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
	 * 清理缓存
	 * @param principal
	 */
    public void clearCachedAuthorizationInfo(String principal) {
        System.out.println("更新用户授权信息缓存");
    	SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
        super.clearCachedAuthorizationInfo(principals);
        super.clearCache(principals);
        super.clearCachedAuthenticationInfo(principals);
    }
    
	/**
	 * 清理缓存 redis
	 * @param principal
	 */
    public void clearCachedAuthorizationInfo_rediscache(String principal) {
        System.out.println("更新用户授权信息缓存");
    	SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
        super.clearCachedAuthorizationInfo(principals);
        super.clearCache(principals);
        super.clearCachedAuthenticationInfo(principals);
    }

	/** 
     * 将一些数据放到ShiroSession中,以便于其它地方使用 
     * 比如Controller,使用时直接用HttpSession.getAttribute(key)就可以取到
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
    
    @Override
    protected TicketValidator createTicketValidator() {
        String urlPrefix = this.getCasServerUrlPrefix();
        TicketValidator ticketValidator = null;
        if("saml".equalsIgnoreCase(this.getValidationProtocol())){
            Saml11TicketValidator saml11TicketValidator = new Saml11TicketValidator(urlPrefix);
            saml11TicketValidator.setEncoding("utf-8");
            ticketValidator = saml11TicketValidator;
        }else {
            Cas20ServiceTicketValidator cas20ServiceTicketValidator = new Cas20ServiceTicketValidator(urlPrefix);
            cas20ServiceTicketValidator.setEncoding("utf-8");
            ticketValidator = cas20ServiceTicketValidator;
        }
        return ticketValidator;
    }
}
