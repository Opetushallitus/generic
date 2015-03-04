package fi.vm.sade.test.util;

import fi.vm.sade.support.selenium.SeleniumContext;
import fi.vm.sade.support.selenium.TestUtils;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.Arrays;

/**
 * Run selenium tests with same browser window and quit it after all tests are finished.
 *
 * USAGE:
 *
 * - annotate test class with @RunWith(GlobalWebDriverRunner.class)
 * - init webdriver with like this: driver = GlobalWebDriverRunner.initDriver();
 *
 * InitDriver -method will return existing GlobalWebDriverRunner.globalDriver (or create new when first called),
 * and GlobalWebDriverRunner will quit the driver when last test is finished.
 *
 * NOTE:
 *
 * - firefox window can be reused event between selenium sessions with 'reuseFirefox'-system property or 'REUSE_FIREFOX'-env param
 *
 * @author Antti Salonen
 */
public class GlobalWebDriverRunner extends SpringJUnit4ClassRunner {

    protected static final Logger LOG = LoggerFactory.getLogger(GlobalWebDriverRunner.class);
    public static WebDriver globalDriver;
    private boolean inited = false;

    public GlobalWebDriverRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    public static WebDriver initDriver() {
        quitDriverIfNeeded();

        // if we already have driver, return it
        if (globalDriver != null) {
            LOG.info("reusing driver: "+globalDriver);
        }

        // if phantomJS
        else if ("phantomjs".equals(TestUtils.getEnvOrSystemProperty(null, "BROWSER", "browser"))) {
            globalDriver = createDriverPhantomJS();
        }

        // if chrome
        else if ("chrome".equals(TestUtils.getEnvOrSystemProperty(null, "BROWSER", "browser"))) {
            globalDriver = createDriverChrome();
        }

        // if firefox
        else {
            globalDriver = createDriverFirefoxOrHtmlUnit();
        }

        return globalDriver;
    }

    public static WebDriver createDriverChrome() {
        // NOTE: vaatii driverit am polkuihin

        // driver binary path depending on os
        String binpath;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            binpath = "chromedriver/chromedriver_win32/chromedriver.exe";
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            binpath = "chromedriver/chromedriver_mac32/chromedriver";
        } else {
            if (System.getProperty("os.arch").contains("64")) {
                binpath = "chromedriver/chromedriver_linux64/chromedriver";
            } else {
                binpath = "chromedriver/chromedriver_linux32/chromedriver";
            }
        }
        System.setProperty("webdriver.chrome.driver", binpath);
        new File(binpath).setExecutable(true);

        // prepare chromedriver
        String[] switches = {"--ignore-certificate-errors"};
        DesiredCapabilities dc = DesiredCapabilities.chrome();
        dc.setCapability("chrome.switches", Arrays.asList(switches));
        LOG.info("creating chromedriver, binary: " + binpath);
        return new ChromeDriver(dc);
    }

    public static WebDriver createDriverPhantomJS() {
        // NOTE: vaatii driverit tuolta: http://phantomjs.org/download.html - ei vaadikaan jos arquilian toimii - mutta ei tunnu toimivan
        // driver binary path depending on os
        String binpath;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            binpath = "phantomjs/phantomjs-1.9.2-windows/phantomjs.exe";
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            binpath = "phantomjs/phantomjs-1.9.2-macosx/bin/phantomjs";
        } else {
            if (System.getProperty("os.arch").contains("64")) {
                binpath = "phantomjs/phantomjs-1.9.2-linux-x86_64/bin/phantomjs";
            } else {
                binpath = "phantomjs/phantomjs-1.9.2-linux-i686/bin/phantomjs";
            }
        }
        new File(binpath).setExecutable(true);

        // prepare driver
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, binpath);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[]{"--ignore-ssl-errors=yes"});
        PhantomJSDriver driver = new PhantomJSDriver(caps);
        LOG.info("created phantomjs driver, binary: "+binpath);
        return driver;
    }

    public static WebDriver createDriverFirefoxOrHtmlUnit() {
        // otherwise create global driver
        WebDriver driver;
        try {
            FirefoxProfile profile = new FirefoxProfile();
            profile.setAcceptUntrustedCertificates(true);
            profile.setAssumeUntrustedCertificateIssuer(false);
            /* disable reuse - todo: jos käyttöön niin driver.manage().deleteAllCookies();
            if (reuseFirefox()) {
                LOG.info("reuseFirefox=true");
                globalDriver = new ReusableFirefoxDriver(profile);
            } else {
            */
            driver = new FirefoxDriver(profile);
            /* disable reuse
            }
            */
        } catch (Exception e) {
            LOG.warn("selenium failed to initialize firefox, falling back to htmlunit");
            driver = new HtmlUnitDriver();
            ((HtmlUnitDriver) driver).setJavascriptEnabled(true);
        }
        return driver;
    }

    /* disable reuse
    private static boolean reuseFirefox() {
        return TestUtils.getEnvOrSystemPropertyAsBoolean(false, "REUSE_FIREFOX", "reuseFirefox");
    }
    */

    @Override
    public void run(RunNotifier notifier) {
        if (!inited) {
            inited = true;
            notifier.addListener(new RunListener(){
                @Override
                public void testRunFinished(Result result) throws Exception {
                    quitDriverIfNeeded();
                }
            });
        }
        super.run(notifier);
    }

    public static void quitDriverIfNeeded() {
        /* disable reuse
        if (!reuseFirefox()) {
        */
        if (globalDriver != null) {
            globalDriver.quit();
            SeleniumContext.setDriver(null);
            GlobalWebDriverRunner.globalDriver = null;
        }
        /* disable reuse
        }
        */
    }
}
