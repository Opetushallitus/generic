package fi.vm.sade.security;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.springframework.aop.interceptor.CustomizableTraceInterceptor;

/**
 * @author Antti Salonen
 */
public class TraceInterceptor extends CustomizableTraceInterceptor {

    protected void writeToLog(Log logger, String message, Throwable ex) {
//        if (ex != null) {
//            logger.info(message, ex);
//        } else {
//            logger.info(message);
//        }
        System.out.println("TRACE - "+message+(ex!=null?" ("+ex+")":""));
    }


    protected boolean isInterceptorEnabled(MethodInvocation invocation, Log logger) {
        return true;
    }

}
