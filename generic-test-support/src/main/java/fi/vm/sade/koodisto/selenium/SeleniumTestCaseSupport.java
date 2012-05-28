package fi.vm.sade.koodisto.selenium;

import fi.vm.sade.generic.common.I18N;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.fail;

public abstract class SeleniumTestCaseSupport {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    public static final int TIME_OUT_IN_SECONDS = 10;
    public static final int SLEEP_IN_MILLIS = 3000;
    protected WebDriver driver;
    protected String ophServerUrl = "http://localhost";
    protected String mode;
    private static final String MODE_PORTAL = "portal";

    public static final int DEMOSLEEP = 5000;
    /**
     * Flag that enables or disables fix for Mac focus problem. This is actually done by disabling screenshot and video capture.
     */
    protected boolean useMacFix = true;
    protected Throwable failure;

    String testName;

    @Rule
    public TestCaseReporter testCaseReporter = new TestCaseReporter(this);
    @Rule
    public TestCaseVideoRecorder videoRecorder = new TestCaseVideoRecorder();

    public SeleniumTestCaseSupport() {
        
        ophServerUrl = TestUtils.getEnvOrSystemProperty(ophServerUrl, "OPH_SERVER_URL", "ophServerUrl");
        mode = TestUtils.getEnvOrSystemProperty(null, "SELENIUM_MODE", "seleniumMode");

        log.info("test running with:"
            + "\n\tophServerUrl: " + ophServerUrl
            + "\n\tmode: " + mode
            + "\n\tdemoMode: " + TestUtils.isDemoMode()
            + "\n\tscreenshot recording: " + testCaseReporter.isTakeScreenshots()
            + "\n\tvideo recording: " + videoRecorder.isTakeVideo());
        
        initI18N();

    }

    protected void initI18N() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        I18N.setMessageSourceAccessor(new MessageSourceAccessor(new MessageSource() {

            @Override
            public String getMessage(String s, Object[] objects, String s1, Locale locale) {
                try {
                    return msg(s, messageSource.getMessage(s, objects, s1, I18N.getLocale()));
                } catch (NoSuchMessageException e) {
                    return s;
                }
            }

            @Override
            public String getMessage(String s, Object[] objects, Locale locale) throws NoSuchMessageException {
                try {
                    return msg(s, messageSource.getMessage(s, objects, I18N.getLocale()));
                } catch (NoSuchMessageException e) {
                    return s;
                }
            }

            @Override
            public String getMessage(MessageSourceResolvable messageSourceResolvable, Locale locale) throws NoSuchMessageException {
                return msg(null, messageSource.getMessage(messageSourceResolvable, I18N.getLocale()));
            }

        }));
        /*
         * I18N.setMessageSourceAccessor(new MessageSourceAccessor(null) { @Override public String getMessage(String code, Locale locale) throws
         * NoSuchMessageException { try { Properties messages = new Properties();
         * messages.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("i18n/messages_fi_FI.properties")); String msg =
         * messages.getProperty(code); log.info("MSG: " + code + " - " + msg); return msg != null ? msg : code; } catch (IOException e) {
         * log.info("OrganisaatioSeleniumTstCaseSupport.I18N.getMessage warn, code: " + code + ", exception: " + e); return code; } } });
         */
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

        log.info("selenium start, ophServerUrl: {}, mode: {}, portalMode: {}", new Object[]{ophServerUrl, mode, modePortal()});

        // maximize browser window - http://stackoverflow.com/questions/3189430/how-do-i-maximize-the-browser-window-using-webdriver-selenium-2
        //driver.manage().window().maximize();

