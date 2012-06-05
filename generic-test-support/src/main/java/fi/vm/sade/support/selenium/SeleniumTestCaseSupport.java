package fi.vm.sade.support.selenium;

import fi.vm.sade.test.util.GlobalWebDriverRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for selenium GUI tests, features:
 *
 *  - selenium start at test setup
 *  - uses single browser window for all tests (via GlobalWebDriverRunner)
 *  - spring integration (via GlobalWebDriverRunner which inherits SpringJUnit4ClassRunner)
 *  - firefox window can be reused event between selenium sessions with 'reuseFirefox'-system property or 'REUSE_FIREFOX'-env param
 *  - supports producing test reports optionally with screenshots and video recording
 *
 *  NOTE!
 *  - describe test steps to reports using TestCaseReporter.STEP or SeleniumUtils.STEP -method
 *  - override initPageObjects() -method
 *  - annotate with test app ctx, eg. @ContextConfiguration("classpath:spring/test-context.xml")
 */
@RunWith(GlobalWebDriverRunner.class)
public abstract class SeleniumTestCaseSupport {

    protected Logger log = LoggerFactory.getLogger(getClass());
    /**
     * driver can also be accessed from GlobalWebDriverRunner.initDriver or SeleniumContext.getDriver
     */
    protected WebDriver driver;
    /**
     * Creates html reports from testrun (note! screenshots off by default)
     */
    @Rule
    public TestCaseReporter testCaseReporter = new TestCaseReporter(this);
    /**
     * records a video out of testrun (note! disabled by default)
     */
    @Rule
    public TestCaseVideoRecorder videoRecorder = new TestCaseVideoRecorder();

    public SeleniumTestCaseSupport() {
        SeleniumContext.setTestCase(this);

        log.info("test running with:"
           + "\n\tbaseUrl: " + SeleniumContext.getBaseUrl()
            + "\n\tdemoMode: " + TestUtils.isDemoMode()
            + "\n\tscreenshot recording: " + testCaseReporter.isTakeScreenshots()
            + "\n\tvideo recording: " + videoRecorder.isTakeVideo());
        
        TestUtils.initI18N();
    }

    public SeleniumTestCaseSupport(WebDriver driver) {
        this();
        this.driver = driver;
    }

    @Before
    public void setUp() throws Exception {
        startSelenium();
        initPageObjects();
    }

    protected void startSelenium() {
        driver = GlobalWebDriverRunner.initDriver();
        // maximize browser window - http://stackoverflow.com/questions/3189430/how-do-i-maximize-the-browser-window-using-webdriver-selenium-2
        //driver.manage().window().maximize();
        SeleniumContext.setDriver(driver);
        log.info("selenium start, baseUrl: {}", new Object[]{SeleniumContext.getBaseUrl()});
    }

    @After
    public void tearDown() throws Exception {
        //driver.quit(); don't quit here will be quit by GlobalWebDriverRunner after whole testsuite has ran
    }

    public abstract void initPageObjects();

}
