package com.insigma.mvc.serviceimp.common.mobile;

import com.insigma.common.listener.AppConfig;
import com.insigma.resolver.AppException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ���ŷ��ͷ���
 * @author admin
 */
public class SmsService {

	Log log= LogFactory.getLog(SmsService.class);

	/**
	 * ���ŷ���
	 *
	 * 0    û�лظ���Ϣ
	 * -1	û�и��û��˻�
	 * -2	�ӿ���Կ����ȷ [�鿴��Կ]�����˻���½����
	 * -21	MD5�ӿ���Կ���ܲ���ȷ
	 * -3	������������
	 * -11	���û�������
	 * -14	�������ݳ��ַǷ��ַ�
	 * -4	�ֻ��Ÿ�ʽ����ȷ
	 * -41	�ֻ�����Ϊ��
	 * -42	��������Ϊ��
	 * -51	����ǩ����ʽ����ȷ�ӿ�ǩ����ʽΪ����ǩ�����ݡ�
	 * -6	IP����
	 * ����0	���ŷ�������
	 *
	 * @param mobile �ֻ�����
	 * @param cmstext ģ������
	 * @return ����0,�����ͳɹ�
	 */
	public String sendSMS(String mobile, String cmstext) throws AppException {
		if(AppConfig.getProperties("mswitch").equals("on")){
			//��������
			//cmstext+="��"+AppConfig.getConfig("sitename")+"��";
			try{
				HttpClient client = new HttpClient();
				PostMethod post = new PostMethod(AppConfig.getProperties("mobile_api"));
				// PostMethod post = new PostMethod("http://sms.webchinese.cn/web_api/");
				post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=gbk");// ��ͷ�ļ�������ת��
				NameValuePair[] data = { new NameValuePair("Uid", AppConfig.getProperties("mobile_uid")),// ע����û���
						new NameValuePair("Key", AppConfig.getProperties("mobile_key")),// ע��ɹ��󣬵�¼��վ��õ�����Կ
						new NameValuePair("smsMob", mobile),// �ֻ�����
						new NameValuePair("smsText",cmstext) };// ��������
				post.setRequestBody(data);
				client.executeMethod(post);
				int statusCode = post.getStatusCode();
				String result = new String(post.getResponseBodyAsString().getBytes("gbk"));
				//System.out.println("�ֻ���֤����:" + result + "���-3 Ϊ������������");
				post.releaseConnection();
				if(Integer.parseInt(result)<=0){
					throw new AppException("���ŷ���ʧ�ܣ�ʧ��ԭ�����Ϊ��"+result);
				}
				log.info("���ŷ��ͳɹ�");
				return result;
			}catch (Exception e){
				log.info("���ŷ���ʧ��");
				throw new AppException(e);
			}
		}else{
			System.out.println("���Žӿ�û�п��ţ�����ϵ��վ����Ա!");
			throw new AppException("���Žӿ�û�п��ţ�����ϵ��վ����Ա!");
		}
	}

}
