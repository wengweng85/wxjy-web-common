package com.insigma.common.interceptor;

import java.util.UUID;


/**
 * token������
 * @author admin
 *
 */
public class TokenUtil {
	
	/**
	 * ����token
	 * @return
	 */
	public static String createToken(){
		return UUID.randomUUID().toString();
	}
}