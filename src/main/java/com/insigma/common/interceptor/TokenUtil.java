package com.insigma.common.interceptor;

import java.util.UUID;


/**
 * token工具类
 * @author xxx
 *
 */
public class TokenUtil {
	
	/**
	 * 创建token
	 * @return
	 */
	public static String createToken(){
		return UUID.randomUUID().toString();
	}
}