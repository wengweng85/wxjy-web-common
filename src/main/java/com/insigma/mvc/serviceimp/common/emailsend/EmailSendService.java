package com.insigma.mvc.serviceimp.common.emailsend;


import com.insigma.common.email.MailSenderInfo;
import com.insigma.common.email.SimpleMailSender;
import com.insigma.common.listener.AppConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.insigma.resolver.AppException;

/**
 * Created by xxx on 2015-01-11.
 */
public class EmailSendService {

    Log log= LogFactory.getLog(EmailSendService.class);


    /**
     * �����ʼ�
     * @param toEmail �ʼ���ַ
     * @param subject �ʼ�����
     * @return
     */
    public boolean sendMail(String toEmail,String subject,String mail_context) throws AppException{
        try {
            MailSenderInfo mailSenderInfo=new MailSenderInfo();
            mailSenderInfo.setMailServerHost(AppConfig.getProperties("email_host"));
            mailSenderInfo.setValidate(true);
            mailSenderInfo.setUserName(AppConfig.getProperties("email_username"));
            mailSenderInfo.setPassword(AppConfig.getProperties("email_password"));
            mailSenderInfo.setFromAddress(AppConfig.getProperties("email_username"));
            mailSenderInfo.setToAddress(toEmail);
            mailSenderInfo.setSubject(subject);
            mailSenderInfo.setContent(mail_context);
            SimpleMailSender.sendHtmlMail(mailSenderInfo);//����html��ʽ
            log.info("�ʼ����ͳɹ�");
            return true;
        } catch (Exception e) {
            log.error("�ʼ�����ʧ��");
            throw new AppException(e);
        }

    }

    /**
     * �����ʼ�������
     * @param toEmail
     * @param subject
     * @param mail_context
     * @param files
     * @return
     */
    public boolean sendMail(String toEmail,String subject,String mail_context,String[] files) throws AppException{
        try {
            MailSenderInfo mailSenderInfo=new MailSenderInfo();
            mailSenderInfo.setMailServerHost(AppConfig.getProperties("email_host"));
            mailSenderInfo.setValidate(true);
            mailSenderInfo.setUserName(AppConfig.getProperties("email_username"));
            mailSenderInfo.setPassword(AppConfig.getProperties("email_password"));
            mailSenderInfo.setFromAddress(AppConfig.getProperties("email_username"));
            mailSenderInfo.setToAddress(toEmail);
            mailSenderInfo.setSubject(subject);
            mailSenderInfo.setContent(mail_context);
            mailSenderInfo.setAttachFileNames(files);
            SimpleMailSender.sendHtmlMail(mailSenderInfo);//����html��ʽ
            log.info("�ʼ����ͳɹ�");
            return true;
        } catch (Exception e) {
            log.error("�ʼ�����ʧ��");
            throw new AppException(e);
        }
    }
}
