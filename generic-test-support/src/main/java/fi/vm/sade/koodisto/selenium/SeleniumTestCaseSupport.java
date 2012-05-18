package fi.vm.sade.koodisto.selenium;

import fi.vm.sade.generic.common.I18N;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
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

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.fail;
import static org.monte.media.FormatKeys.*;
import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.VideoFormatKeys.*;

public abstract class SeleniumTestCaseSupport {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    public static final int TIME_OUT_IN_SECONDS = 10;
    public static final int SLEEP_IN_MILLIS = 3000;
    protected WebDriver driver;
    protected String ophServerUrl = "http://localhost";
    protected String mode;
    private static final String MODE_PORTAL = "portal";

    public static final int DEMOSLEEP = 5000;
    protected boolean demoMode = false;
    /**
     * Flag that enables or disables tests still image capturing.
     */
    protected boolean takeScreenshots = true;
    /**
     * Flag that enables or disables tests video recording.
     */
    protected boolean takeVideo = true;
    /**
     * Flag that enables or disables fix for Mac focus problem. This is actually done by disabling screenshot and video capture.
     */
    protected boolean useMacFix = true;
    protected String previousStep;
    protected Throwable failure;
    private StringBuffer testReport = new StringBuffer();

    String testName;

    protected ScreenRecorder screenRecorder;

    @Rule
    public SeleniumTestWatcher testWatcher = new SeleniumTestWatcher(this);

    public SeleniumTestCaseSupport() {
        ophServerUrl = getEnvOrSystemProperty(ophServerUrl, "OPH_SERVER_URL", "ophServerUrl");
        mode = getEnvOrSystemProperty(ophServerUrl, "SELENIUM_MODE", "seleniumMode");
        String demoModeValue = getEnvOrSystemProperty(null, "DEMO_MODE", "demoMode");
        demoMode = (demoModeValue != null && !demoModeValue.equals("false"));
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

        maybeFixMacFocus();

        // maximize browser window - http://stackoverflow.com/questions/3189430/how-do-i-maximize-the-browser-window-using-webdriver-selenium-2
        //driver.manage().window().maximize();

        appendTestReport("<html><body><table border='1'>");
        STEP("TEST: " + testName);

        initPageObjects();
    }
    
    public SeleniumTestCaseSupport(WebDriver driver) {
        this();
        this.driver = driver;
    }
    
    public boolean modePortal() {
        return MODE_PORTAL.equals(mode);
    }

    protected String getEnvOrSystemProperty(String originalValue, String envVariableName, String systemPropertyName) {
        if (System.getenv(envVariableName) != null) {
            originalValue = System.getenv(envVariableName);
        }
        if (System.getProperty(systemPropertyName) != null) {
            originalValue = System.getProperty(systemPropertyName);
        }
        return originalValue;
    }

