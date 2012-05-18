package fi.vm.sade.koodisto.selenium;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Test watcher that will:
 *  - set test name
 *  - initialize and write test report
 *
 * TODO: videointi myös tänne?
 *
* @author Antti
*/
public class SeleniumTestWatcher extends TestWatcher {

    private SeleniumTestCaseSupport seleniumTestCaseSupport;

    public SeleniumTestWatcher(SeleniumTestCaseSupport seleniumTestCaseSupport) {
        this.seleniumTestCaseSupport = seleniumTestCaseSupport;
    }

    @Override
    protected void starting(Description description) {
        seleniumTestCaseSupport.testName = seleniumTestCaseSupport.getClass().getSimpleName() + "." + description.getMethodName();
    }

    @Override
    protected void failed(Throwable e, Description description) {
        seleniumTestCaseSupport.log.info("TestWatcher.failed: " + e);
        seleniumTestCaseSupport.failure = e;
        seleniumTestCaseSupport.STEP("test FAILED\nstep: " + seleniumTestCaseSupport.previousStep + "\nexception: " + seleniumTestCaseSupport.failure);
        seleniumTestCaseSupport.writeReport();
        seleniumTestCaseSupport.driver.quit();
    }

    @Override
    protected void succeeded(Description description) {
        seleniumTestCaseSupport.log.info("TestWatcher.succeeded");
        seleniumTestCaseSupport.STEP("test OK");
        seleniumTestCaseSupport.writeReport();
        seleniumTestCaseSupport.driver.quit();
    }

}
