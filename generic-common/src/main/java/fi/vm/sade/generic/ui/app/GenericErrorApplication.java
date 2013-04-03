package fi.vm.sade.generic.ui.app;

import org.springframework.beans.factory.annotation.Value;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.vaadin.Oph;
import fi.vm.sade.vaadin.constants.UiConstant;
import fi.vm.sade.vaadin.ui.OphAbstractWindow;

/**
 * 
 * @author Jussi Jartamo
 * 
 *         Yleiskäyttöinen virhesivu sovellus. Lokaalimerkit voi ylikirjoittaa
 *         springin propertyplaceholderilla.
 */
public class GenericErrorApplication extends AbstractSpringContextApplication {

    @Value("${virhe.i18n.title:application.virhe.title}")
    private String applicationTitle;

    @Value("${virhe.i18n.header:virhe.title}")
    private String applicationHeader;

    protected void initialize() {
        setMainWindow(new OphAbstractWindow(I18N.getMessage(applicationTitle), "100%",
                UiConstant.DEFAULT_RELATIVE_SIZE, true, null) {

            @Override
            public void buildLayout(VerticalLayout layout) {
                layout.setWidth("100%");
                layout.setMargin(true);
                layout.setSpacing(true);
                Label title = new Label(I18N.getMessage(applicationHeader));
                title.addStyleName(Oph.LABEL_H1);

                layout.addComponent(title);
            }

            private static final long serialVersionUID = 2713710441212947434L;
        });
    }

    /**
     * 
     */
    private static final long serialVersionUID = 3804211811175850719L;

}
