package com.insigma.mvc;

/**
 * api��ַԼ��
 * @author wengsh
 *
 */
public class UriConstraints {
	
	//api-auth
	private static String API_AUTH="/api-auth";
	
	//api-base
	private static String API_BASE="/api-base";
		
	//��֤
	public static String API_TOKEN=API_AUTH+"/token";
	//��֤ˢ��
	public static String API_REFRESHTOKEN=API_AUTH+"/refreshToken";
    //��ɫ
	public static String API_ROLES=API_AUTH+"/roles";
	//Ȩ��
    public static String API_PERMISSIONS=API_AUTH+"/permissions";
    
    
    // �������͡�����������ȡ���� 
    public static String  API_CODEVALUEBYTYPEANDPARENT= API_BASE+"/codetype/queryCodeValueByCodeTypeAndParent";
    
    // �������ͻ�ȡ�����б�
    public static String  API_INITCODEVALUELIST =API_BASE+ "/codetype/getInitCodeValueList";
    
    // ������
    public static String  API_CODETREE = API_BASE+"/codetype/treedata";
    
    // �ļ�ɾ��
    public static String API_FILE_DELETE=API_BASE+"/api/fileUploadInfo/delete";
    
    //��ȡ�ϴ��ļ���Ϣ
    public static String API_FILE_INFO=API_BASE+"  /api/getFileUploadInfo";
}
