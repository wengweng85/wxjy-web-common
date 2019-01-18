package com.insigma.mvc.controller.common.item;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.insigma.http.HttpRequestUtils;
import com.insigma.mvc.MvcHelper;
import com.insigma.mvc.model.ItemMaterial;
import com.insigma.resolver.AppException;

/**
 *   事项附件参数
 */
@Controller
public class ItemController extends MvcHelper {

	@Resource
	private HttpRequestUtils httpRequestUtils;

	/**
	 * 事项附件参数列表
	 * 
	 * @param request
	 * @param response
	 * @param item_id
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/sys/itemmaterial")
	@ResponseBody
	public List<ItemMaterial> treedata(HttpServletRequest request, HttpServletResponse response, @PathVariable String item_id) throws AppException {
		String url = "/api/itemmaterial";
		ItemMaterial itemMaterial = new ItemMaterial();
		itemMaterial.setItem_id(item_id);
		List<ItemMaterial> list = httpRequestUtils.httpPostReturnList(url, itemMaterial);
		return list;
	}
}