        initPageObjects();
    }
    
    public SeleniumTestCaseSupport(WebDriver driver) {
        this();
        this.driver = driver;
    }
    
    public boolean modePortal() {
        return MODE_PORTAL.equals(mode);
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
                log.debug(this.getClass().getSimpleName() + " - url: " + url + ", expectedContains: " + expectedContains + ", contains: " + contains);
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
    
    public void selectCustom(WebElement element, final String optionText) {
        final WebElement btn = element.findElement(By.xpath("//div[@class='v-filterselect-button']"));
        
//        btn.click();
//        WebElement option = waitForElement(By.xpath("//td[@class='gwt-MenuItem']/span[contains(.,'" + text + "')]"));
        WebElement option = wait("KoodistoComponent not found: " + optionText, new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                btn.click();
                return driver.findElement(By.xpath("//td[@class='gwt-MenuItem']/span[contains(.,'" + optionText + "')]"));
            }

        });
        option.click();
    }

    public void select(WebElement element, final String optionText) {
        final WebElement btn = element.findElement(By.xpath("div[@class='v-filterselect-button']"));
//        btn.click();
//        WebElement option = waitForElement(By.xpath("//td[@class='gwt-MenuItem']/span[contains(.,'" + text + "')]"));
        WebElement option = wait("option not found: " + optionText, new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                btn.click();
                return driver.findElement(By.xpath("//td[@class='gwt-MenuItem']/span[contains(.,'" + optionText + "')]"));
            }

        });
        option.click();
    }

    public void input(String elementId, String text) {
        input(elementId, text, true, true);
    }

    public void input(WebElement element, String text) {
        input(element, text, true, true);
    }

    public void input(String elementId, String text, boolean fastInput, boolean clickOutsideAfter) {
        WebElement element = getWebElementForDebugId(elementId);
        input(element, text, fastInput, clickOutsideAfter);
    }

    public void input(WebElement element, String text, boolean fastInput, boolean clickOutsideAfter) {
        driver.switchTo().window(""); // firefox ei lauo onchange jos ikkuna ei ole aktiivinen - http://code.google.com/p/selenium/issues/detail?id=157
        element.click();
        if (fastInput) {
            setValue(element, text);
        } else {
            element.clear();
            element.sendKeys(text);
        }
        //fireOnChange(element);
        if (clickOutsideAfter) {
            driver.findElement(By.xpath("//body")).click(); // click outside the element to launch validation etc javascripts
        }
    }

    public void setValue(WebElement element, String value) {
        ((JavascriptExecutor)driver).executeScript("arguments[0].value = arguments[1]", element, value);
    }

    public WebElement click(final By by) {
        return wait("failed to click element: " + by, new ExpectedCondition<WebElement>() {
            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                try {
                    WebElement element = driver.findElement(by);
                    element.click();
                    return element;
                } catch (Exception e) {
                    log.warn("OrganisaatioSeleniumTstCaseSupport.click - WARNING: " + e);
                    return null;
                }
            }
        });
    }

    public WebElement getWebElementForDebugId(String debugId) {
        return driver.findElement(By.id(debugId));
    }

    public WebElement getWebElementForXpath(String xpath) {
        return driver.findElement(By.xpath(xpath));
    }

    public <T> T wait(String errorMsg, ExpectedCondition<T> expectedCondition) {
        try {
            T result = new WebDriverWait(driver, TIME_OUT_IN_SECONDS).until(expectedCondition);
            return result;
        } catch (Throwable e) {
            if (e instanceof StaleElementReferenceException) {
                log.warn("OrganisaatioSeleniumTstCaseSupport.wait - WARNING: " + e);
                return null;
            } else {
                fail(errorMsg + " --- (" + e + ")");
                throw new RuntimeException("should have failed already");
            }
        }
    }

    public void initPageObjects() {
        throw new RuntimeException("override this method (some problem with abstract method vs aspectj compiler)");
    }

    public void assertMessageKey(final String key) {
        assertMessageKey(key, "//div[@id[contains(.,'serverMessage')]]", null);//='serverMessage']", null);
    }

    public void assertErrorKey(final String key, boolean dismiss) {
        assertMessageKey(key, null, "v-Notification-error");
        if (dismiss) {
            dismissError();
        }
    }

    public void assertError(final String text, boolean dismiss) {
        assertMessage(text, null, "v-Notification-error");
        if (dismiss) {
            dismissError();
        }
    }

    private void dismissError() {
        WebElement errordlg = driver.findElement(By.cssSelector(".v-Notification-error"));
        errordlg.click();
        waitForElementNotDisplayed("error dialog not dismissed", errordlg);
    }

    public void assertMessageKey(final String expectedMessageKey, final String id, final String cssClassName) {
        final String expectedMessage = I18N.getMessage(expectedMessageKey);
        assertMessage(expectedMessage, id, cssClassName);
    }

    public void assertMessage(final String expectedMessage, final String id, final String cssClassName) {
        try {
            (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).until(new ExpectedCondition<Boolean>() {

                public Boolean apply(WebDriver d) {
                    return getMessage(id, cssClassName).equals(expectedMessage);
                }

            });
            if (TestUtils.isDemoMode()) {
                Thread.sleep(DEMOSLEEP);
            }
        } catch (Exception e) {
            fail("assertMessage failed in timeout, expected message: " + expectedMessage + ", but was: " + getMessage(id, cssClassName));
        }
    }

    public String getMessage(String xpath, String cssClassName) {
        if (xpath != null) {
            WebElement element = driver.findElement(By.xpath(xpath));
            log.info("ELEM: " + element.getAttribute("id") + " - " + element.getText());
            return element.getText();
        } else {
            return driver.findElement(By.className(cssClassName)).getText();
        }
    }

    public WebElement waitForText(final String text) {
        return wait("text not found in time: " + text, new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                return driver.findElement(By.xpath("//*[contains(.,'" + text + "')]"));
            }

        });
    }

    public WebElement waitForElement(final By by) {
        return wait("element not found in time: " + by, new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                return driver.findElement(by);
            }

        });
    }

    public WebElement waitForElement(final WebElement parent, final By by) {
        return wait("element not found in time: " + by, new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                return parent.findElement(by);
            }

        });
    }

    public List<String> getOptions(WebElement element) {
        List<String> result = new ArrayList<String>();
        // click to open options
        WebElement btn = element.findElement(By.xpath("div[@class='v-filterselect-button']"));
        btn.click();
        // get options elems
        By byOptions = By.xpath("//td[@class='gwt-MenuItem']/span");
        waitForElement(byOptions);
        List<WebElement> optionElems = driver.findElements(byOptions);
        // get options strings
        for (WebElement optionElem : optionElems) {
            result.add(optionElem.getText());
        }
        // close options and return
        driver.findElement(By.tagName("body")).click(); // click outside options to hide them
        return result;
    }

    public void waitForElementNotDisplayed(String errorMsg, final WebElement element) {
        wait(errorMsg, new ExpectedCondition<Boolean>() {

            @Override
            public Boolean apply(@Nullable WebDriver webDriver) {
                try {
                    boolean displayed = element.isDisplayed();
                    log.warn("elem not displ: " + displayed);
                    return !displayed;
                } catch (StaleElementReferenceException e) {
                    log.warn("elem not displ: " + e);
                    return true;
                }
            }

        });
    }

    public void STEP(String description) {
        testCaseReporter.STEP(description, driver, log);
    }

    private String msg(String key, String message) {
        return message != null ? message : key;
    }

}
