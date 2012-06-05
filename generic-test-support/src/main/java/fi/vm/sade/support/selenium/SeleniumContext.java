package fi.vm.sade.support.selenium;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

/**
 * @author Antti Salonen
 */
public class SeleniumContext {

    private static String ophServerUrl = "http://localhost";
    private static ThreadLocal<Logger> log = new ThreadLocal<Logger>();
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<WebDriver>();
    private static ThreadLocal<TestCaseReporter> testCaseReporter = new ThreadLocal<TestCaseReporter>();
    private static ThreadLocal<SeleniumTestCaseSupport> testCase = new ThreadLocal<SeleniumTestCaseSupport>();
    private static ThreadLocal<String> testName = new ThreadLocal<String>();
    private static ThreadLocal<Integer> httpPort = new ThreadLocal<Integer>();
    private static ThreadLocal<String> baseUrl = new ThreadLocal<String>();

    static {
        SeleniumContext.ophServerUrl = TestUtils.getEnvOrSystemProperty(SeleniumContext.ophServerUrl, "OPH_SERVER_URL", "ophServerUrl");
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
        return ophServerUrl;
    }

    public static int getPort() {
        Integer port = httpPort.get();
        if (port == null) {
            return 8080;
        } else {
            return port;
        }
    }

    public static void setHttpPort(int port) {
        SeleniumContext.httpPort.set(port);
    }

    public static String getBaseUrl() {
        return ophServerUrl + ":" + getPort();
    }

}
