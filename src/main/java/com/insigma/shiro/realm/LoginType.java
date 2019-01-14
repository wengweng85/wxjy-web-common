package com.insigma.shiro.realm;


/**
 *  登录类型枚举
 * @author wengsh
 *
 */
public enum LoginType {
	/**密码登录*/
	PASS("0","密码登录"),
	/**免密码登录*/
	NO_PASS("1","免密码登录"),
	/**手机验证码登录*/
	PHONE_CAPTCHA("2","手机验证码登录"),
	/**手机号登录*/
	PHONE("3","手机号登录");

	
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
