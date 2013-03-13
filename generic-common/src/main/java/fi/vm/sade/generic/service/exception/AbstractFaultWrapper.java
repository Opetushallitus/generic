package fi.vm.sade.generic.service.exception;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFaultWrapper<T extends Exception> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractFaultWrapper.class);

    private Class<T> clazz;

    public AbstractFaultWrapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Defines the pointcut for service interface methods.
     */
    public abstract void serviceMethod();

    /**
     * Around-type advice which simply proceeds to join point but catches thrown
     * exceptions and wraps them.
     * 
     * @param pjp
     * @return
     * @throws GenericFault
     */
    @SuppressWarnings("rawtypes")
    public Object wrapException(ProceedingJoinPoint pjp) throws T {

        try {
            return pjp.proceed();
        } catch (Throwable ex) {

            LOGGER.error("Exception wrapped.", ex);

            // If GenericFault
            MethodSignature sigu = (MethodSignature) pjp.getSignature();
            Class[] types = sigu.getExceptionTypes();
            Set<Class> classSet = new HashSet<Class>(Arrays.asList(types));

            if (classSet.contains(clazz)) {
                throw createFaultInstance(ex);
            } else {
                throw new RuntimeException("Unhandled error: " + ex.getClass() + " - " + ex.getMessage(), ex);
            }
        }
    }

    protected abstract T createFaultInstance(Throwable ex);
}
