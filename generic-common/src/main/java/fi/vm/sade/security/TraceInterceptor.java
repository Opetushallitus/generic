package fi.vm.sade.security;

import fi.vm.sade.generic.ui.feature.UserFeature;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.springframework.aop.interceptor.CustomizableTraceInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * Poor man's performance tracer, investigate Spring etc performance interceptors if this are used beyond temp usage
 *
 * @author Antti Salonen
 */
public class TraceInterceptor implements MethodInterceptor {

    private static Map<String, Long> cumulativeInvocationTimes = new HashMap<String, Long>();
    private static Map<String, Long> invocationCounts = new HashMap<String, Long>();

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        long t0 = System.currentTimeMillis();
        try {
            return methodInvocation.proceed();
        } finally {
            long invocationTime = System.currentTimeMillis() - t0;
            String methodKey = methodInvocation.getMethod().getDeclaringClass().getSimpleName()+"."+methodInvocation.getMethod().getName();
            Long cumulativeTime = addCumulativeTime(invocationTime, methodKey);
            Long count = raiseInvocationCount(methodKey);
            System.out.println("PERFORMANCE TRACE, cumulative: "+cumulativeTime+"ms, count: "+count+", method: "+methodKey);
        }
    }

    private synchronized static Long addCumulativeTime(long invocationTime, String methodKey) {
        Long cumulativeInvocationTime = cumulativeInvocationTimes.get(methodKey);
        if (cumulativeInvocationTime == null) {
            cumulativeInvocationTime = 0l;
        }
        cumulativeInvocationTime += invocationTime;
        cumulativeInvocationTimes.put(methodKey, cumulativeInvocationTime);
        return cumulativeInvocationTime;
    }

    private synchronized static Long raiseInvocationCount(String methodKey) {
        Long count = invocationCounts.get(methodKey);
        if (count == null) {
            count = 0l;
        }
        count ++;
        invocationCounts.put(methodKey, count);
        return count;
    }

}
