package fi.vm.sade.support.selenium;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

/**
 * @author Antti Salonen
 */
public class SeleniumContext {

    private static ThreadLocal<Logger> log = new ThreadLocal<Logger>();
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<WebDriver>();
    private static ThreadLocal<TestCaseReporter> testCaseReporter = new ThreadLocal<TestCaseReporter>();
    private static ThreadLocal<SeleniumTestCaseSupport> testCase = new ThreadLocal<SeleniumTestCaseSupport>();
    private static ThreadLocal<String> testName = new ThreadLocal<String>();
    private static ThreadLocal<String> baseUrl = new ThreadLocal<String>();

    static {
        String baseUrl = TestUtils.getEnvOrSystemProperty("http://localhost:8080", "BASE_URL", "baseUrl");
        if (baseUrl != null) {
            setBaseUrl(baseUrl);
        }
    }

    public static void setBaseUrl(String baseUrl) {
        SeleniumContext.baseUrl.set(baseUrl);
    }

    private SeleniumContext() {
    }

    public static TestCaseReporter getTestCaseReporter() {
        return testCaseReporter.get();
    }

    public static void setTestCaseReporter(TestCaseReporter testCaseReporter) {
        SeleniumContext.testCaseReporter.set(testCaseReporter);
    }

    public static SeleniumTestCaseSupport getTestCase() {
        return testCase.get();
    }

    public static void setTestCase(SeleniumTestCaseSupport testCase) {
        SeleniumContext.testCase.set(testCase);
    }

    public static String getTestName() {
        return testName.get();
    }

    public static void setTestName(String testName) {
        SeleniumContext.testName.set(testName);
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void setDriver(WebDriver driver) {
        SeleniumContext.driver.set(driver);
    }

    public static Logger getLog() {
        return log.get();
    }

    public static void setLog(Logger log) {
        SeleniumContext.log.set(log);
    }

    public static String getOphServerUrl() {
        return getBaseUrl();
    }

    public static String getBaseUrl() {
        return baseUrl.get();
    }

}
