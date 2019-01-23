package com.insigma.shiro.token;

import org.apache.shiro.authc.UsernamePasswordToken;

import com.insigma.shiro.realm.LoginType;

/**
 * CustomUsernamePasswordToken
 */
public class CustomUsernamePasswordToken extends UsernamePasswordToken {

    /**
     *
     */
    private static final long serialVersionUID = 6730381322353623113L;

    //用于存储用户输入的校验码
    private String verifycode;

    //是否验证校验码
    private String isvercode;

    //登录类型
    private LoginType loginType;

    public CustomUsernamePasswordToken(String loginname, char[] password,boolean rememberMe, String host, String verifycode,String isvercode) {
        //调用父类的构造函数
        super(loginname,password,rememberMe,host);
        this.verifycode=verifycode;
        this.isvercode=isvercode;
        this.loginType=LoginType.PASS;
    }

    public CustomUsernamePasswordToken(String loginname, char[] password,boolean rememberMe, String host, String verifycode,String isvercode,LoginType loginType) {
        //调用父类的构造函数
        super(loginname,password,rememberMe,host);
        this.verifycode=verifycode;
        this.isvercode=isvercode;
        this.loginType=loginType;
    }


    public LoginType getLoginType() {
        return loginType;
    }


    public void setLoginType(LoginType loginType) {
        this.loginType = loginType;
    }



    public String getVerifycode() {
        return verifycode;
    }

    public void setVerifycode(String verifycode) {
        this.verifycode = verifycode;
    }

    public String getIsvercode() {
        return isvercode;
    }

    public void setIsvercode(String isvercode) {
        this.isvercode = isvercode;
    }
}
