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
     * 发送邮件
     * @param toEmail 邮件地址
     * @param subject 邮件主题
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
            SimpleMailSender.sendHtmlMail(mailSenderInfo);//发送html格式
            log.info("邮件发送成功");
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败");
            throw new AppException(e);
        }

    }

    /**
     * 发送邮件带附件
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
            SimpleMailSender.sendHtmlMail(mailSenderInfo);//发送html格式
            log.info("邮件发送成功");
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败");
            throw new AppException(e);
        }
    }
}
