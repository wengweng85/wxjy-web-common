package com.insigma.mvc.controller.common.token;

import com.insigma.common.util.EhCacheUtil;
import com.insigma.mvc.MvcHelper;
import com.insigma.resolver.AppException;
import net.sf.ehcache.Element;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

/**
 * CsrfTokenController
 * 过滤csrf
 * @author  admin
 */
@Controller
public class CsrfTokenController extends MvcHelper {

	/**
	 * 生成token
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/token")
	public void token(HttpServletRequest request, HttpServletResponse response) throws AppException {
		try {
			String token = UUID.randomUUID().toString();
			request.getSession().setAttribute("csrftoken",token);

	        /*Vector<String> csrftokenlist = (Vector<String>)request.getSession().getAttribute("csrftokenlist");
	        if(csrftokenlist == null){
	        	csrftokenlist = new Vector<String>();
	        }
	        csrftokenlist.add(token);
	        request.getSession().setAttribute("csrftokenlist",csrftokenlist);*/
			EhCacheUtil.getManager().getCache("tokencache").put(new Element(token,""));
	        //System.out.println("缓存数："+EhCacheUtil.getManager().getCache("tokencache").getSize());
			
			PrintWriter out = response.getWriter();
			out.print(token);
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new AppException(e);
		}
	}
}