package fi.vm.sade.generic.service.authz.aspect;

import fi.vm.sade.generic.service.authz.annotation.RequiresRole;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * Aspect for authorizing service calls.
 * <p/>
 * Checks if called method has been annotated. If annotation exists,
 * looks for corresponding roles in authorization data. If found, authorization
 * is granted, otherwise throws RuntimeException back to caller.
 *
 * @author Eetu Blomqvist
 */
@Aspect
public class AuthorizingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizingAspect.class);

    @Pointcut("within(fi.vm.sade.*.service.impl.*)")
    public void serviceMethod() {
    }

    @Around("serviceMethod()")
    public Object authorize(ProceedingJoinPoint pjp) throws Throwable {
        try {
            LOGGER.info("Intercepting serviceMethod() call!");

            AuthzData authzData = AuthzDataThreadLocal.get();

            LOGGER.info("Authzdata: " + authzData);

            boolean authorized = false;

            MethodSignature sig = (MethodSignature) pjp.getSignature();

            LOGGER.info(sig.getDeclaringTypeName() + " - " + sig.getMethod());

            RequiresRole annotation = pjp.getTarget().getClass().getMethod(sig.getName(),
                    sig.getParameterTypes()).getAnnotation(RequiresRole.class);

            if (annotation == null) {
                throw new RuntimeException(sig.getMethod().getName() +  " - RequiresRole missing.");
            }

            String[] roleNames = annotation.roleNames();

            for (String roleName : roleNames) {
                for (Map.Entry<String, Set<String>> entry : authzData.getDataMap().entrySet()) {
                    if (entry.getValue().contains(roleName)) {
                        authorized = true;
                        break;
                    }
                }
                if (authorized) {
                    break;
                }
            }

            if (!authorized) {
                throw new RuntimeException("Not authorized.");
            }
            // proceed to actual method call
            return pjp.proceed();

        } finally {
            AuthzDataThreadLocal.remove();
        }
    }
}
