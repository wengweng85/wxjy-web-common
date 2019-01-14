package com.insigma.shiro.realm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cas.CasRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.Saml11TicketValidator;
import org.jasig.cas.client.validation.TicketValidator;

import com.insigma.common.util.SUserUtil;
import com.insigma.common.util.StringUtil;
import com.insigma.http.HttpRequestUtils;
import com.insigma.mvc.UriConstraints;
import com.insigma.mvc.model.AccessToken;
import com.insigma.mvc.model.SPermission;
import com.insigma.mvc.model.SRole;
import com.insigma.mvc.model.SUser;

public class MyCasRealm extends CasRealm {

	Log log=LogFactory.getLog(WebLoginShiroRealm.class);
	//http������
	@Resource
	private HttpRequestUtils httpRequestUtils;


	 /**
     * ��֤
     */
	@Override
    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
		//���ø���ķ�����Ȼ����Ȩ�û�
		AuthenticationInfo authc = super.doGetAuthenticationInfo(authcToken);
		//��ȡ�û���
		String username = (String) authc.getPrincipals().getPrimaryPrincipal();
		//�����û�����ȡ�û���Ϣ
		try {
				 HashMap map=new HashMap();
	        	 map.put("username", username);
				 AccessToken accessToken = (AccessToken) httpRequestUtils.httpPostObject(UriConstraints.API_TOKEN,map,AccessToken.class);
				 SUser suser=new SUser();
				 suser.setUsername(accessToken.getUsername()); 
				 suser.setToken(accessToken.getToken());
				 suser.setName(accessToken.getName());
		
				 map=new HashMap();
				 map.put("username", username);
				 //�û�Ȩ��
				 List<SPermission> spermlist=httpRequestUtils.httpPostReturnList(UriConstraints.API_PERMISSIONS ,map, SPermission.class);
				 List<SPermission> permlist=SUserUtil.filterPersmList(spermlist);
				 setSession(SUserUtil.SHIRO_CURRENT_USER_INFO,suser);
				 setSession(SUserUtil.SHIRO_CURRENT_PERM_LIST_INFO,permlist);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return authc;
	}

	/**
	 * ��Ȩ
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
				//�û���ɫ
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

				//�û�Ȩ��
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
	 * ������ redis
	 * @param principal
	 */
    public void clearCachedAuthorizationInfo_rediscache(String principal) {
        System.out.println("�����û���Ȩ��Ϣ����");
    	SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
        super.clearCachedAuthorizationInfo(principals);
        super.clearCache(principals);
        super.clearCachedAuthenticationInfo(principals);
    }

	/** 
     * ��һЩ���ݷŵ�ShiroSession��,�Ա��������ط�ʹ�� 
     * ����Controller,ʹ��ʱֱ����HttpSession.getAttribute(key)�Ϳ���ȡ��
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
