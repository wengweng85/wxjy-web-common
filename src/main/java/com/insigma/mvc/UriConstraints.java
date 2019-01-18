package com.insigma.mvc;

/**
 * api地址约束
 * @author wengsh
 *
 */
public class UriConstraints {
	
	//api-auth
	private static String API_AUTH="/api-auth";
	
	//api-base
	private static String API_BASE="/api-base";
		
	//认证
	public static String API_TOKEN=API_AUTH+"/token";
	//认证刷新
	public static String API_REFRESHTOKEN=API_AUTH+"/refreshToken";
    //角色
	public static String API_ROLES=API_AUTH+"/roles";
	//权限
    public static String API_PERMISSIONS=API_AUTH+"/permissions";
    
    
    // 代码类型、过滤条件获取代码 
    public static String  API_CODEVALUEBYTYPEANDPARENT= API_BASE+"/codetype/queryCodeValueByCodeTypeAndParent";
    
    // 代码类型获取参数列表
    public static String  API_INITCODEVALUELIST =API_BASE+ "/codetype/getInitCodeValueList";
    
    // 代码数
    public static String  API_CODETREE = API_BASE+"/codetype/treedata";
    
    // 文件删除
    public static String API_FILE_DELETE=API_BASE+"/api/fileUploadInfo/delete";
    
    //获取上传文件信息
    public static String API_FILE_INFO=API_BASE+"  /api/getFileUploadInfo";
}
