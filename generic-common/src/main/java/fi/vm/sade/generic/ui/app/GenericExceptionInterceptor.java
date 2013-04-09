package fi.vm.sade.generic.ui.app;

import com.vaadin.Application;
import com.vaadin.ui.Window;

/**
 * 
 * @author Jussi Jartamo
 * 
 *         Autowire this service to provide custom error pages
 */
public interface GenericExceptionInterceptor {

    /**
     * Tarkistin ollaanko kyseisestä virheestä kiinnostuneita
     * 
     * @param exception
     *            Caught exception
     * @return Whether or not intercept it or leave it to Application default
     *         error handler
     */
    boolean intercept(Throwable exception);

    /**
     * 
     * @return
     */
    Window getErrorWindow(Application application);
}
