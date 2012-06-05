package fi.vm.sade.support.selenium;

import fi.vm.sade.generic.common.I18N;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Utils for selenium tests
 *
 * @author Antti Salonen
 */
public class SeleniumUtils {

    public static final int TIME_OUT_IN_SECONDS = 10;
    public static final int SLEEP_IN_MILLIS = 3000;
    public static final int DEMOSLEEP = 5000;
    private static Logger log = LoggerFactory.getLogger(SeleniumUtils.class);

    public static WebDriver getDriver() {
        return SeleniumContext.getDriver();
    }

    private SeleniumUtils() {
    }

    public static void assertMessageKey(final String key) {
        assertMessageKey(key, "//div[@id[contains(.,'serverMessage')]]", null);//='serverMessage']", null);
    }

    public static void assertErrorKey(final String key, boolean dismiss) {
        assertMessageKey(key, null, "v-Notification-error");
        if (dismiss) {
            dismissError();
        }
    }

    public static void assertError(final String text, boolean dismiss) {
        assertMessage(text, null, "v-Notification-error");
        if (dismiss) {
            dismissError();
        }
    }

    public static void dismissError() {
        WebElement errordlg = getDriver().findElement(By.cssSelector(".v-Notification-error"));
        errordlg.click();
        waitForElementNotDisplayed("error dialog not dismissed", errordlg);
    }

    public static void assertMessageKey(final String expectedMessageKey, final String id, final String cssClassName) {
        final String expectedMessage = I18N.getMessage(expectedMessageKey);
        assertMessage(expectedMessage, id, cssClassName);
    }

    public static void assertMessage(final String expectedMessage, final String id, final String cssClassName) {
        try {
            (new WebDriverWait(getDriver(), TIME_OUT_IN_SECONDS)).until(new ExpectedCondition<Boolean>() {

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

    public static String getMessage(String xpath, String cssClassName) {
        if (xpath != null) {
            WebElement element = getDriver().findElement(By.xpath(xpath));
            log.info("ELEM: " + element.getAttribute("id") + " - " + element.getText());
            return element.getText();
        } else {
            return getDriver().findElement(By.className(cssClassName)).getText();
        }
    }

    public static WebElement waitForText(final String text) {
        return waitFor("text not found in time: " + text, new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                return getDriver().findElement(By.xpath("//*[contains(.,'" + text + "')]"));
            }

        });
    }

    public static WebElement waitForElement(final By by) {
        return waitFor("element not found in time: " + by, new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                return getDriver().findElement(by);
            }

        });
    }

    public static WebElement waitForElement(final WebElement parent, final By by) {
        return waitFor("element not found in time: " + by, new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                return parent.findElement(by);
            }

        });
    }

    public static List<String> getOptions(WebElement element) {
        List<String> result = new ArrayList<String>();
        // click to open options
        WebElement btn = element.findElement(By.xpath("div[@class='v-filterselect-button']"));
        btn.click();
        // get options elems
        By byOptions = By.xpath("//td[@class='gwt-MenuItem']/span");
        waitForElement(byOptions);
        List<WebElement> optionElems = getDriver().findElements(byOptions);
        // get options strings
        for (WebElement optionElem : optionElems) {
            result.add(optionElem.getText());
        }
        // close options and return
        getDriver().findElement(By.tagName("body")).click(); // click outside options to hide them
        return result;
    }

    public static void waitForElementNotDisplayed(String errorMsg, final WebElement element) {
        waitFor(errorMsg, new ExpectedCondition<Boolean>() {

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

    public static void STEP(String description) {
        SeleniumContext.getTestCaseReporter().STEP(description, getDriver(), log);
    }

    public static void selectCustom(WebElement element, final String optionText) {
        final WebElement btn = element.findElement(By.xpath("//div[@class='v-filterselect-button']"));

//        btn.click();
//        WebElement option = waitForElement(By.xpath("//td[@class='gwt-MenuItem']/span[contains(.,'" + text + "')]"));
        WebElement option = waitFor("KoodistoComponent not found: " + optionText, new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                btn.click();
                return getDriver().findElement(By.xpath("//td[@class='gwt-MenuItem']/span[contains(.,'" + optionText + "')]"));
            }

        });
        option.click();
    }

    public static void select(WebElement element, final String optionText) {
        final WebElement btn = element.findElement(By.xpath("div[@class='v-filterselect-button']"));
//        btn.click();
//        WebElement option = waitForElement(By.xpath("//td[@class='gwt-MenuItem']/span[contains(.,'" + text + "')]"));
        WebElement option = waitFor("option not found: " + optionText, new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                btn.click();
                return getDriver().findElement(By.xpath("//td[@class='gwt-MenuItem']/span[contains(.,'" + optionText + "')]"));
            }

        });
        option.click();
    }

    public static void input(String elementId, String text) {
        input(elementId, text, true, true);
    }

    public static void input(WebElement element, String text) {
        input(element, text, true, true);
    }

    public static void input(String elementId, String text, boolean fastInput, boolean clickOutsideAfter) {
        WebElement element = getWebElementForDebugId(elementId);
        input(element, text, fastInput, clickOutsideAfter);
    }

    public static void input(WebElement element, String text, boolean fastInput, boolean clickOutsideAfter) {
        getDriver().switchTo().window(""); // firefox ei lauo onchange jos ikkuna ei ole aktiivinen - http://code.google.com/p/selenium/issues/detail?id=157
        element.click();
        if (fastInput) {
            setValue(element, text);
        } else {
            element.clear();
            element.sendKeys(text);
        }
        //fireOnChange(element);
        if (clickOutsideAfter) {
            getDriver().findElement(By.xpath("//body")).click(); // click outside the element to launch validation etc javascripts
        }
    }

    public static void setValue(WebElement element, String value) {
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].value = arguments[1]", element, value);
    }

    public static WebElement click(final By by) {
        return waitFor("failed to click element: " + by, new ExpectedCondition<WebElement>() {
            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                try {
                    WebElement element = getDriver().findElement(by);
                    element.click();
                    return element;
                } catch (Exception e) {
                    log.warn("OrganisaatioSeleniumTstCaseSupport.click - WARNING: " + e);
                    return null;
                }
            }
        });
    }

    public static WebElement getWebElementForDebugId(String debugId) {
        return getDriver().findElement(By.id(debugId));
    }

    public static WebElement getWebElementForXpath(String xpath) {
        return getDriver().findElement(By.xpath(xpath));
    }

    public static <T> T waitFor(String errorMsg, ExpectedCondition<T> expectedCondition) {
        try {
            T result = new WebDriverWait(getDriver(), TIME_OUT_IN_SECONDS).until(expectedCondition);
            return result;
        } catch (Throwable e) {
            if (e instanceof StaleElementReferenceException) {
                log.warn("OrganisaatioSeleniumTstCaseSupport.waitFor - WARNING: " + e);
                return null;
            } else {
                fail(errorMsg + " --- (" + e + ")");
                throw new RuntimeException("should have failed already");
            }
        }
    }

}
