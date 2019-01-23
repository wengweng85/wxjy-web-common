package com.insigma.shiro.realm;

import com.insigma.http.HttpRequestUtils;
import com.insigma.mvc.UriConstraints;
import com.insigma.mvc.model.SPermission;
import com.insigma.mvc.model.SUser;
import com.insigma.resolver.AppException;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
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
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 自定义shiro realm 基于casrealm
 */
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
		log.debug("ticket->"+ticket);
		if(!StringUtils.hasText(ticket)){
			return null;
		}
		TicketValidator ticketValidator = ensureTicketValidator();
		try {
        	// contact CAS server to validate service ticket
        	Assertion casAssertion = ticketValidator.validate(ticket, getCasService());
        	// get principal, user id and attributes
        	AttributePrincipal casPrincipal = casAssertion.getPrincipal();
        	log.debug("casPrincipal.getName()->"+casPrincipal.getName());
        	
        	//解析json
        	JSONObject jsonobject = JSONObject.fromObject(casPrincipal.getName()) ;
        	String userid=jsonobject.getString("userid");
            String token=jsonobject.getString("token");
            String name=jsonobject.getString("name");
            String username=jsonobject.getString("username");
            String usertype= jsonobject.getString("usertype"); 
            
        	Map<String, Object> attributes = casPrincipal.getAttributes();
        	// refresh authentication token (user id + remember me)
        	casToken.setUserId(userid);
        	String rememberMeAttributeName = getRememberMeAttributeName();
        	String rememberMeStringValue = (String)attributes.get(rememberMeAttributeName);
        	boolean isRemembered = rememberMeStringValue != null && Boolean.parseBoolean(rememberMeStringValue);
        	if (isRemembered) {
            	casToken.setRememberMe(true);
        	}
        	// create simple authentication info
        	List<Object> principals = CollectionUtils.asList(userid, attributes);
        	PrincipalCollection principalCollection = new SimplePrincipalCollection(principals, getName());
        	
        	SUser suser=new SUser();
			suser.setUsername(username); 
			suser.setToken(token);
			suser.setName(name);
			suser.setUsertype(usertype);
	
			HashMap  map=new HashMap();
			map.put("username", username);
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
		return null;
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
}
