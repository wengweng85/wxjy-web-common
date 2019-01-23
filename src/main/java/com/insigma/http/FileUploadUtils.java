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
 * �ļ��ϴ�������
 * @author  admin
 */
public class FileUploadUtils {

    @Value("${localdir}")
    private String localdir;

    //http������
    @Resource
    private HttpRequestUtils httpRequestUtils;

    public SFileRecord uploadFile(HttpServletRequest request, String userid, String file_bus_id, String file_bus_type, String url) throws Exception {
        String desc = request.getParameter("desc");
        // **********************������ʼ��*********************
        //����һ��ͨ�õĶಿ�ֽ�����
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        //�ж� request �Ƿ����ļ��ϴ�,���ಿ������
        if (multipartResolver.isMultipart(request)) {
            //ת���ɶಿ��request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            //ȡ��request�е������ļ���
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                //ȡ���ϴ��ļ�
                MultipartFile multipartFile = multiRequest.getFile(iter.next());
                // �õ�ȥ��·�����ļ���
                String originalFilename = multipartFile.getOriginalFilename();
                int indexofdoute = originalFilename.lastIndexOf(".");

                if (indexofdoute < 0) {
                    throw new Exception("�ļ���ʽ����");
                }

                /**�ļ�������׺*/
                String prefix = originalFilename.substring(0, indexofdoute);
                /**��ȡ�ļ��ĺ�׺**/
                String endfix = originalFilename.substring(indexofdoute).toLowerCase();
                String[] arr = {".jpg", ".jpeg", ".gif", ".png", ".bmp", ".pdf", ".doc", ".docx",
                        ".xls", ".xlsx", ".rar", ".zip", ".mp4"};
                if (!Arrays.asList(arr).contains(endfix)) {
                    throw new Exception("�ļ���ʽ����ȷ,��ȷ��");
                }
                if (prefix.length() < 3) {
                    prefix = prefix + "-001";
                }

                //�ϴ�����¼��־
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
        // **********************������ʼ��*********************
        // ����һ��ͨ�õĶಿ�ֽ�����
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        // �ж� request �Ƿ����ļ��ϴ�,���ಿ������
        if (multipartResolver.isMultipart(request)) {
            // ת���ɶಿ��request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            // ȡ��request�е������ļ���
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                // ȡ���ϴ��ļ�
                MultipartFile multipartFile = multiRequest.getFile(iter.next());

                // �õ�ȥ��·�����ļ���
                String originalFilename = multipartFile.getOriginalFilename();
                int indexofdoute = originalFilename.lastIndexOf(".");

                if (indexofdoute < 0) {
                    throw new Exception("�ļ���ʽ����");
                }

                /** �ļ�������׺ */
                String prefix = originalFilename.substring(0, indexofdoute);
                /** ��ȡ�ļ��ĺ�׺ **/
                String endfix = originalFilename.substring(indexofdoute).toLowerCase();
                String[] arr = { ".xlsx" };
                if (!Arrays.asList(arr).contains(endfix)) {
                    throw new Exception("�ļ���ʽ����ȷ,��ȷ��");
                }
                if (prefix.length() < 3) {
                    prefix = prefix + "-001";
                }

                // �ϴ�����¼��־
                File file = File.createTempFile(prefix, endfix);
                multipartFile.transferTo(file);
                JSONObject result = httpRequestUtils.executeUploadExcelFile(url, file, excel_batch_excel_type,excel_batch_assistid,
                        mincolumns);
                if (!result.getBoolean("success")) {
                    throw new Exception(result.getString("message"));
                }
                //--------ԭ�в���----��ʼ----
                SysExcelBatch sysExcelBatch = (SysExcelBatch) JSONObject.toBean(result.getJSONObject("obj"),
                        SysExcelBatch.class);
                //--------ԭ�в���----����----
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
        /** ��ǰ�·� **/
        String fileDir = "/fileroot/" + file_bus_type + "/" + DateUtil.dateToString(new Date(), "yyyyMM");// yyyyMM
        /** ������ʵ·������Ŀ¼ **/
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
        // **********************������ʼ��*********************
        //����һ��ͨ�õĶಿ�ֽ�����
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        //�ж� request �Ƿ����ļ��ϴ�,���ಿ������
        if (multipartResolver.isMultipart(request)) {
            //ת���ɶಿ��request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            //ȡ��request�е������ļ���
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                //ȡ���ϴ��ļ�
                MultipartFile multipartFile = multiRequest.getFile(iter.next());

                // �õ�ȥ��·�����ļ���
                String originalFilename = multipartFile.getOriginalFilename();
                int indexofdoute = originalFilename.lastIndexOf(".");

                if (indexofdoute < 0) {
                    throw new Exception("�ļ���ʽ����");
                }

                /**�ļ�������׺*/
                String prefix = originalFilename.substring(0, indexofdoute);
                /**��ȡ�ļ��ĺ�׺**/
                String endfix = originalFilename.substring(indexofdoute).toLowerCase();
                String[] arr = {".jpg", ".jpeg", ".gif", ".png", ".bmp", ".pdf", ".doc", ".docx",
                        ".xls", ".xlsx", ".rar", ".zip", ".mp4"};
                if (!Arrays.asList(arr).contains(endfix)) {
                    throw new Exception("�ļ���ʽ����ȷ,��ȷ��");
                }
                if (prefix.length() < 3) {
                    prefix = prefix + "-001";
                }

                //�ϴ�����¼��־
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
