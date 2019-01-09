package com.insigma.shiro.credentials;

import com.insigma.shiro.realm.LoginType;
import com.insigma.shiro.token.CustomUsernamePasswordToken;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;

public class MyHashedCredentialsMatcher extends HashedCredentialsMatcher {

	@Override
	public boolean doCredentialsMatch(AuthenticationToken token,AuthenticationInfo info) {
		CustomUsernamePasswordToken authtoken = (CustomUsernamePasswordToken) token;
		if (authtoken.getLoginType().equals(LoginType.NO_PASS)) {
			return true;
		} 
	    return super.doCredentialsMatch(token, info);
	}
}
