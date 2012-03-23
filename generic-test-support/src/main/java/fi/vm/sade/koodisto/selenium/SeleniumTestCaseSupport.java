package fi.vm.sade.koodisto.selenium;

import junit.framework.TestCase;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class SeleniumTestCaseSupport extends TestCase {

    protected WebDriver driver;
    protected String ophServerUrl = "http://localhost";

    public SeleniumTestCaseSupport() {
        try {
            driver = new FirefoxDriver();
        } catch (Exception e) {
            System.out.println("selenium failed to initialize firefox, falling back to htmlunit");
            driver = new HtmlUnitDriver();
            ((HtmlUnitDriver)driver).setJavascriptEnabled(true);
        }
        
        if (System.getenv("OPH_SERVER_URL") != null) {
            ophServerUrl = System.getenv("OPH_SERVER_URL");
        }
        if (System.getProperty("ophServerUrl") != null) {
            ophServerUrl = System.getProperty("ophServerUrl");
        }
        System.out.println("selenium using ophServerUrl: "+ophServerUrl);
    }

    @Override
    protected void tearDown() throws Exception {
        driver.quit();
        super.tearDown();
    }

    protected void waitForPageSourceContains(final String relativeUrl, final String expectedContains) {
        (new WebDriverWait(driver, 10, 3)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                String url = ophServerUrl+ relativeUrl;
                driver.get(url);
                boolean contains = driver.getPageSource().contains(expectedContains);
                System.out.println(this.getClass().getSimpleName()+" - url: "+url+", expectedContains: "+expectedContains+", contains: "+contains);
//                if (!contains) {
//                    System.out.println(this.getClass().getSimpleName()+" - page source: "+driver.getPageSource());
//                }
                return contains;
            }
        });
    }

}
