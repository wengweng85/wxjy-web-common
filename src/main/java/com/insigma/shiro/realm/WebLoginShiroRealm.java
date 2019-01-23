package com.insigma.shiro.realm;

import com.insigma.common.util.MD5Util;
import com.insigma.common.util.StringUtil;
import com.insigma.http.HttpRequestUtils;
import com.insigma.mvc.UriConstraints;
import com.insigma.mvc.model.AccessToken;
import com.insigma.mvc.model.SPermission;
import com.insigma.mvc.model.SRole;
import com.insigma.mvc.model.SUser;
import com.insigma.shiro.cache.RedisCache;
import com.insigma.shiro.token.CustomUsernamePasswordToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;


/**
 * 网站使用的web shiro realm
 */
public class WebLoginShiroRealm extends AuthorizingRealm {

	Log log=LogFactory.getLog(WebLoginShiroRealm.class);
	@Autowired
    private RedisCache<String, Set<String>> redisCache;
	//http工具类
	@Resource
	private HttpRequestUtils httpRequestUtils;


	 /**
     * 认证
     */
	@Override
    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
		CustomUsernamePasswordToken customtoken = (CustomUsernamePasswordToken) authcToken;
		SUser suser=null;
		SimpleAuthenticationInfo authenticationInfo=null;
		switch (customtoken.getLoginType().getCode()){
			case "0":
				//是否检验验证码
				if("1".equals(customtoken.getIsvercode())){
					//取得用户输入的校验码
					String userInputValidCode =customtoken.getVerifycode();
					//取得真实的正确校验码
					String realRightValidCode = (String) SecurityUtils.getSubject().getSession().getAttribute("session_validator_code");
					//清除校验码
					SecurityUtils.getSubject().getSession().removeAttribute("session_validator_code");

					if (null == userInputValidCode || !userInputValidCode.equalsIgnoreCase(realRightValidCode)) {
						throw new AuthenticationException("验证码输入不正确");
					}
				}
				//根据用户名获取用户信息
				try {
					HashMap map=new HashMap();
		        	map.put("username", customtoken.getUsername());
					map.put("password", MD5Util.MD5Encode(String.valueOf(customtoken.getPassword())));
					AccessToken accessToken = (AccessToken) httpRequestUtils.httpPostObject(UriConstraints.API_TOKEN,map,AccessToken.class);
					String token= accessToken.getToken();
					/*suser = JWT.unsign(token, SUser.class);
					suser.setToken(token);*/
					suser=new SUser();
					suser.setUsername(accessToken.getUsername()); 
					suser.setToken(accessToken.getToken());
					suser.setName(accessToken.getName());
					authenticationInfo = new SimpleAuthenticationInfo (suser.getUsername(), MD5Util.MD5Encode(String.valueOf(customtoken.getPassword())), getName()); //realm name
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case "1":
				break;
			case "2":
				//根据手机号、验证码登录获取用户信息
				break;
			case "3":
				//根据手机号登录获取用户信息
				break;
			default:
				break;
		}

		//根据用户名获取用户信息
		try {
			HashMap map=new HashMap();
			map.put("username", suser.getUsername());
			//用户权限
			List<SPermission> spermlist=httpRequestUtils.httpPostReturnList(UriConstraints.API_PERMISSIONS ,map, SPermission.class);
			List<SPermission> permlist=SUserUtil.filterPersmList(spermlist);
			setSession(SUserUtil.SHIRO_CURRENT_USER_INFO,suser);
			setSession(SUserUtil.SHIRO_CURRENT_PERM_LIST_INFO,permlist);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	//清理缓存
    	clearCachedAuthorizationInfo(authenticationInfo.getPrincipals());
	    return authenticationInfo;
	}

	/**
	 * 授权
	 */
	@Override
	public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		String username = (String) principals.getPrimaryPrincipal();
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
        redisCache.remove(Constants.getUserPermissionCacheKey(principal));
        redisCache.remove(Constants.getUserRolesCacheKey(principal));
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
