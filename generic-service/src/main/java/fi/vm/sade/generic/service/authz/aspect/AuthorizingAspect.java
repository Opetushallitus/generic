package fi.vm.sade.generic.service.authz.aspect;

import fi.vm.sade.generic.service.authz.annotation.RequiredRole;
import org.apache.cxf.jaxws.javaee.RoleNameType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;

/**
 * @author Eetu Blomqvist
 */
public class AuthorizingAspect {

    @Pointcut("execution(* fi.vm.sade.*.service.impl.*(..))")
    public void serviceMethod() {
    }

    @Around("serviceMethod()")
    public void authorize(ProceedingJoinPoint pjp) {
        try {
            AuthzData authzData = AuthzDataThreadLocal.get();


            MethodSignature sig = (MethodSignature) pjp.getSignature();
            RequiredRole annotation = sig.getMethod().getAnnotation(RequiredRole.class);
            if (annotation == null) {
                throw new RuntimeException("Annotation required");
            }

            String roleName = annotation.roleName();

            // TODO handle role name

            pjp.proceed();

        } catch (Throwable throwable) {
            // TODO
        } finally {
            AuthzDataThreadLocal.remove();
        }

    }
}
