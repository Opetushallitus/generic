/**
 * 
 */
package fi.vm.sade.selenium.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.Application;
import com.vaadin.ui.Component;

/**
 * @author tommiha
 *
 */
@Aspect
public class ApplicationAspect implements InitializingBean {

    @Autowired
    private ComponentCatcher catcher;
    
    private boolean initialized = false;
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @After("execution(com.vaadin.Application+.new(..))")
    public void afterApplicationConstruct(JoinPoint pjp) throws Throwable {
        if(!initialized) {
            return;
        }
        //DEBUGSAWAY:logger.debug("Invoked after Application: " + pjp.getTarget());
        Application application = (Application) pjp.getTarget();
        catcher.execute(application);
    }
    
    @After("execution(com.vaadin.ui.Component+.new(..))")
    public void afterComponentConstruct(JoinPoint pjp) throws Throwable {
        if(!initialized) {
            return;
        }
        //DEBUGSAWAY:logger.debug("Invoked after Component: " + pjp.getTarget());
        Component component = (Component) pjp.getTarget();
        catcher.execute(component);
    }

    public ComponentCatcher getCatcher() {
        return catcher;
    }

    public void setCatcher(ComponentCatcher catcher) {
        this.catcher = catcher;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.initialized = true;
    }

}
