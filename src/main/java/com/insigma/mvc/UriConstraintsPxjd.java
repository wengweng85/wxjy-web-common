package com.insigma.mvc;

/**
 * api地址约束
 * @author admin
 *
 */
public class UriConstraintsPxjd {
	
	//api-auth
	private static String API_AUTH="";
	//登录
	public static String API_LOGIN=API_AUTH+"/token";
    //角色
	public static String API_ROLES=API_AUTH+"/roles";
	//权限
    public static String API_PERMISSIONS=API_AUTH+"/permissions";
    
    // 代码类型获取参数列表
    public static String  API_INITCODEVALUELIST = "/codetype/getInitCodeValueListByFilter";
}
