package com.insigma.mvc.controller.common.codevalue;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.insigma.common.listener.AppConfig;
import com.insigma.common.util.CodeValueUtil;
import com.insigma.http.HttpRequestUtils;
import com.insigma.mvc.MvcHelper;
import com.insigma.mvc.UriConstraints;
import com.insigma.mvc.UriConstraintsPxjd;
import com.insigma.mvc.model.CodeValue;
import com.insigma.resolver.AppException;

/**
 * 参数代码
 * @author admin
 */
@Controller
@RequestMapping("/sys/codetype")
public class CodeValueController extends MvcHelper {
	
	 @Resource
	 private HttpRequestUtils httpRequestUtils;
	 
    /**
     * 获取地区信息
     *
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping("/getAreaData")
    public ModelAndView getAreaData(Model model) throws Exception {
        ModelAndView modelAndView = new ModelAndView("/common/codevalue/area_data");
        List<CodeValue> provinces = CodeValueUtil.getCodeListByCodeType("AAB800");
        List<CodeValue> citys = CodeValueUtil.getCodeListByCodeType("AAB801");
        List<CodeValue> three_citys = CodeValueUtil.getCodeListByCodeType("AAB802");
        List<CodeValue> areas = CodeValueUtil.getCodeListByCodeType("AAB301");
        List<Map<String, Object>> area_list = new ArrayList<>();
        List<Map<String, Object>> area_son_list;
        Map<String, Object> hm_father;
        Map<String, Object> hm_son;

        // 获取省级及其下属地区
        for (CodeValue province : provinces) {
            area_son_list = new ArrayList<>();
            hm_father = new HashMap<>();
            if (citys != null) {
                for (CodeValue city : citys) {
                    if (province.getCode_value().equals(city.getPar_code_value())) {
                        hm_son = new HashMap<>();
                        hm_son.put("son", city.getCode_value());
                        area_son_list.add(hm_son);
                    }
                }
            }
            if (area_son_list.size() > 0) {
                hm_father.put("father", province.getCode_value());
                hm_father.put("son_list", area_son_list);
                area_list.add(hm_father);
            }
        }

        //获取市级及其下属地区
        for (CodeValue city : citys) {
            area_son_list = new ArrayList<>();
            hm_father = new HashMap<>();
            if (three_citys != null) {
                for (CodeValue three_city : three_citys) {
                    if (city.getCode_value().equals(three_city.getPar_code_value())) {
                        hm_son = new HashMap<>();
                        hm_son.put("son", three_city.getCode_value());
                        area_son_list.add(hm_son);
                    }
                }
            }
            if (area_son_list.size() > 0) {
                hm_father.put("father", city.getCode_value());
                hm_father.put("son_list", area_son_list);
                area_list.add(hm_father);
            }
        }

        //本地城市编码
        String localcity = AppConfig.getProperties("localcity");
        model.addAttribute("localcity", localcity);

        //全部地区
        model.addAttribute("areas", areas);
        //地区及其下属地区
        model.addAttribute("area_list", area_list);
        //省级地区
        model.addAttribute("provinces", provinces);
        return modelAndView;
    }


    /**
     * 获取工种信息
     *
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping("/getJobData")
    public ModelAndView getJobData(Model model) throws Exception {
        ModelAndView modelAndView = new ModelAndView("/common/codevalue/job_data");

        List<CodeValue> job_one_list = CodeValueUtil.getCodeListByCodeType("AAB803");
        List<CodeValue> job_two_list = CodeValueUtil.getCodeListByCodeType("AAB804");
        List<CodeValue> job_three_list = CodeValueUtil.getCodeListByCodeType("AAB805");

        List<CodeValue> jobs = CodeValueUtil.getCodeListByCodeType("ACA112");
        List<Map<String, Object>> job_list = new ArrayList<>();
        List<Map<String, Object>> job_son_list;
        Map<String, Object> hm_father;
        Map<String, Object> hm_son;
        //获取一级工种及其下级
        for (CodeValue job_one : job_one_list) {
            job_son_list = new ArrayList<>();
            hm_father = new HashMap<>();
            for (CodeValue job_two : job_two_list) {
                if (job_one.getCode_value().equals(job_two.getPar_code_value())) {
                    hm_son = new HashMap<>();
                    hm_son.put("son", job_two.getCode_value());
                    job_son_list.add(hm_son);
                }
            }
            if (job_son_list.size() > 0) {
                hm_father.put("father", job_one.getCode_value());
                hm_father.put("son_list", job_son_list);
                job_list.add(hm_father);
            }
        }
        //获取二级工种及其下级
        for (CodeValue job_two : job_two_list) {
            job_son_list = new ArrayList<>();
            hm_father = new HashMap<>();
            for (CodeValue job_three : job_three_list) {
                if (job_two.getCode_value().equals(job_three.getPar_code_value())) {
                    hm_son = new HashMap<>();
                    hm_son.put("son", job_three.getCode_value());
                    job_son_list.add(hm_son);
                }
            }
            if (job_son_list.size() > 0) {
                hm_father.put("father", job_two.getCode_value());
                hm_father.put("son_list", job_son_list);
                job_list.add(hm_father);
            }
        }

        //全部工种
        model.addAttribute("jobs", jobs);
        //工种及其下级
        model.addAttribute("job_list", job_list);
        //一级工种
        model.addAttribute("job_one_list", job_one_list);
        return modelAndView;
    }


    /**
     * 获取行业信息
     *
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping("/getIndustryData")
    public ModelAndView getIndustryData(Model model) throws Exception {
        ModelAndView modelAndView = new ModelAndView("/common/codevalue/industry_data");

        List<CodeValue> industry_one_list = CodeValueUtil.getCodeListByCodeType("AAB807");
        List<CodeValue> industry_two_list = CodeValueUtil.getCodeListByCodeType("AAB808");

        List<CodeValue> industrys = CodeValueUtil.getCodeListByCodeType("AAA115");
        List<Map<String, Object>> industry_list = new ArrayList<>();
        List<Map<String, Object>> industry_son_list;
        Map<String, Object> hm_father;
        Map<String, Object> hm_son;

        //获取一级行业及其下级
        for (CodeValue industry_one : industry_one_list) {
            industry_son_list = new ArrayList<>();
            hm_father = new HashMap<>();
            for (CodeValue industry_two : industry_two_list) {
                if (industry_one.getCode_value().equals(industry_two.getPar_code_value())) {
                    hm_son = new HashMap<>();
                    hm_son.put("son", industry_two.getCode_value());
                    industry_son_list.add(hm_son);
                }
            }
            if (industry_son_list.size() > 0) {
                hm_father.put("father", industry_one.getCode_value());
                hm_father.put("son_list", industry_son_list);
                industry_list.add(hm_father);
            }
        }

        //全部行业
        model.addAttribute("industrys", industrys);
        //行业及其下级
        model.addAttribute("industry_list", industry_list);
        //一级行业
        model.addAttribute("industry_one_list", industry_one_list);
        return modelAndView;
    }


    /**
     * 获取专业信息
     *
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping("/getMajorData")
    public ModelAndView getMajorData(Model model) throws Exception {
        ModelAndView modelAndView = new ModelAndView("/common/codevalue/major_data");
        List<CodeValue> major_one_list = CodeValueUtil.getCodeListByCodeType("AAB809");
        List<CodeValue> major_two_list = CodeValueUtil.getCodeListByCodeType("AAB810");
        List<CodeValue> major_three_list = CodeValueUtil.getCodeListByCodeType("AAB811");
        List<CodeValue> majors = CodeValueUtil.getCodeListByCodeType("AAC183");
        List<Map<String, Object>> major_list = new ArrayList<>();
        List<Map<String, Object>> major_son_list;
        Map<String, Object> hm_father;
        Map<String, Object> hm_son;

        //获取一级专业及其下级
        for (CodeValue major_one : major_one_list) {
            major_son_list = new ArrayList<>();
            hm_father = new HashMap<>();
            for (CodeValue major_two : major_two_list) {
                if (major_one.getCode_value().equals(major_two.getPar_code_value())) {
                    hm_son = new HashMap<>();
                    hm_son.put("son", major_two.getCode_value());
                    major_son_list.add(hm_son);
                }
            }
            if (major_son_list.size() > 0) {
                hm_father.put("father", major_one.getCode_value());
                hm_father.put("son_list", major_son_list);
                major_list.add(hm_father);
            }
        }
        //获取二级专业及其下级
        for (CodeValue major_two : major_two_list) {
            major_son_list = new ArrayList<>();
            hm_father = new HashMap<>();
            for (CodeValue major_three : major_three_list) {
                if (major_two.getCode_value().equals(major_three.getPar_code_value())) {
                    hm_son = new HashMap<>();
                    hm_son.put("son", major_three.getCode_value());
                    major_son_list.add(hm_son);
                }
            }
            if (major_son_list.size() > 0) {
                hm_father.put("father", major_two.getCode_value());
                hm_father.put("son_list", major_son_list);
                major_list.add(hm_father);
            }
        }

        //全部专业
        model.addAttribute("majors", majors);
        //专业及其下级
        model.addAttribute("major_list", major_list);
        //一级专业
        model.addAttribute("major_one_list", major_one_list);
        return modelAndView;
    }
    
    /**
	 * 跳转到代码搜索页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws com.insigma.resolver.AppException
	 */
	@RequestMapping(value = "/toCodeValuesuggest")
	public ModelAndView toCodeValuesuggest(HttpServletRequest request, HttpServletResponse response,CodeValue codevalue) throws Exception {
		String callback_fun_name=request.getParameter("callback_fun_name");
		String codetype=request.getParameter("codetype");
		String select_val_name=URLDecoder.decode(request.getParameter("select_val_name"),"utf-8"); 
		ModelAndView modelAndView=new ModelAndView("common/codevalue/codeValueSelect");
        modelAndView.addObject("callback_fun_name", callback_fun_name);
        modelAndView.addObject("codetype", codetype);
        modelAndView.addObject("select_val_name", select_val_name);
        return modelAndView;
	}
	
