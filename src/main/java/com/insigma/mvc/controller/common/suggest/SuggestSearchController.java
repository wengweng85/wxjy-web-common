package com.insigma.mvc.controller.common.suggest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.insigma.http.HttpRequestUtils;
import com.insigma.mvc.MvcHelper;
import com.insigma.mvc.model.SysSuggestKey;
import com.insigma.resolver.AppException;

/**
 * 建议搜索controller
 */
@Controller
@RequestMapping(value = "/common/suggest")
public class SuggestSearchController extends MvcHelper<SysSuggestKey> {


	 @Resource
	 private HttpRequestUtils httpRequestUtils;
    
    /** 
	 * 通过搜索关键字
	 * @param request
	 * @param response
	 * @return
	 * @throws com.insigma.resolver.AppException
	 */
	@RequestMapping(value = "/searchcode")
	@ResponseBody
	public HashMap<String,List<SysSuggestKey>> searchcodebykey(HttpServletRequest request, HttpServletResponse response, SysSuggestKey key) throws AppException {
		String url = "/api/common/suggest/searchcode";
		try{
			key.setKeyword(URLDecoder.decode(key.getKeyword(),"utf-8"));
		}catch( UnsupportedEncodingException ex ){
			ex.printStackTrace();
		}
		List<SysSuggestKey> list = httpRequestUtils.httpPostReturnList(url, key);
		HashMap map=new HashMap();
		map.put("value", list);
		return map;
	}

}
