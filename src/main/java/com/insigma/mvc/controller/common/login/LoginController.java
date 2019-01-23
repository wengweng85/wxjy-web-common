package com.insigma.mvc.controller.common.login;

import com.insigma.common.rsa.RSAUtils;
import com.insigma.common.util.MD5Util;
import com.insigma.dto.AjaxReturnMsg;
import com.insigma.http.HttpRequestUtils;
import com.insigma.mvc.MvcHelper;
import com.insigma.mvc.model.SUser;
import com.insigma.shiro.realm.LoginType;
import com.insigma.shiro.realm.SUserUtil;
import com.insigma.shiro.token.CustomUsernamePasswordToken;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * ��¼controller
 * 
 * @author admin
 *
 */
@Controller
public class LoginController extends MvcHelper {

	Log log = LogFactory.getLog(LoginController.class);

	//http������
	@Resource
	private HttpRequestUtils httpRequestUtils;

	/**
	 * ��ת����¼ҳ��
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/gotologin")
	public ModelAndView gotologin(HttpServletRequest request) throws Exception {
		Subject subject = SecurityUtils.getSubject();
		// �Ƿ��Ѿ���¼-��-ֱ�Ӷ�����ҳ��
		if (subject.isAuthenticated()) {
			ModelAndView modelAndView = new ModelAndView("redirect:/");
			return modelAndView;
		}
		// ��¼ҳ��
		else {
			ModelAndView modelAndView = new ModelAndView("login/login");
			HashMap<String, String> map = new HashMap<String, String>();
			String contextPath = request.getContextPath();
			map.put("contextPath", contextPath);
			// ����rsakey
			RSAUtils.getPublicKeyMap(map);
			modelAndView.addAllObjects(map);
			return modelAndView;
		}
	}

	/**
	 * ��¼
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public AjaxReturnMsg login(HttpServletRequest request, SUser suer) {
		String errorMessage = "";
		String isvercode = suer.getIsvercode() == null ? "1" : suer.getIsvercode();// �Ƿ�У����֤��
		// String isvercode="0";
		Subject subject = SecurityUtils.getSubject();
		boolean rememberMe = WebUtils.isTrue(request,FormAuthenticationFilter.DEFAULT_REMEMBER_ME_PARAM);
		String host = request.getRemoteHost();
		// ʹ��rsa privatekey����
		String password = RSAUtils.decryptStringByJs(suer.getPassword());
		// �����½���ƻ�
		CustomUsernamePasswordToken token = new CustomUsernamePasswordToken(suer.getUsername(), password.toCharArray(), rememberMe, host,suer.getVerifycode(), isvercode,LoginType.PASS);
		try {
			subject.login(token);
			return this.success("��¼�ɹ�");
		} catch (UnknownAccountException uae) {
			errorMessage = "�û��������벻��";
		} catch (IncorrectCredentialsException ice) {
			errorMessage = "�û��������벻��";
		} catch (LockedAccountException lae) {
			errorMessage = "�˻�������.";
		} catch (AuthenticationException e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			token.clear();
		}
		log.error(errorMessage);
		subject.getSession().setAttribute("errorMessage", errorMessage);
		return this.error(errorMessage);
	}


	/**
	 * ��ת��ע��ҳ��
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/register")
	public ModelAndView register(HttpServletRequest request) throws Exception {
		// ע��ҳ��
		ModelAndView modelAndView = new ModelAndView("login/register");
		return modelAndView;
	}

	/**
	 * �˳�
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/loginout")
	public String loginout(HttpServletRequest request) {
		request.getSession().removeAttribute(SUserUtil.SHIRO_CURRENT_USER_INFO);
		Subject user = SecurityUtils.getSubject();
		user.logout();
		return "redirect:/gotologin";
	}

	/**
	 * �����޸�ҳ��
	 * @param request
	 * @return
	 */
	@RequestMapping("/updPassword/view")
	public ModelAndView toUpdPassword(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Subject subject = SecurityUtils.getSubject();
		//�Ƿ��Ѿ���¼-��-ֱ�Ӷ��������޸�ҳ��
		if(subject.isAuthenticated()){
			ModelAndView modelAndView=new ModelAndView("login/updPassword");
			return modelAndView;
		}
		//��¼ҳ��
		else{
			ModelAndView modelAndView=new ModelAndView("login/login");
			HashMap<String, String> map = new HashMap<String, String>();
			String contextPath = request.getContextPath();
			map.put("contextPath", contextPath);
			//����rsakey
			RSAUtils.getPublicKeyMap(map);
			modelAndView.addAllObjects(map);
			return modelAndView;
		}
	}

	/**
	 * �����޸�
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/updPassword/upd", method = RequestMethod.POST)
	@ResponseBody
	public AjaxReturnMsg<String> updPassword(HttpServletRequest request, String password_old, String password_new) throws Exception {
		password_old = MD5Util.MD5Encode(password_old);
		Subject subject = SecurityUtils.getSubject();
		//�Ƿ��Ѿ���¼
		if(!subject.isAuthenticated()){
			return this.error("���¼���ٲ���");
		}else {
			String pwd = SUserUtil.getCurrentUser().getPassword();
			if(!password_old.equals(pwd)) {
				return this.error("ԭ�����������������");
			}else {
				SUser suser = new SUser();
				suser.setUserid(SUserUtil.getCurrentUser().getUserid());
				suser.setPassword(MD5Util.MD5Encode(password_new));
				JSONObject obj = httpRequestUtils.httpPost("/api/webLoginExtra/updPassword", suser);
				if((Boolean) obj.get("success")) {
					SUserUtil.getCurrentUser().setPassword(suser.getPassword());
				}
				return (AjaxReturnMsg) JSONObject.toBean(obj, AjaxReturnMsg.class);
			}
		}
	}
}
