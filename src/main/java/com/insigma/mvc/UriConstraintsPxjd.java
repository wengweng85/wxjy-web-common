package com.insigma.mvc;

/**
 * api��ַԼ��
 * @author admin
 *
 */
public class UriConstraintsPxjd {
	
	//api-auth
	private static String API_AUTH="";
	//��¼
	public static String API_LOGIN=API_AUTH+"/token";
    //��ɫ
	public static String API_ROLES=API_AUTH+"/roles";
	//Ȩ��
    public static String API_PERMISSIONS=API_AUTH+"/permissions";
    
    // �������ͻ�ȡ�����б�
    public static String  API_INITCODEVALUELIST = "/codetype/getInitCodeValueListByFilter";
}
