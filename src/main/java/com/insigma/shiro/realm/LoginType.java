package com.insigma.shiro.realm;

public enum LoginType {
	PASS("0","ÃÜÂëµÇÂ¼"),
	NO_PASS("1","ÃâÃÜÂëµÇÂ¼"),
	PHONE_CAPTCHA("2","ÊÖ»úÑéÖ¤ÂëµÇÂ¼"),
	PHONE("3","ÊÖ»úºÅµÇÂ¼");

	
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
