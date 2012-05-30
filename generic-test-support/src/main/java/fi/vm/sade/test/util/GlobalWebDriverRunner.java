package fi.vm.sade.test.util;

import fi.vm.sade.koodisto.selenium.TestUtils;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
 * - firefox window can bse reused with 'reuseFirefox'-system property or 'REUSE_FIREFOX'-env param
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
        // if we already have driver, return it
        if (globalDriver != null) {
            return globalDriver;
        }

        // otherwise create global driver
        try {
            FirefoxProfile profile = new FirefoxProfile();
            //profile.setEnableNativeEvents(false); // disable update firefox etc dialogs
            if (reuseFirefox()) {
                LOG.info("reuseFirefox=true");
                globalDriver = new ReusableFirefoxDriver(profile);
            } else {
                globalDriver = new FirefoxDriver(profile);
            }
        } catch (Exception e) {
            LOG.warn("selenium failed to initialize firefox, falling back to htmlunit");
            globalDriver = new HtmlUnitDriver();
            ((HtmlUnitDriver) globalDriver).setJavascriptEnabled(true);
        }
        return globalDriver;
    }

    private static boolean reuseFirefox() {
        return TestUtils.getEnvOrSystemPropertyAsBoolean(false, "REUSE_FIREFOX", "reuseFirefox");
    }

    @Override
    public void run(RunNotifier notifier) {
        if (!inited) {
            inited = true;
            notifier.addListener(new RunListener(){
                @Override
                public void testRunFinished(Result result) throws Exception {
                    if (!reuseFirefox()) {
                        globalDriver.quit();
                    }
                }
            });
        }
        super.run(notifier);
    }
}
