package com.insigma.mvc.serviceimp.common.mobile;

import com.insigma.common.listener.AppConfig;
import com.insigma.resolver.AppException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SmsService {

	Log log= LogFactory.getLog(SmsService.class);

	/**
	 * 短信发送
	 *
	 * 0    没有回复信息
	 * -1	没有该用户账户
	 * -2	接口密钥不正确 [查看密钥]不是账户登陆密码
	 * -21	MD5接口密钥加密不正确
	 * -3	短信数量不足
	 * -11	该用户被禁用
	 * -14	短信内容出现非法字符
	 * -4	手机号格式不正确
	 * -41	手机号码为空
	 * -42	短信内容为空
	 * -51	短信签名格式不正确接口签名格式为：【签名内容】
	 * -6	IP限制
	 * 大于0	短信发送数量
	 *
	 * @param mobile 手机号码
	 * @param cmstext 模板名称
	 * @return 大于0,代表发送成功
	 */
	public String sendSMS(String mobile, String cmstext) throws AppException {
		if(AppConfig.getProperties("mswitch").equals("on")){
			//短信内容
			//cmstext+="【"+AppConfig.getConfig("sitename")+"】";
			try{
				HttpClient client = new HttpClient();
				PostMethod post = new PostMethod(AppConfig.getProperties("mobile_api"));
				// PostMethod post = new PostMethod("http://sms.webchinese.cn/web_api/");
				post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=gbk");// 在头文件中设置转码
				NameValuePair[] data = { new NameValuePair("Uid", AppConfig.getProperties("mobile_uid")),// 注册的用户名
						new NameValuePair("Key", AppConfig.getProperties("mobile_key")),// 注册成功后，登录网站后得到的密钥
						new NameValuePair("smsMob", mobile),// 手机号码
						new NameValuePair("smsText",cmstext) };// 短信内容
				post.setRequestBody(data);
				client.executeMethod(post);
				int statusCode = post.getStatusCode();
				String result = new String(post.getResponseBodyAsString().getBytes("gbk"));
				//System.out.println("手机认证测试:" + result + "如果-3 为短信数量不足");
				post.releaseConnection();
				if(Integer.parseInt(result)<=0){
					throw new AppException("短信发送失败，失败原因代码为："+result);
				}
				log.info("短信发送成功");
				return result;
			}catch (Exception e){
				log.info("短信发送失败");
				throw new AppException(e);
			}
		}else{
			System.out.println("短信接口没有开放，请联系网站管理员!");
			throw new AppException("短信接口没有开放，请联系网站管理员!");
		}
	}

}
