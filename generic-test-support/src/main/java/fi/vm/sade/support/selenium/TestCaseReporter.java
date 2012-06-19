package fi.vm.sade.support.selenium;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Test watcher that will:
 *  - set test name for
 *  - initialize and write test report
 *  - append test steps to the report with screenshotss when STEP(..) -method is called
 *
 *  NOTE! screenshots will be taken only if if 'screenshotMode' systemproperty or 'SCREENSHOT_MODE' envvar is !false
 *
* @author Antti
*/
public class TestCaseReporter extends TestWatcher {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected SeleniumTestCaseSupport seleniumTestCaseSupport;
    protected String previousStep;
    private StringBuffer testReport = new StringBuffer();
    /**
     * Flag that enables or disables tests still image capturing.
     */
    protected boolean takeScreenshots = false;
    private String testName;

    public TestCaseReporter(SeleniumTestCaseSupport seleniumTestCaseSupport) {
        SeleniumContext.setTestCaseReporter(this);
        this.seleniumTestCaseSupport = seleniumTestCaseSupport;
        takeScreenshots = TestUtils.getEnvOrSystemPropertyAsBoolean(takeScreenshots, "SCREENSHOT_MODE", "screenshotMode");
    }

    @Override
    protected void starting(Description description) {
        //seleniumTestCaseSupport.testName = seleniumTestCaseSupport.getClass().getSimpleName() + "." + description.getMethodName();
        SeleniumContext.setTestName(TestUtils.getTestName(description));
        testName = TestUtils.getTestName(description);
        appendTestReport("<html><body><table border='1'>");
        STEP("TEST: " + testName, null, seleniumTestCaseSupport.log, false);
    }

    @Override
    protected void failed(Throwable e, Description description) {
        seleniumTestCaseSupport.log.info("TestWatcher.failed: " + e);
        SeleniumUtils.STEP("test FAILED\nstep: " + previousStep + "\nexception: " + e);
        writeReport(e);
    }

    @Override
    protected void succeeded(Description description) {
        seleniumTestCaseSupport.log.info("TestWatcher.succeeded");
        SeleniumUtils.STEP("test OK");
        writeReport(null);
    }

    public void STEP(String description, WebDriver driver, Logger log, boolean takeScreenshots) {
        log.info("STEP description: " + description.replaceAll("\n", ""));
        previousStep = description;

        // screenshot
        String screenshotHtml = "";
        if (takeScreenshots) {
            try {
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String relativePath = "screenshots/" + screenshot.getName();
                File destFile = new File(TestUtils.getReportDir(), relativePath);
                FileUtils.moveFile(screenshot, destFile);
                log.info("SCREENSHOT: " + destFile.getAbsolutePath());
                //screehshotPath = "file:///"+destFile.getAbsolutePath().replaceAll("\\\\", "/");
                screenshotHtml = "<a href='" + relativePath + "'>screenshot</a>";
            } catch (Exception e) {
                log.info("failed to take screenshot for step: "+description+", exception: "+e);
                e.printStackTrace();
            }
        }

        appendTestReport("<tr><td>" + convert(description) + "</td>");
        appendTestReport("<td>" + screenshotHtml + "</td>");
        appendTestReport("</tr>");

        if (TestUtils.isDemoMode()
                && driver != null) { // TODO: aloitus steppi ei toimi koska driver null siinä vaiheessa

            try {

                // otetaan alkuperäisen ikkunan handle talteen
                String originalWindow = driver.getWindowHandle();

                // näytetään infodialogi htmlsivulla (alert)
                JavascriptExecutor js = (JavascriptExecutor) driver;
                String secondAlert = "hackDialogToIgnoreSeleniumBug"; // selenium hajoaa jos jotain dialogia ei suljeta seleniumilla, siksi toinen alert perään
                js.executeScript("alert('" + convert(description) + "');" + "alert('" + secondAlert + "');");

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
//                throw new RuntimeException(e); // TODO: viimeinenkin step failaa, syy: UnreachableBrowserException
            }

        }
    }

    private void appendTestReport(String s) {
        testReport.append(s);
    }

    void writeReport(Throwable failure) {
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
            File reportFile = new File(TestUtils.getReportDir(), testName + "-report.html");
            FileUtils.writeStringToFile(reportFile, testReport.toString());
            log.info("WROTE REPORT: " + reportFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String convert(String str) {
        return str.replaceAll("ä", "\u00e4").replaceAll("ö", "\u00f6").replaceAll("'", "\\\\'").replaceAll("\n", "<br/>");
    }

    public boolean isTakeScreenshots() {
        return takeScreenshots;
    }
}
