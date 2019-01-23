package com.insigma.token;

import com.insigma.http.HttpRequestUtils;
import com.insigma.mvc.UriConstraints;
import com.insigma.mvc.component.appcontext.MyApplicationContextUtil;
import com.insigma.mvc.model.AccessToken;
import com.insigma.shiro.realm.SUserUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;


/**
 * ����Thread�࣬ˢ��jwt token
 * @author admin
 *
 */
public class TokenThread implements Runnable { 

  private static Log log = LogFactory.getLog(TokenThread.class);

  @Override
  public void run() {  
      while (true) {  
           try {  
        	  if (null != SUserUtil.getCurrentUser()) {
                  String token = SUserUtil.getCurrentUser().getToken(); //��ȡ��ǰ��¼��Աtoken�Լ���Ч��
                  long expires=SUserUtil.getCurrentUser().getExpires();
                  log.info("��Чʱ��"+expires+"�� token:"+token);
                  Date now=new Date(System.currentTimeMillis());
                  Date expirationd_date=new Date();
                  expirationd_date.setTime(expires);
                  long next_pull_time= (expirationd_date.getTime()-now.getTime())/1000;
                  log.info("now="+now.toLocaleString());
                  log.info("expirationd_date"+expirationd_date.toLocaleString());
                  log.info(next_pull_time);
                  //�������ʱ��С��200��
                  if(next_pull_time<200){
                      log.debug("next_pull_time="+next_pull_time);
                      //ˢ��token
                      AccessToken accessToken=new AccessToken();
                      accessToken.setToken(token);
                      final HttpRequestUtils httpRequestUtils = MyApplicationContextUtil.getContext().getBean(HttpRequestUtils.class);
                      AccessToken refreshAccessToken = (AccessToken) httpRequestUtils.httpPostObject(UriConstraints.API_REFRESHTOKEN,accessToken,AccessToken.class);
                      String refreshToken= refreshAccessToken.getToken();
                      log.debug("refreshToken="+refreshToken);
                      SUserUtil.getCurrentUser().setToken(refreshToken);
                  }else{
                      Thread.sleep(60 * 1000);
                  }
              } else {
                  Thread.sleep(60 * 1000);
              }  
          } catch (Exception e) {  
              try {  
                  Thread.sleep(60 * 1000);  
              } catch (InterruptedException e1) {  
                  log.error("{}", e1);  
              }  
              log.error("{}", e);  
          } 
      }  
  }  
}
