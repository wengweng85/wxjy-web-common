package com.insigma.shiro.realm;


/**
 *  ��¼����ö��
 * @author wengsh
 *
 */
public enum LoginType {
	/**�����¼*/
	PASS("0","�����¼"),
	/**�������¼*/
	NO_PASS("1","�������¼"),
	/**�ֻ���֤���¼*/
	PHONE_CAPTCHA("2","�ֻ���֤���¼"),
	/**�ֻ��ŵ�¼*/
	PHONE("3","�ֻ��ŵ�¼");

	
	private LoginType(String code, String name){
		this.code=code;
		this.name=name;
	}
	
	private String code;
	private String name;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