    @After
    public void tearDown() throws Exception {
//        driver.quit(); tehdäänkin rulella
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
        driver.switchTo().window(""); // firefox ei lauo onchange jos ikkuna ei ole aktiivinen - http://code.google.com/p/selenium/issues/detail?id=157
        WebElement element = getWebElementForDebugId(elementId);
        element.click();
        element.clear();
        element.sendKeys(text);
        //fireOnChange(element);
        driver.findElement(By.xpath("//body")).click(); // click outside the element to launch validation etc javascripts
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

    public String getRelativePath() {
        throw new RuntimeException("override this method (some problem with abstract method vs aspectj compiler)");
    }

    public String getRelativePathPortal() {
        throw new RuntimeException("override this method (some problem with abstract method vs aspectj compiler)");
    }

    public void open(final String lang) {
        // TODO: portti järkevämmin
        log.info("open, lang: " + lang + ", portalMode: " + modePortal());
        if (modePortal()) {
            loginToPortal();
            openRelative(":8180" + getRelativePathPortal() + "?restartApplication&lang=" + lang);
        } else {
            openRelative(":8080" + getRelativePath() + "?restartApplication&lang=" + lang);
        }
//        openRelative(":8080/organisaatio-app?restartApplication");
//        openRelative(":8080/organisaatio-app?lang="+lang);
        (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).until(new ExpectedCondition<Boolean>() {

            public Boolean apply(WebDriver d) {
                return driver.getPageSource().contains("Organisaatiot");
            }

        });
    }

    public void loginToPortal() {
        log.info("loginToPortal...");
        openRelative(":8180/c/portal/logout"); // first logout
        openRelative(":8180/c/portal/login");
        input("_58_login", "test@liferay.com");
        input("_58_password", "test");
        driver.findElement(By.xpath("//input[@type='submit']")).click();
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
            if (demoMode) {
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

    void writeReport() {
        try {
            appendTestReport("</table></body></html>");
            if (failure == null) {
                appendTestReport("<h3>SUCCESS</h3>");
            } else {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                failure.printStackTrace(pw);
                pw.close();
                appendTestReport("<h3>FAILURE</h3><pre>" + sw + "</pre>");
            }
            File reportFile = new File(getReportDir(), testName + "-report.html");
            FileUtils.writeStringToFile(reportFile, testReport.toString());
            log.info("WROTE REPORT: " + reportFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getReportDir() {
        return new File("target/failsafe-reports/selenium-reports");
    }

    /**
     * If running on Mac OS and useMacFix flag is enabled, this will turn off video and still image capturing
     * as it will interfere window focus.
     */
    public void maybeFixMacFocus() {

        final String os = System.getProperty("os.name");
        if (os.startsWith("Mac")) {

            if (!useMacFix) {
                log.warn("WARN: you are running a Mac OS and macFix is disabled. This may cause some tests to fail. ");
                return;
            }

            takeScreenshots = false;
            takeVideo = false;

        }

    }

    public void STEP(String description) {
        log.info("STEP description: " + description.replaceAll("\n", ""));
        previousStep = description;

        // screenshot
        String screehshotPath = null;
        if (takeScreenshots) {
            try {
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String relativePath = "screenshots/" + screenshot.getName();
                File destFile = new File(getReportDir(), relativePath);
                FileUtils.moveFile(screenshot, destFile);
                log.info("SCREENSHOT: " + destFile.getAbsolutePath());
                //screehshotPath = "file:///"+destFile.getAbsolutePath().replaceAll("\\\\", "/");
                screehshotPath = relativePath;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        appendTestReport("<tr><td>" + convert(description) + "</td>");
        appendTestReport("<td><a href='" + screehshotPath + "'>screenshot</a></td>");
        appendTestReport("</tr>");

        if (demoMode) {

            try {

                // otetaan alkuperäisen ikkunan handle talteen
                String originalWindow = driver.getWindowHandle();

                // näytetään infodialogi htmlsivulla (alert)
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String secondAlert = "hackDialogToIgnoreSeleniumBug"; // selenium hajoaa jos jotain dialogia ei suljeta seleniumilla, siksi toinen alert perään
                js.executeScript("alert('" + convert(description) + "');" + "alert('" + secondAlert + "');");
//                js.executeScript("alert('"+ convert(description)+ "');");
                /*
                 * String okbtnonclick = "document.body.removeChild(document.getElementById('stepdlg'));"; //
                 * js.executeScript("window.onerror=function(msg){document.body.setAttribute('JSError',msg);}"); for (int i=0; i<5; i++) { try {
                 * js.executeScript("" + "var dlg = document.createElement('div');" + "dlg.setAttribute('id', 'stepdlg');" + "dlg.setAttribute('style',
                 * 'position:fixed;top:30%;left:30%;width:300px;height:300px;background-color:#eee;border:2px solid black;z-index:99999;');" +
                 * "dlg.innerHTML='<h3>STEP</h3>" + convert(description) + "<br/>';" + "var okbtn = document.createElement('input');" // +
                 * "okbtn.setAttribute('style', 'z-index:99999;');" + "okbtn.setAttribute('type', 'button');" + "okbtn.setAttribute('value', 'OK');" +
                 * "okbtn.setAttribute('onclick', \"" + okbtnonclick + "\");" + "dlg.appendChild(okbtn);" + "document.body.appendChild(dlg);" + "return 0;" +
                 * ""); break; } catch (WebDriverException e) { // log.info("step inject html error: "+e); throw e; // log.info("failed to
                 * inject STEP html, trying again..."); // if (i == 5-1) { // throw new RuntimeException("failed to inject STEP html", e); // } //
                 * Thread.sleep(1000); } } // odotetaan että dialogi ilmestyy By byDlg = By.xpath("//h3[contains(.,'STEP')]"); // waitForElement(byDlg); //
                 * log.info("step dialog found");
                 */

                // odotetaan kunnes käyttäjä klikkaa "jatka/ok"
                Alert alert = null;
                while (true) {
                    Thread.sleep(100);
                    /*
                     * try { WebElement elem = driver.findElement(byDlg); //log.info("step dialog: "+elem); } catch (Exception e) { break; //
                     * log.info(e); }
                     */
                    try {
                        alert = driver.switchTo().alert();
                        if (alert.getText().equals(secondAlert)) {
                            alert.accept();
                            driver = driver.switchTo().window(originalWindow);
                            break;
                        }
                    } catch (NoAlertPresentException e) {
                        // first alert dismissed by user
//                        try {
//                            js.executeScript("alert('" + secondAlert + "');");
//                        } catch (Exception e2) {
//                            e2.printStackTrace();
                        break;
//                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
//                log.warn("STEP FAILED! step: "+description.replaceAll("\n","")+", exception: "+e);
//                STEP(description);
                throw new RuntimeException(e);
            }

        }
    }

    private void appendTestReport(String s) {
        testReport.append(s);
    }

    public static String convert(String str) {
        return str.replaceAll("ä", "\u00e4").replaceAll("ö", "\u00f6").replaceAll("'", "\\\\'").replaceAll("\n", "<br/>");
    }

    @Before
    public void startVideo() throws Exception {

        if (takeVideo) {

            try {

                GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                log.info("GraphicsDevices: " + devices.length);
                for (GraphicsDevice device : devices) {
                    log.info("    GraphicsDevice - id: " + device.getIDstring() + ", type: " + device.getType() + ", device: " + device);
                }

                GraphicsConfiguration gc = GraphicsEnvironment//
                        .getLocalGraphicsEnvironment()//
                        .getDefaultScreenDevice()//
                        .getDefaultConfiguration();

                // Create a instance of ScreenRecorder with the required configurations
                screenRecorder = new ScreenRecorder(gc,
                        new Format(MediaTypeKey, FormatKeys.MediaType.FILE, MimeTypeKey, MIME_AVI),
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                                DepthKey, (int) 24, FrameRateKey, Rational.valueOf(15),
                                QualityKey, 1.0f,
                                KeyFrameIntervalKey, (int) (15 * 60)),
                        new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black",
                                FrameRateKey, Rational.valueOf(30)),
                        null);

                // Call the start method of ScreenRecorder to begin recording
                screenRecorder.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @After
    public void stopVideo() throws Exception {

        if (takeVideo) {

            try {

                screenRecorder.stop();

                File video = screenRecorder.getCreatedMovieFiles().get(0);
                //String relativePath = "videos/" + video.getName();
                String relativePath = "videos/" + testName + ".avi";
                File destFile = new File(getReportDir(), relativePath);
                FileUtils.deleteQuietly(destFile);
                FileUtils.moveFile(video, destFile);
                log.info("VIDEO: " + destFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private String msg(String key, String message) {
        return message != null ? message : key;
    }

}
