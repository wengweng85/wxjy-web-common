package com.insigma.resolver;

/**
 * �Զ�����Ŀ�쳣
 * @author xxx
 * @date 2014-11-25
 *
 */
public class AppException extends Exception {  
	  
    private static final long serialVersionUID = 1L;  
  
    public AppException() {  
    }
  
    public AppException(String message) {  
        super(message);  
    }
  
    public AppException(Throwable cause) {  
        super(cause);  
    }
  
    public AppException(String message, Throwable cause) {  
        super(message, cause);  
    }
  
} 
