package fi.vm.sade.generic.ui.app;

import org.springframework.beans.factory.annotation.Value;

/**
 * 
 * @author Jussi Jartamo
 * 
 *         Vakiototeutus vuotavien virheiden käsittelyyn. Lisää
 *         application-context.xml:ään niin vuotavat virheet ohjataan
 *         automaattisesti virhe-urliin
 */
public class DefaultExceptionInterceptor implements GenericExceptionInterceptor {

    // voit ylikirjoittaa vakio virhesivun urlin propertyplaceholderilla
    @Value("${virhe.page.url:virhe}")
    private String errorPageUrl;

    public boolean intercept(Throwable exception) {
        return true;
    }

    public String redirect(Throwable exception) {
        return errorPageUrl;
    }

}
