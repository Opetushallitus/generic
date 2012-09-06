package fi.vm.sade.generic.service.authz.aspect;

import fi.vm.sade.generic.common.auth.Role;
import fi.vm.sade.generic.common.auth.annotation.RequiresRole;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
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
            LOGGER.info("Intercepting serviceMethod() call to " + pjp.getSignature().getName());

            boolean authorized = false;
            MethodSignature sig = (MethodSignature) pjp.getSignature();
            AuthzData authzData = AuthzDataThreadLocal.get();

            LOGGER.info("User " + authzData.getUser() + " calling operation " + sig.getMethod().getName()
                    + " in " + sig.getDeclaringType());

            LOGGER.info("Authzdata: " + authzData);

            RequiresRole annotation = pjp.getTarget().getClass().getMethod(sig.getName(),
                    sig.getParameterTypes()).getAnnotation(RequiresRole.class);

            if (annotation == null) {
                throw new RuntimeException(sig.getMethod().getName() + " - RequiresRole missing.");
            }

            Role[] roles = annotation.roles();

            if(Arrays.asList(roles).contains(Role.NOT_REQUIRED)){
                authorized = true;
            }


            if (!authorized) {
                LOGGER.info("Method requires one of roles: " + Arrays.toString(roles));

                for (Role role : roles) {
                    for (Map.Entry<String, AuthzData.Organisation> entry : authzData.getDataMap().entrySet()) {
                        if (entry.getValue().roles.contains(role.name())) {
                            authorized = true;
                            break;
                        }
                    }
                    if (authorized) {
                        break;
                    }
                }
            }

            if (!authorized) {
                LOGGER.info(" -- Not authorized. -- ");
                throw new RuntimeException("Not authorized.");
            }

            LOGGER.info(" -- Authorized! -- ");
            // proceed to actual method call
            return pjp.proceed();

        } finally {
            AuthzDataThreadLocal.remove();
        }
    }
}
