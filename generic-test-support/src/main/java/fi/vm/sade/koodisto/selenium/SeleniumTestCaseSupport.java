package fi.vm.sade.koodisto.selenium;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SeleniumTestCaseSupport {

    private final Logger log = LoggerFactory.getLogger(getClass());
    public static final int TIME_OUT_IN_SECONDS = 10;
    public static final int SLEEP_IN_MILLIS = 3000;
    protected WebDriver driver;
    protected String ophServerUrl = "http://localhost";

    public SeleniumTestCaseSupport() {
        
    }
    
    public SeleniumTestCaseSupport(boolean createDriver) {
        
    }

    public SeleniumTestCaseSupport(WebDriver driver) {
        this.driver = driver;
    }
    
    @Before
    public void setUp() throws Exception {
        if (driver == null) {
            try {
                FirefoxProfile profile = new FirefoxProfile();
                //profile.setEnableNativeEvents(false); // disable update firefox etc dialogs
                driver = new FirefoxDriver(profile);
            } catch (Exception e) {
                log.warn("selenium failed to initialize firefox, falling back to htmlunit");
                driver = new HtmlUnitDriver();
                ((HtmlUnitDriver) driver).setJavascriptEnabled(true);
            }
        }

        if (System.getenv("OPH_SERVER_URL") != null) {
            ophServerUrl = System.getenv("OPH_SERVER_URL");
        }
        if (System.getProperty("ophServerUrl") != null) {
            ophServerUrl = System.getProperty("ophServerUrl");
        }
        log.info("selenium using ophServerUrl: %s", ophServerUrl);

    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
    }

    public void waitForPageSourceContains(final String relativeUrl, final String expectedContains) {
        (new WebDriverWait(driver, TIME_OUT_IN_SECONDS, SLEEP_IN_MILLIS)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                String url = openRelative(relativeUrl);
                boolean contains = driver.getPageSource().contains(expectedContains);
                System.out.println(this.getClass().getSimpleName() + " - url: " + url + ", expectedContains: " + expectedContains + ", contains: " + contains);
//                if (!contains) {
//                    System.out.println(this.getClass().getSimpleName()+" - page source: "+driver.getPageSource());
//                }
                return contains;
            }
        });
    }

    public String openRelative(String relativeUrl) {
        String url = ophServerUrl + relativeUrl;
        driver.get(url);
        return url;
    }


}
