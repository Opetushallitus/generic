package fi.vm.sade.support.selenium;

import com.vaadin.ui.Component;
import org.openqa.selenium.WebDriver;

/**
 * Superclass for pageobjects in embed vaadin tests
 *
 * @author Antti Salonen
 */
public class VaadinPageObjectSupport<COMPONENT extends Component> extends SeleniumTestCaseSupport {

    protected COMPONENT component;

    public VaadinPageObjectSupport(WebDriver driver, COMPONENT component) {
        super(driver);
        this.component = component;
    }

    @Override
    public void initPageObjects() {
    }

    public COMPONENT getComponent() {
        return component;
    }
}
