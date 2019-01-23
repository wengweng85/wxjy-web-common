package com.insigma.http;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.insigma.common.util.Base64Util;
import com.insigma.common.util.DateUtil;
import com.insigma.mvc.model.SysExcelBatch;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.insigma.mvc.model.SFileRecord;
import com.insigma.mvc.model.SuploadFile;

/**
 * 文件上传工具类
 * @author  admin
 */
public class FileUploadUtils {

    @Value("${localdir}")
    private String localdir;

    //http工具类
    @Resource
    private HttpRequestUtils httpRequestUtils;

    public SFileRecord uploadFile(HttpServletRequest request, String userid, String file_bus_id, String file_bus_type, String url) throws Exception {
        String desc = request.getParameter("desc");
        // **********************参数初始化*********************
        //创建一个通用的多部分解析器
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        //判断 request 是否有文件上传,即多部分请求
        if (multipartResolver.isMultipart(request)) {
            //转换成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            //取得request中的所有文件名
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                //取得上传文件
                MultipartFile multipartFile = multiRequest.getFile(iter.next());
                // 得到去除路径的文件名
                String originalFilename = multipartFile.getOriginalFilename();
                int indexofdoute = originalFilename.lastIndexOf(".");

                if (indexofdoute < 0) {
                    throw new Exception("文件格式错误");
                }

                /**文件名及后缀*/
                String prefix = originalFilename.substring(0, indexofdoute);
                /**获取文件的后缀**/
                String endfix = originalFilename.substring(indexofdoute).toLowerCase();
                String[] arr = {".jpg", ".jpeg", ".gif", ".png", ".bmp", ".pdf", ".doc", ".docx",
                        ".xls", ".xlsx", ".rar", ".zip", ".mp4"};
                if (!Arrays.asList(arr).contains(endfix)) {
                    throw new Exception("文件格式不正确,请确认");
                }
                if (prefix.length() < 3) {
                    prefix = prefix + "-001";
                }

                //上传并记录日志
                File file = File.createTempFile(prefix, endfix);
                multipartFile.transferTo(file);
                JSONObject result = httpRequestUtils.httpUploadFile(url, file, originalFilename, file_bus_type, file_bus_id, userid, desc);
                if (!result.getBoolean("success")) {
                    throw new Exception(result.getString("message"));
                }
                return (SFileRecord) JSONObject.toBean(result.getJSONObject("obj"), SFileRecord.class);
            }
        }
        return null;
    }



    /**
     *
     * @param request
     * @param url
     * @return
     * @throws Exception
     */
    public SysExcelBatch uploadExcelFile(HttpServletRequest request, String url) throws Exception {
        String excel_batch_excel_type = request.getParameter("excel_batch_excel_type");
        String excel_batch_assistid = request.getParameter("excel_batch_assistid");
        String mincolumns = request.getParameter("mincolumns");
        // **********************参数初始化*********************
        // 创建一个通用的多部分解析器
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        // 判断 request 是否有文件上传,即多部分请求
        if (multipartResolver.isMultipart(request)) {
            // 转换成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            // 取得request中的所有文件名
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                // 取得上传文件
                MultipartFile multipartFile = multiRequest.getFile(iter.next());

                // 得到去除路径的文件名
                String originalFilename = multipartFile.getOriginalFilename();
                int indexofdoute = originalFilename.lastIndexOf(".");

                if (indexofdoute < 0) {
                    throw new Exception("文件格式错误");
                }

                /** 文件名及后缀 */
                String prefix = originalFilename.substring(0, indexofdoute);
                /** 获取文件的后缀 **/
                String endfix = originalFilename.substring(indexofdoute).toLowerCase();
                String[] arr = { ".xlsx" };
                if (!Arrays.asList(arr).contains(endfix)) {
                    throw new Exception("文件格式不正确,请确认");
                }
                if (prefix.length() < 3) {
                    prefix = prefix + "-001";
                }

                // 上传并记录日志
                File file = File.createTempFile(prefix, endfix);
                multipartFile.transferTo(file);
                JSONObject result = httpRequestUtils.executeUploadExcelFile(url, file, excel_batch_excel_type,excel_batch_assistid,
                        mincolumns);
                if (!result.getBoolean("success")) {
                    throw new Exception(result.getString("message"));
                }
                //--------原有部分----开始----
                SysExcelBatch sysExcelBatch = (SysExcelBatch) JSONObject.toBean(result.getJSONObject("obj"),
                        SysExcelBatch.class);
                //--------原有部分----结束----
                return sysExcelBatch;
            }
        }
        return null;
    }


    /**
     * uploadFile
     * @param base64String
     * @param userid
     * @param file_bus_id
     * @param file_bus_type
     * @param url
     * @return
     * @throws Exception
     */
    public SFileRecord uploadFile(String base64String, String userid, String file_bus_id, String file_bus_type, String url) throws Exception {
        /** 当前月份 **/
        String fileDir = "/fileroot/" + file_bus_type + "/" + DateUtil.dateToString(new Date(), "yyyyMM");// yyyyMM
        /** 根据真实路径创建目录 **/
        File fileuploadDir = new File(localdir + fileDir);
        if (!fileuploadDir.exists()) {
            fileuploadDir.mkdirs();
        }
        String originalFilename = file_bus_id + ".jpg";
        String imagePath = localdir + fileDir +"/"+ originalFilename;
        String imageType = "jpg";
        boolean isSuccess = Base64Util.base64StringToImage(base64String, imagePath, imageType);
        if (isSuccess) {
            File file = new File(imagePath);
            JSONObject result = httpRequestUtils.httpUploadFile(url, file, originalFilename, file_bus_type, file_bus_id,
                    userid, "");
            if (!result.getBoolean("success")) {
                throw new Exception(result.getString("message"));
            }
            return (SFileRecord) JSONObject.toBean(result.getJSONObject("obj"), SFileRecord.class);
        }
        return null;
    }
    /**
     * 
     * @param request
     * @param userid
     * @param file_bus_id
     * @param file_bus_type
     * @param fileRandomFlag
     * @param url
     * @return
     * @throws Exception
     */
    public SuploadFile uploadFile_ForProvince(HttpServletRequest request, String file_bus_id, String file_bus_type,String fileRandomFlag, String url) throws Exception {
        String desc = request.getParameter("desc");
        // **********************参数初始化*********************
        //创建一个通用的多部分解析器
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        //判断 request 是否有文件上传,即多部分请求
        if (multipartResolver.isMultipart(request)) {
            //转换成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            //取得request中的所有文件名
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                //取得上传文件
                MultipartFile multipartFile = multiRequest.getFile(iter.next());

                // 得到去除路径的文件名
                String originalFilename = multipartFile.getOriginalFilename();
                int indexofdoute = originalFilename.lastIndexOf(".");

                if (indexofdoute < 0) {
                    throw new Exception("文件格式错误");
                }

                /**文件名及后缀*/
                String prefix = originalFilename.substring(0, indexofdoute);
                /**获取文件的后缀**/
                String endfix = originalFilename.substring(indexofdoute).toLowerCase();
                String[] arr = {".jpg", ".jpeg", ".gif", ".png", ".bmp", ".pdf", ".doc", ".docx",
                        ".xls", ".xlsx", ".rar", ".zip", ".mp4"};
                if (!Arrays.asList(arr).contains(endfix)) {
                    throw new Exception("文件格式不正确,请确认");
                }
                if (prefix.length() < 3) {
                    prefix = prefix + "-001";
                }

                //上传并记录日志
                File file = File.createTempFile(prefix, endfix);
                multipartFile.transferTo(file);
                JSONObject result = httpRequestUtils.httpUploadFile_ForProvince(url, file, originalFilename, file_bus_type, file_bus_id, fileRandomFlag,desc);
                if (!result.getBoolean("success")) {
                    throw new Exception(result.getString("message"));
                }
                return (SuploadFile) JSONObject.toBean(result.getJSONObject("obj"), SuploadFile.class);
            }
        }
        return null;
    }
}
