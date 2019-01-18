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
import com.insigma.dto.AjaxReturnMsg;
import com.insigma.http.FileUploadUtils;
import com.insigma.http.HttpRequestUtils;
import com.insigma.mvc.MvcHelper;
import com.insigma.mvc.UriConstraints;
import com.insigma.mvc.model.SuploadFile;

/**
 * FileUploadController
 */
@Controller
@RequestMapping(value = "/common")
public class FileUploadController extends MvcHelper {

    @Resource
    private HttpRequestUtils httpRequestUtils;
    
    //文件上传工具类
    @Resource
    private FileUploadUtils fileUploadUtils;
    
    /**
     * 上传文件
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
     * 上传图片
     *
     * @param request
     * @return
     * @throws Exception
     */

    @ResponseBody
    @RequestMapping("/uploadFile/uploadImage/{fileStyle}/{businessType}/{fileRandomFlag}")
    public AjaxReturnMsg uploadImage(HttpServletRequest request,@PathVariable(value = "fileStyle") String fileStyle,@PathVariable(value = "businessType") String businessType,@PathVariable(value = "fileRandomFlag") String fileRandomFlag) {
        try {
            fileUploadUtils.uploadFile_ForProvince(request, businessType, fileStyle,fileRandomFlag, UriConstraints.API_FILE_UPLOADIMAGE);
            return this.success("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            return this.error(e.getMessage());
        }
    }

    /**
     * 获取上传文件数量和限制
     */
    @ResponseBody
    @RequestMapping("/getUploadFileNumberInfo/{fileStyle}/{businessType}/{fileRandomFlag}")
    public AjaxReturnMsg getUploadFileNumberInfo(HttpSession session,@PathVariable String fileStyle,@PathVariable String businessType,@PathVariable String fileRandomFlag) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("aaa004",fileStyle);
        map.put("aaa010", businessType);
        map.put("aaa011",fileRandomFlag);
        JSONObject object = httpRequestUtils.httpPost(UriConstraints.API_FILE_UploadFileNumberInfo, map);
        return this.success(object.getJSONObject("obj"));
    }

    /**
     * 获取上传文件信息列表(新)
     */
    @ResponseBody
    @RequestMapping("/getFileUploadInfoList")
    public List<SuploadFile> getFileUploadInfoList(HttpServletRequest request,HttpSession session) throws Exception {
        String filetype = request.getParameter("fileStyle");
        String businessType = request.getParameter("businessType");
        String fileRandomFlag = request.getParameter("fileRandomFlag");
        SuploadFile suploadFile = new SuploadFile();
        suploadFile.setAaa004(filetype);
        suploadFile.setAaa010(businessType);
        suploadFile.setAaa011(fileRandomFlag);
        List<SuploadFile> list = httpRequestUtils.httpPostReturnList(UriConstraints.API_FILE_UPLOADINFOLIST, suploadFile);
        return list;
    }

    /**
     * 获取上传文件信息
     */
    @ResponseBody
    @RequestMapping("/getFileUploadInfo")
    public AjaxReturnMsg getUploadInfo(HttpServletRequest request,HttpSession session) throws Exception {
        String filetype = request.getParameter("filetype");
        SuploadFile suploadFile = new SuploadFile();
        suploadFile.setAaa004(filetype);
        PageInfo<SuploadFile> pageInfo = httpRequestUtils.httpPostReturnPage(UriConstraints.API_FILE_INFO, suploadFile);
        return this.success(pageInfo);
    }

    /**
     * 删除图片
     *
     * @param fileStyle
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/fileUploadInfo/delete/{fileStyle}/{businessType}/{fileRandomFlag}")
    public AjaxReturnMsg delUpLoadFile(HttpSession session,@PathVariable String fileStyle,@PathVariable String businessType,@PathVariable String fileRandomFlag) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("aaa004", fileStyle);
        map.put("aaa010", businessType);
        map.put("aaa011",fileRandomFlag);
        JSONObject object = httpRequestUtils.httpPost(UriConstraints.API_FILE_DELETE, map);
        return (AjaxReturnMsg) JSONObject.toBean(object, AjaxReturnMsg.class);
    }
}
