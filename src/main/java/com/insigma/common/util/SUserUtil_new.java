package com.insigma.common.util;

import com.insigma.mvc.model.SPermission;
import com.insigma.mvc.model.SUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统工具类
 * @author wengsh
 *
 */
public class SUserUtil_new {

	public static final String SHIRO_CURRENT_USER_INFO="SHIRO_CURRENT_USER_INFO";
	public static final String SHIRO_CURRENT_PERM_LIST_INFO="SHIRO_CURRENT_PERM_LIST_INFO";


	 /** * 保存变量的ThreadLocal，保持在同一线程中同步数据. */
    private static final ThreadLocal threadLocal = new ThreadLocal();

    /** * 工具类的protected构造方法. */
    protected SUserUtil_new() {
    }

	 /**
	  *  setCurrentUser
	  * @param suser
	  */
    public static void setCurrentUser(SUser suser) {
        Map map = (Map) threadLocal.get();
        if (map == null) {
            map = new HashMap();
            threadLocal.set(map);
        }

        map.put(SHIRO_CURRENT_USER_INFO, suser);
    }  
   

    /**
	  *  setCurrentUser
	  */
   public static void removeCurrentUser() {
       Map map = (Map) threadLocal.get();
       if (map == null) {
           map = new HashMap();
           threadLocal.set(map);
       }

       map.put(SHIRO_CURRENT_USER_INFO, null);
   }  
  
    /**
     * getCurrentUser
     * @return
     */
    public static SUser getCurrentUser() {
    	  Map map = (Map) threadLocal.get();
    	  if(map!=null){
              return (SUser)map.get(SHIRO_CURRENT_USER_INFO);
    	  }else{
    		  return null;
    	  }
    }

    public static void remove() {
        threadLocal.remove();
    }
    
  
   
	/**
    * 过滤三级菜单
    * @param permlist
    */
   public static List<SPermission> filterPersmList(List< SPermission > permlist){
       List<SPermission> resultlist=new ArrayList<SPermission>();
       List<SPermission> firstTempPermlist=new ArrayList<SPermission>();
       //过滤掉按钮节点
       for(int i=0;i<permlist.size();i++) {
           if(permlist.get(i).getPermtype().equals("3")){
           	    permlist.remove(i);
           	    i--;
           }
       }
       
       //先将第一级节点过滤出来
       for(int i=0;i<permlist.size();i++) {
           //如果是第一级
           if(permlist.get(i).getParentid().equals("0")||permlist.get(i).getParentid().matches("\\w{0,12}") ){
             	firstTempPermlist.add(permlist.get(i));
           	    permlist.remove(i);
           	    i--;
           }
       }

       //再根据第一级节点过滤出第二级或三级或四级节点
       for(int i=0;i<firstTempPermlist.size();i++){
    	   SPermission firstTempPerm=firstTempPermlist.get(i);
           List<SPermission> secondPersmList=new ArrayList<SPermission>();
           for(int j=0;j<permlist.size();j++) {
        	   SPermission secondTempPerm=permlist.get(j);
               //第二级
               if(secondTempPerm.getParentid().equals(firstTempPerm.getPermissionid())){
            	   permlist.remove(j);
            	   j--;
            	   List<SPermission> thirdPermList=new ArrayList<SPermission>();
            	   for(int k=0;k<permlist.size();k++){
            		   SPermission thirdTempPerm = permlist.get(k);
            		   //第三级
            		   if(thirdTempPerm.getParentid().equals(secondTempPerm.getPermissionid())){
            			   List<SPermission> fourthPermList = new ArrayList<SPermission>();
            			   for(int h=0;h<permlist.size();h++) {
            				   //第四级
            				   if(permlist.get(h).getParentid().equals(thirdTempPerm.getPermissionid())) {
            					   fourthPermList.add(permlist.get(h));
            				   }
            			   }
            			   thirdTempPerm.setChild(fourthPermList);
            			   thirdPermList.add(thirdTempPerm);
					   }
				   }
            	   secondTempPerm.setChild(thirdPermList);
            	   secondPersmList.add(secondTempPerm);
               }
           }
           if(secondPersmList.size()>0){
           	   firstTempPerm.setChild(secondPersmList);
           }
           resultlist.add(firstTempPerm);
       }
       
       
       return resultlist;
   }

   
   

}
