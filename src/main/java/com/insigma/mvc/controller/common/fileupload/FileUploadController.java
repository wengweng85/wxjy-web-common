package com.insigma.mvc.controller.common.fileupload;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.insigma.common.listener.AppConfig;
import com.insigma.common.util.DateUtil;
import com.insigma.common.util.RandomNumUtil;
import com.insigma.mvc.model.SFileRecord;
import com.insigma.mvc.model.SysExcelBatch;
import com.insigma.mvc.model.SysFileRecord;
import com.insigma.resolver.AppException;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import org.springframework.web.servlet.ModelAndView;

/**
 * �ļ�������
 * @author  admin
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
            fileUploadUtils.uploadFile_ForProvince(request, businessType, fileStyle,fileRandomFlag, UriConstraints.API_FILE_UPLOADIMAGE);
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
        HashMap<String, String> map = new HashMap<>();
        map.put("aaa004",fileStyle);
        map.put("aaa010", businessType);
        map.put("aaa011",fileRandomFlag);
        JSONObject object = httpRequestUtils.httpPost(UriConstraints.API_FILE_UploadFileNumberInfo, map);
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
        SuploadFile suploadFile = new SuploadFile();
        suploadFile.setAaa004(filetype);
        suploadFile.setAaa010(businessType);
        suploadFile.setAaa011(fileRandomFlag);
        List<SuploadFile> list = httpRequestUtils.httpPostReturnList(UriConstraints.API_FILE_UPLOADINFOLIST, suploadFile);
        return list;
    }

    /**
     * ��ȡ�ϴ��ļ���Ϣ
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
     * ɾ��ͼƬ
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


    /**
     * ��ת���ϴ�ҳ��
     * @param request
     * @return
     */
    @RequestMapping("/tofilelist")
    public ModelAndView tofilelist(HttpServletRequest request, Model model, SysFileRecord sFileRecord ) throws Exception {
        ModelAndView modelAndView=new ModelAndView("common/fileupload/filelist");
        if(sFileRecord.getFile_bus_id()==null){
            throw new Exception("ҵ���Ų���Ϊ��");
        }
        String url = request.getParameter("url");
        if(url==null){
            throw new Exception("�ϴ�·������Ϊ��");
        }
        sFileRecord.setFile_name("����");
        modelAndView.addObject("filerecord", sFileRecord);
        modelAndView.addObject("url", url);
        return modelAndView;
    }

    /**
     * ��ת��ͼƬ�ϴ�ҳ��
     * @param request
     * @return
     */
    @RequestMapping("/toImgUpload")
    public ModelAndView toupload(HttpServletRequest request,Model modell,SysFileRecord sFileRecord) throws Exception {
        ModelAndView modelAndView=new ModelAndView("common/fileupload/imgUpload");
        modelAndView.addObject("filerecord", sFileRecord);
        String url = request.getParameter("url");
        modelAndView.addObject("url", url);
        return modelAndView;
    }

    /**
     * ��ת��ͼƬ�ϴ�ҳ��(����д�ļ�˵��)
     * @param request
     * @return
     */
    @RequestMapping("/toImgUploadWithDesc")
    public ModelAndView toImgUploadWithDesc(HttpServletRequest request,Model modell,SysFileRecord sFileRecord) throws Exception {
        ModelAndView modelAndView=new ModelAndView("common/fileupload/ImgUploadWithDesc");
        modelAndView.addObject("filerecord", sFileRecord);
        String url = request.getParameter("url");
        modelAndView.addObject("url", url);
        return modelAndView;
    }

    /**
     * ��ת���ļ��ϴ�ҳ��
     * @param request
     * @return
     */
    @RequestMapping("/toFileUpload")
    public ModelAndView toFileUpload(HttpServletRequest request,Model modell,SysFileRecord sFileRecord) throws Exception {
        ModelAndView modelAndView=new ModelAndView("common/fileupload/fileUpload");
        modelAndView.addObject("filerecord", sFileRecord);
        String url = request.getParameter("url");
        modelAndView.addObject("url", url);
        return modelAndView;
    }

    /**
     * ��ת���ļ��ϴ�ҳ��(����д�ļ�˵��)
     * @param request
     * @return
     */
    @RequestMapping("/toFileUploadWithDesc")
    public ModelAndView toFileUploadWithDesc(HttpServletRequest request,Model modell,SysFileRecord sFileRecord) throws Exception {
        ModelAndView modelAndView=new ModelAndView("common/fileupload/fileUploadWithDesc");
        modelAndView.addObject("filerecord", sFileRecord);
        String url = request.getParameter("url");
        modelAndView.addObject("url", url);
        return modelAndView;
    }
    /**
     * ��ת���ļ��ϴ�ҳ��
     * @param request
     * @return
     */
    @RequestMapping("/toFileUploadSpecial")
    public ModelAndView toFileUploadSpecial(HttpServletRequest request,Model modell,SysFileRecord sFileRecord) throws Exception {
        ModelAndView modelAndView=new ModelAndView("common/fileupload/fileUploadSpecial");
        modelAndView.addObject("filerecord", sFileRecord);
        String url = request.getParameter("url");
        modelAndView.addObject("url", url);
        return modelAndView;
    }
    /**
     * ͨ����Աid��ȡ�ļ��б�
     *
     * @param sFileRecord
     * @return
     */
    @RequestMapping("/getFileList")
    @ResponseBody
    public HashMap getUserListByGroupid(SFileRecord sFileRecord) throws Exception {
        PageInfo<SFileRecord> pageinfo = httpRequestUtils.httpPostReturnPage("/api/files", sFileRecord);
        return this.success_hashmap_response(pageinfo);
    }

    /**
     * ͨ��bus_uuidɾ���ļ���Ϣ
     *
     * @param bus_uuid
     * @return
     */
    @RequestMapping("/deleteById/{bus_uuid}")
    @ResponseBody
    public AjaxReturnMsg deleteFileByid(@PathVariable String bus_uuid) throws Exception {
        String url = "/api/file/" + bus_uuid;
        JSONObject obj =  httpRequestUtils.httpDelete(url);
        return (AjaxReturnMsg) JSONObject.toBean(obj, AjaxReturnMsg.class);
    }


    /**
     * �ļ���ȡ
     *
     * @param bus_uuid
     * @param response
     * @throws AppException
     */
    @RequestMapping(value = "/download/{bus_uuid}/{type}")
    public void download(@PathVariable String bus_uuid, @PathVariable String type, HttpServletResponse response) throws AppException {
        try {
            String url = "";
            if("1".equals(type)) {
                //���ر����ļ�
                url = "/api/common/download/" + bus_uuid;
            }else {
                //�����ļ���������ͼƬ
                url = "/api/common/downloadWeb/" + bus_uuid;
            }
            File file = httpRequestUtils.httpDownLoadFile(url);
            if (file != null) {
                byte[] temp = download(file);
                if (temp != null) {
                    //���д����Ƿ�ֹ��������Ĺؼ�����
                    response.setHeader("Content-disposition", "attachment; filename=" + new String(file.getName().getBytes("utf-8"), "iso-8859-1"));
                    BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(temp));
                    BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
                    //�½�һ��2048�ֽڵĻ�����
                    byte[] buff = new byte[2048];
                    int bytesRead;
                    while ((bytesRead = bis.read(buff, 0, buff.length)) != -1) {
                        bos.write(buff, 0, bytesRead);
                    }
                    bos.flush();
                    bis.close();
                    bos.close();
                    if (file.exists()) {
                        file.delete();
                    }
                } else {
                    throw new AppException("���ش���,�ļ�������");
                }
            }
        } catch (Exception e) {
            throw new AppException("���ش���,�ļ�������");
        }
    }

    /**
     * �ļ�����
     *
     * @param bus_uuid
     * @param response
     * @throws AppException
     */
    @RequestMapping(value = "/downloads/{bus_uuid}")
    public void downloads(@PathVariable String bus_uuid, HttpServletResponse response, HttpServletRequest request) throws AppException {
        PrintWriter out;
        JSONObject json = new JSONObject();
        String urldownloads = request.getSession().getServletContext().getRealPath(""); //·��
        byte[] buffer;
        String localdir = AppConfig.getProperties("localdir");
        String filepath;
        String fileDir;
        String file_rel_path = null;
        try {
            out = response.getWriter();
            String url = "/api/common/download/" + bus_uuid;
            File file = httpRequestUtils.httpDownLoadFile(url);
            if (file != null) {
                byte[] temp = download(file);
                InputStream is2 = new ByteArrayInputStream(temp);

                //�����ļ�·���ʹ����ļ�

                /** ����ͼƬ�����Ŀ¼ **/
                /** ��ǰ�·� **/

                fileDir = "/webroot/" + DateUtil.dateToString(new Date(), "yyyyMM");
                /** ������ʵ·������Ŀ¼ **/
                File fileuploadDir = new File(urldownloads + fileDir);
                if (!fileuploadDir.exists()) {
                    fileuploadDir.mkdirs();
                }
                String originalFilename = file.getName();
                file_rel_path = fileDir + "/" + originalFilename;
                /** ���ر���������ļ�����·�� **/
                filepath = urldownloads + file_rel_path;
                File files = new File(filepath);
                //���ͬ�����ļ�����
                if (files.exists()) {
                    int indexofdoute = originalFilename.lastIndexOf(".");
                    /**�ļ�������׺*/
                    String prefix = originalFilename.substring(0, indexofdoute);
                    String endfix = originalFilename.substring(indexofdoute).toLowerCase();
                    /**���ļ���������+�������*/
                    prefix += DateUtil.dateToString(new Date(), "yyyyMMddHHmmss") + '_' + RandomNumUtil.getRandomString(6);
                    file_rel_path = fileDir + "/" + prefix + endfix;
                    filepath = localdir + file_rel_path;
                    files = new File(filepath);
                }
                OutputStream os = new FileOutputStream(files);
                int bytesRead;
                buffer = new byte[8192];
                while ((bytesRead = is2.read(buffer, 0, 8192)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
                os.close();
                is2.close();

            }
            json.put("message", file_rel_path);
        } catch (Exception e) {
            throw new AppException("���ش���,�ļ�������");
        }
        out.write(json.toString());
        out.close();

    }

    /**
     * ����
     */
    public byte[] download(File file) {
        InputStream data = null;
        try {
            data = new FileInputStream(file);
            int size = data.available();
            byte[] buffer = new byte[size];
            IOUtils.read(data, buffer);
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(data);
        }
        return null;
    }

    /**
     * ��ת���ļ��ϴ�ҳ��
     * @param request
     * @return
     */
    @RequestMapping("/toExcelFileUpload")
    public ModelAndView toExcelFileUpload(HttpServletRequest request, Model modell, SysExcelBatch sExcelBatch) throws Exception {
        ModelAndView modelAndView=new ModelAndView("common/fileupload/excelfileUpload");
        modelAndView.addObject("sExcelBatch", sExcelBatch);
        return modelAndView;
    }

    /**
     * �ϴ�excel�������ļ�
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/excelupload")
    public void excelupload(HttpServletRequest request, HttpServletResponse response)  throws IOException {
        PrintWriter out = response.getWriter();
        JSONObject json = new JSONObject();
        String url="/api/file/uploadExcelFile";
        try {
            SysExcelBatch sysExcelBatch = fileUploadUtils.uploadExcelFile(request,  url);
            json.put("success", "true");
            json.put("bus_uuid", sysExcelBatch.getExcel_batch_number());
        } catch (Exception e) {
            json.put("success", "false");
            json.put("msg", e.getMessage());
        }
        out.write(json.toString());
        out.close();
    }



    /**
     * ���»򱣴� ͨ�������༭����
     * @param request
     * @return
     */
    @RequestMapping("/updateDescByXedit")
    @ResponseBody
    public AjaxReturnMsg<String> updateDescByXedit(HttpServletRequest request,Model mode, SFileRecord sFileRecord) throws Exception {
        return (AjaxReturnMsg) JSONObject.toBean(httpRequestUtils.httpPost("/api/file/updateFileDesc", sFileRecord), AjaxReturnMsg.class);
    }
}
