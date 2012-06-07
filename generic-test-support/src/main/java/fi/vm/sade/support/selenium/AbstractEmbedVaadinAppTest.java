package fi.vm.sade.support.selenium;

import com.bsb.common.vaadin.embed.EmbedVaadinServerBuilder;
import com.bsb.common.vaadin.embed.support.EmbedVaadin;
import com.vaadin.Application;
import com.vaadin.ui.Window;

/**
 * Super class for running embed vaadin selenium tests against some vaadin application.
 * Inherits AbstractEmbedVaadinTest, which starts vaadin for single component,
 * and instead of that starts vaadin for Application.
 *
 * NOTE:
 * - remember @ContextConfiguration and to override initPageObjects -method
 *
 * See super class javadoc for more information
 *
 * @see AbstractEmbedVaadinTest
 * @author Antti Salonen
 */
public abstract class AbstractEmbedVaadinAppTest<APP extends Application> extends AbstractEmbedVaadinTest<Window> {

    public AbstractEmbedVaadinAppTest() {
        super(false, true);
    }

    @Override
    protected EmbedVaadinServerBuilder createEmbedVaadinBuilder() {
        Class<APP> appClass = (Class<APP>) findGenericType();
        return EmbedVaadin.forApplication(appClass);
    }

}
