package fi.vm.sade.support.selenium;

import com.vaadin.ui.Component;
import fi.vm.sade.generic.common.I18N;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
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

    public static WebElement waitForElement(final Component component) {
        return waitFor("element not found in time: " + component.getDebugId(), new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                return getDriver().findElement(By.id(component.getDebugId()));
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
        // first click outside (if option is selected+highlighted, this breaks up)
        getDriver().findElement(By.tagName("body")).click();
        // click to open options
        final WebElement btn = element.findElement(By.xpath("div[@class='v-filterselect-button']"));
        final By byOptions = By.xpath("//td[@class='gwt-MenuItem']");

        // get options elems
        //waitForElement(byOptions); // NOTE: tämä ei toimi joissain tapauksissa bamboolla? jos xpath: //td[@class='gwt-MenuItem']/span ? trying sthing else..
        waitFor("options not found in time: " + byOptions, new ExpectedCondition<WebElement>() {
            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                btn.click();
                return getDriver().findElement(byOptions);
            }
        });

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
        TestCaseReporter testCaseReporter = SeleniumContext.getTestCaseReporter();
        testCaseReporter.STEP(description, getDriver(), log, testCaseReporter.isTakeScreenshots());
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

    public static void select(Component comp, final String optionText) {
        select(comp.getDebugId(), optionText);
    }

    public static void select(String selectboxId, final String optionText) {
        final WebElement btn = waitForElement(By.xpath("//*[@id='"+selectboxId+"']//*[@class='v-filterselect-button']"));
        WebElement option = waitFor("option not found with option: " + optionText, new ExpectedCondition<WebElement>() {
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

    public static void select(Component selectboxComponent, final int optionIndex) {
        select(selectboxComponent.getDebugId(), optionIndex);
    }

    public static void select(String selectboxId, final int optionIndex) {
        final WebElement btn = waitForElement(By.xpath("//*[@id='"+selectboxId+"']//*[@class='v-filterselect-button']"));
        btn.click();
        WebElement option = waitFor("option not found with index: " + optionIndex, new ExpectedCondition<WebElement>() {
            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                return getDriver().findElements(By.xpath("//td[@class='gwt-MenuItem']")).get(optionIndex);
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

    /**
     * <p>Tries to find and click element of element name <code>matchElementName</code> 
     * from <code>parent</code>. If parent's element name matches <code>matchElementName</code> 
     * parent is returned, else first matching child is returned - if any.</p> 
     * 
     * <p>This method was added since Vaadin components often match into a surrounding div that
     * is not really the actual target of the click but some child element underneath</p>
     * 
     * <p>E.g. to click a Vaadin CheckBox element:
     * <pre>
     * click(By.id(theCheckBox.getDebugId()), "input");
     * </pre>
     * </p>
     * @param parent
     * @param matchElementName
     * @return
     */
    public static WebElement click(final By parent, final String matchElementName) {
        
        log.debug("trying click element of type: " + matchElementName + " from parent by: " + parent);

        return waitFor("failed to find element: " + parent, new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                try {

                    WebElement element = getDriver().findElement(parent);
                    final String parentType = element.getTagName();
                    if (parentType.equals(matchElementName)) {
                        log.debug("element type matches parent directly: " + matchElementName + ", parent: " + parent);
                        return element;
                    } else {
                        List<WebElement> children = element.findElements(By.tagName(matchElementName));                        
                        for (WebElement child : children) {
                            if (child.getTagName().equals(matchElementName)) {
                                log.debug("clicking first child of type: " + matchElementName + " of parent: " + parent);
                                child.click();
                                return child;
                            }
                        }
                        log.warn("no such child element type: " + matchElementName + " for parent " + parent);
                        return null;
                    }

                } catch (Exception e) {
                    log.warn("unexpected error while trying to find child element to click of type: " + matchElementName, e);
                    return null;
                }
            }

        });

    }

    public static WebElement click(final By by) {
        return waitFor("failed to click element: " + by, new ExpectedCondition<WebElement>() {

            @Override
            public WebElement apply(@Nullable WebDriver webDriver) {
                try {
                    WebElement element = getDriver().findElement(by);
                    log.info("clicking element: " + element
                        + "\n\ttagName: " + element.getTagName()
                        + "\n\tclass: " + element.getClass().getName()
                        + "\n\tcssClass: " + element.getCssValue("class"));

                    List<WebElement> inputs = element.findElements(By.tagName("input"));
                    log.info("number of inputs under this element: " + inputs.size());
                    for (WebElement i : inputs) {
                        log.info("input: " + i);
                    }


                    element.click();
                    return element;
                } catch (Exception e) {
                    log.warn("OrganisaatioSeleniumTstCaseSupport.click - WARNING: " + e);
                    return null;
                }
            }

        });
    }
    
    public static List<WebElement> getWebElementsByContainingId(String debugId) {
        List<WebElement> elements = new ArrayList<WebElement>();
        try {
            
            WebElement element  = getDriver().findElement(By.id(debugId));
            elements.add(element);
            return elements;
        } catch (NoSuchElementException e) {
            try {
                log.info("getWebElementFor didn't find element by id, trying with partial id: " + debugId);
                
                return getDriver().findElements(By.xpath("//*[contains(@id,'" + debugId + "')]"));
            } catch (NoSuchElementException e2) {
               
                throw new NoSuchElementException("no element for id: " + debugId);
            }
        }
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

    /**
     * Assert if webelement contains validation errors (css class "v-errorindicator")
     *
     * @param expectedErrorCount
     * @param elem
     */
    public static void assertValidationErrors(final int expectedErrorCount, WebElement elem) throws Throwable {
        final List<WebElement> errorElems = elem.findElements(By.xpath("//*[contains(@class,'v-errorindicator')]"));
        waitAssert(new AssertionCallback() {
            @Override
            public void doAssertion() {
                assertEquals("assertValidationErrors failed", expectedErrorCount, errorElems.size());
            }
        });
    }

    /**
     * Wait until callback does _not_ throw exception (or timeout), useful for making waited assertions.
     */
    public static void waitAssert(final AssertionCallback assertionCallback) throws Throwable {
        final Throwable[] exception = new Throwable[1];
        try {
            Object result = new WebDriverWait(SeleniumContext.getDriver(), SeleniumUtils.TIME_OUT_IN_SECONDS).until(new ExpectedCondition<Object>() {
                @Override
                public Object apply(@Nullable WebDriver webDriver) {
                    try {
                        assertionCallback.doAssertion();
                        return "OK";
                    } catch (Throwable e) {
                        exception[0] = e;
                        log.warn("waitAssert not yet succeeded: " + e);
                        return null;
                    }
                }
            });
        } catch (org.openqa.selenium.TimeoutException te) {
            if (exception[0] != null) {
                throw exception[0];
            } else {
                fail("waitAssert failed but exception is null?!");
            }
        }
    }

    public static List<String> getCheckboxOptions(String parentId) {
        List<WebElement> elems = getDriver().findElements(By.xpath("//*[@id='" + parentId + "']//*[contains(@class,'v-checkbox')]"));
        List<String> result = new ArrayList<String>();
        for (WebElement elem : elems) {
            WebElement cb = elem.findElement(By.tagName("input"));
            if ("checked".equals(cb.getAttribute("checked")) || "true".equals(cb.getAttribute("checked"))) {
                result.add(elem.getText());
            }
        }
        return result;
    }

    /**
     * Click checkbox option and return the checkbox
     *
     * @param parentId
     * @param optionText
     * @return
     */
    public static WebElement clickCheckbox(String parentId, String optionText) {
        List<WebElement> elems = getDriver().findElements(By.xpath("//*[@id='" + parentId + "']//*[contains(@class,'v-checkbox')]"));
        for (WebElement elem : elems) {
            if (optionText.equals(elem.getText())) {
                WebElement cb = elem.findElement(By.tagName("input"));
                cb.click();
                return cb;
            }
        }
        return null;
    }



    public static abstract class AssertionCallback {
        public abstract void doAssertion() throws Throwable;
    }

}

