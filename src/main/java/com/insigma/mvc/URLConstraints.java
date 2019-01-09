package com.insigma.mvc;

/**
 * api地址约束
 * @author wengsh
 *
 */
public class URLConstraints {
	
	//api-auth
	private static String API_AUTH="/api-auth";
	//认证
	public static String API_TOKEN=API_AUTH+"/token";
	//认证刷新
	public static String API_REFRESHTOKEN=API_AUTH+"/refreshToken";
    //角色
	public static String API_ROLES=API_AUTH+"/roles";
	//权限
    public static String API_PERMISSIONS=API_AUTH+"/permissions";
}
