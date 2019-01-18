package com.insigma.mvc.controller.common.fileupload;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.insigma.common.util.SUserUtil;
import com.insigma.dto.AjaxReturnMsg;
import com.insigma.http.FileUploadUtils;
import com.insigma.http.HttpRequestUtils;
import com.insigma.mvc.MvcHelper;
import com.insigma.mvc.UriConstraints;
import com.insigma.mvc.model.SUser;
import com.insigma.mvc.model.SuploadFile;

/**
 * FileUploadController
 */
@Controller
@RequestMapping(value = "/common")
public class FileUploadController extends MvcHelper {

    @Resource
    private HttpRequestUtils httpRequestUtils;
    
    //�ļ��ϴ�������
    @Resource
    private FileUploadUtils fileUploadUtils;
    
    /**
     * �ϴ��ļ�
     *
     * @param
     */
    @RequestMapping("/uploadFiles/{type}/{businessType}/{fileRandomFlag}")
    public String upload(@PathVariable String type,@PathVariable String businessType,@PathVariable String fileRandomFlag, ModelMap modelMap){
        modelMap.put("fileStyle",type);
        modelMap.put("businessType",businessType);
        modelMap.put("fileRandomFlag",fileRandomFlag);
        return "/common/fileupload/uploadFile";
    }

    /**
     * �ϴ�ͼƬ
     *
     * @param request
     * @return
     * @throws Exception
     */

    @ResponseBody
    @RequestMapping("/uploadFile/uploadImage/{fileStyle}/{businessType}/{fileRandomFlag}")
    public AjaxReturnMsg uploadImage(HttpServletRequest request,@PathVariable(value = "fileStyle") String fileStyle,@PathVariable(value = "businessType") String businessType,@PathVariable(value = "fileRandomFlag") String fileRandomFlag) {
        try {
        	SUser suser = (SUser) request.getSession().getAttribute(SUserUtil.SHIRO_CURRENT_USER_INFO);
            fileUploadUtils.uploadFile_ForProvince(request, suser.getUsername(), businessType, fileStyle,fileRandomFlag, "/api/uploadFile/uploadImage");
            return this.success("�ϴ��ɹ�");
        } catch (Exception e) {
            e.printStackTrace();
            return this.error(e.getMessage());
        }
    }

    /**
     * ��ȡ�ϴ��ļ�����������
     */
    @ResponseBody
    @RequestMapping("/getUploadFileNumberInfo/{fileStyle}/{businessType}/{fileRandomFlag}")
    public AjaxReturnMsg getUploadFileNumberInfo(HttpSession session,@PathVariable String fileStyle,@PathVariable String businessType,@PathVariable String fileRandomFlag) throws Exception {
        SUser suser = (SUser) session.getAttribute(SUserUtil.SHIRO_CURRENT_USER_INFO);
        String aac002 = suser.getUsername();
        HashMap<String, String> map = new HashMap<>();
        map.put("aaa002", aac002);
        map.put("aaa004",fileStyle);
        map.put("aaa010", businessType);
        map.put("aaa011",fileRandomFlag);
        JSONObject object = httpRequestUtils.httpPost("/api/UploadFileNumberInfo", map);
        return this.success(object.getJSONObject("obj"));
    }

    /**
     * ��ȡ�ϴ��ļ���Ϣ�б�(��)
     */
    @ResponseBody
    @RequestMapping("/getFileUploadInfoList")
    public List<SuploadFile> getFileUploadInfoList(HttpServletRequest request,HttpSession session) throws Exception {
        String filetype = request.getParameter("fileStyle");
        String businessType = request.getParameter("businessType");
        String fileRandomFlag = request.getParameter("fileRandomFlag");
        SUser suser = (SUser) session.getAttribute(SUserUtil.SHIRO_CURRENT_USER_INFO);
        SuploadFile suploadFile = new SuploadFile();
        suploadFile.setAaa002(suser.getUsername());
        suploadFile.setAaa004(filetype);
        suploadFile.setAaa010(businessType);
        suploadFile.setAaa011(fileRandomFlag);
        List<SuploadFile> list = httpRequestUtils.httpPostReturnList("/api/getFileUploadInfoList", suploadFile);
        return list;
    }

    /**
     * ��ȡ�ϴ��ļ���Ϣ
     */
    @ResponseBody
    @RequestMapping("/getFileUploadInfo")
    public AjaxReturnMsg getUploadInfo(HttpServletRequest request,HttpSession session) throws Exception {
        String filetype = request.getParameter("filetype");
        SUser suser = (SUser) session.getAttribute(SUserUtil.SHIRO_CURRENT_USER_INFO);
        SuploadFile suploadFile = new SuploadFile();
        suploadFile.setAaa002(suser.getUsername());
        suploadFile.setAaa004(filetype);
        PageInfo<SuploadFile> pageInfo = httpRequestUtils.httpPostReturnPage(UriConstraints.API_FILE_INFO, suploadFile);
        return this.success(pageInfo);
    }

    /**
     * ɾ��ͼƬ
     *
     * @param fileStyle
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/fileUploadInfo/delete/{fileStyle}/{businessType}/{fileRandomFlag}")
    public AjaxReturnMsg delUpLoadFile(HttpSession session,@PathVariable String fileStyle,@PathVariable String businessType,@PathVariable String fileRandomFlag) throws Exception {
        SUser suser = (SUser) session.getAttribute(SUserUtil.SHIRO_CURRENT_USER_INFO);
        HashMap<String, String> map = new HashMap<>();
        map.put("aaa004", fileStyle);
        map.put("aaa002", suser.getUsername());
        map.put("aaa010", businessType);
        map.put("aaa011",fileRandomFlag);
        JSONObject object = httpRequestUtils.httpPost(UriConstraints.API_FILE_DELETE, map);
        return (AjaxReturnMsg) JSONObject.toBean(object, AjaxReturnMsg.class);
    }
}