	 /**
	  * 通过代码类型、过滤条件获取代码 
	  * @param request
	  * @param response
	  * @param codevalue
	  * @return
	  * @throws AppException
	  */
	 @RequestMapping(value = "/queryByCodeTypeAndParent")
	 @ResponseBody
	 public List<CodeValue> queryByCodeTypeAndParent(HttpServletRequest request, HttpServletResponse response,CodeValue codevalue) throws AppException {
		   List<CodeValue> list = httpRequestUtils.httpPostReturnList(UriConstraints.API_CODEVALUEBYTYPEANDPARENT, codevalue);
		   return list;
	 }
	
	 /**
	  * 通过代码类型、过滤条件获取代码 
	  * @param request
	  * @param response
	  * @param codevalue
	  * @return
	  * @throws AppException
	  */
	 @RequestMapping(value = "/getCodeValueList")
	 @ResponseBody
	 public List<CodeValue> getCodeValueList(HttpServletRequest request, HttpServletResponse response,CodeValue codevalue) throws AppException {
			List<CodeValue>  list =(List<CodeValue> )httpRequestUtils.httpPostReturnList(UriConstraintsPxjd.API_INITCODEVALUELIST,codevalue);
			return list;
	 }
	 
	 /**
	  * 通过代码类型获取参数列表
	  * @param request
	  * @param response
	  * @param codevalue
	  * @return
	  * @throws AppException
	  */
	 @RequestMapping(value = "/getCodeValueListFromZuul")
	 @ResponseBody
	 public List<CodeValue> getCodeValueListFromZuul(HttpServletRequest request, HttpServletResponse response,CodeValue codevalue) throws AppException {
		   List<CodeValue> list = httpRequestUtils.httpPostReturnList(UriConstraints.API_INITCODEVALUELIST, codevalue);
		   return list;
	 }
		
		
		/**
		 * codevalue 代码树
		 * 
		 * @param request
		 * @param response
		 * @return
		 * @throws com.insigma.resolver.AppException
		 */
		@RequestMapping(value = "/treedata/{code_type}")
		@ResponseBody
		public List<CodeValue> treedata(HttpServletRequest request, HttpServletResponse response,@PathVariable String code_type) throws AppException {
			String id=request.getParameter("id");
			if(StringUtils.isEmpty(id)){
				id="610000";
			}
			CodeValue codevalue=new CodeValue();
			codevalue.setSub_code_value(id);
			codevalue.setCode_type(code_type.toUpperCase());
			List<CodeValue> list = httpRequestUtils.httpPostReturnList(UriConstraints.API_CODETREE, codevalue);
			return list;
		}
}
