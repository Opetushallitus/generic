package fi.vm.sade.systemtest.liferay;

import fi.vm.sade.support.selenium.SeleniumContext;
import fi.vm.sade.support.selenium.SeleniumTestCaseSupport;
import fi.vm.sade.support.selenium.SeleniumUtils;
import org.openqa.selenium.By;
import org.springframework.test.context.ContextConfiguration;

import static fi.vm.sade.support.selenium.SeleniumUtils.getDriver;

/**
 * @author Antti
 */
@ContextConfiguration(value = "classpath:test-context.xml")
public abstract class AbstractPortalSmokeTst extends SeleniumTestCaseSupport {

    @Override
    public void initPageObjects() {
    }

    public void loginToPortal() {
        log.info("loginToPortal...");
        openRelative(":8180/c/portal/logout"); // first logout
        openRelative(":8180/c/portal/login");
        SeleniumUtils.input("_58_login", "joebloggs");
        SeleniumUtils.input("_58_password", "oph");
        driver.findElement(By.xpath("//input[@type='submit']")).click();
    }

    public String openRelative(String relativeUrl) {
        String url = SeleniumContext.getOphServerUrl() + relativeUrl;
        getDriver().get(url);
        return url;
    }

}
