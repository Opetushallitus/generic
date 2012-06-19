package fi.vm.sade.support.selenium;

import com.vaadin.ui.Component;
import org.openqa.selenium.WebDriver;

/**
 * Superclass for pageobjects in embed vaadin tests
 *
 * @author Antti Salonen
 */
public class VaadinPageObjectSupport<COMPONENT extends Component> {

    protected COMPONENT component;
    protected WebDriver driver;

    public VaadinPageObjectSupport(WebDriver driver) {
        this.driver = SeleniumContext.getDriver();
    }

    public VaadinPageObjectSupport(WebDriver driver, COMPONENT component) {
//        super(driver);
        this.driver = SeleniumContext.getDriver();
        this.component = component;
    }

    public COMPONENT getComponent() {
        return component;
    }
}
