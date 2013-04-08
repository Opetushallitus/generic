package fi.vm.sade.generic.ui.app;

import org.springframework.beans.factory.annotation.Value;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.vaadin.Oph;
import fi.vm.sade.vaadin.constants.UiConstant;
import fi.vm.sade.vaadin.ui.OphAbstractWindow;

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
        // TODO: Tee järkevämmässä paikassa stack trace heitto
        exception.printStackTrace();
        return true;
    }

    public String redirect(Throwable exception) {
        return errorPageUrl;
    }

    @Value("${virhe.i18n.title:application.virhe.title}")
    private String applicationTitle;

    @Value("${virhe.i18n.header:virhe.title}")
    private String applicationHeader;

    public Window getErrorWindow(final Application application) {

        return new OphAbstractWindow(I18N.getMessage(applicationTitle), "100%", UiConstant.DEFAULT_RELATIVE_SIZE, true,
                null) {

            @Override
            public void buildLayout(VerticalLayout layout) {
                layout.setWidth("100%");
                layout.setMargin(true);
                layout.setSpacing(true);
                Label title = new Label(I18N.getMessage(applicationHeader));
                title.addStyleName(Oph.LABEL_H1);
                layout.addComponent(title);
                Button refreshButton = new Button("Yritä uudelleen");
                refreshButton.addListener(new ClickListener() {

                    public void buttonClick(ClickEvent event) {
                        application.close();
                    }

                    private static final long serialVersionUID = -8458336714931849802L;
                });
                layout.addComponent(refreshButton);
            }

            private static final long serialVersionUID = 2713710441212947434L;
        };

    }
}
