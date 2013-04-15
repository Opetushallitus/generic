package fi.vm.sade.generic.ui.app;

import org.springframework.beans.factory.annotation.Value;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.vaadin.Oph;

/**
 * 
 * @author Jussi Jartamo
 * 
 *         Vakiototeutus vuotavien virheiden käsittelyyn. Lisää
 *         application-context.xml:ään niin vuotavat virheet ohjataan
 *         automaattisesti virhe-urliin.
 * 
 *         Muista <context:property-placeholder/>
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

    @Value("${virhe.i18n.title:application.virhe.title}")
    private String applicationTitle;

    @Value("${virhe.i18n.header:application.virhe.header}")
    private String applicationHeader;

    @Value("${virhe.i18n.header:application.virhe.button}")
    private String tryAgain;

    public Window getErrorWindow(final Application application) {
        Window window = new Window(I18N.getMessage(applicationTitle));
        Panel mainPanel = new Panel();
        mainPanel.setHeight("300px");

        VerticalLayout layout = new VerticalLayout();
        window.setTheme(Oph.THEME_NAME);
        layout.setWidth("100%");
        layout.setMargin(true);
        // layout.setSpacing(true);
        Label title = new Label(I18N.getMessage(applicationHeader));
        title.addStyleName(Oph.LABEL_H1);
        layout.addComponent(title);
        Button refreshButton = new Button(I18N.getMessage(tryAgain));
        refreshButton.addListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                try {
                    // application.init();
                    application.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private static final long serialVersionUID = -8458336714931849802L;
        });
        layout.addComponent(refreshButton);
        Label expandingGap = new Label();
        expandingGap.setWidth("100%");
        expandingGap.setHeight("100%");
        layout.addComponent(expandingGap);
        mainPanel.addComponent(layout);
        window.addComponent(mainPanel);

        return window;

    }

}
