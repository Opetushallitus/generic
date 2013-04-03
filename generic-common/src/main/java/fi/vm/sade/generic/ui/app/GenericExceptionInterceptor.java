package fi.vm.sade.generic.ui.app;

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
     * Palauttaa virhesivun osoitteen
     * 
     * @param exception
     * @return URL
     */
    String redirect(Throwable exception);
}
