package fi.vm.sade.support.selenium;

import fi.vm.sade.generic.common.I18N;
import org.junit.runner.Description;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.File;
import java.util.Locale;

/**
 * Utils for all tests
 *
 * @author Antti Salonen
 */
public final class TestUtils {

    private TestUtils() {
    }

    public static String getTestName(Description description) {
        return description.getClassName() + "." + description.getMethodName();
    }

    public static boolean getEnvOrSystemPropertyAsBoolean(Boolean originalValue, String envVariableName, String systemPropertyName) {

        String value = getEnvOrSystemProperty(null, envVariableName, systemPropertyName);
        return (value == null ? originalValue : Boolean.parseBoolean(value));

    }

    public static String getEnvOrSystemProperty(String originalValue, String envVariableName, String systemPropertyName) {
        if (System.getenv(envVariableName) != null) {
            originalValue = System.getenv(envVariableName);
        }
        if (System.getProperty(systemPropertyName) != null) {
            originalValue = System.getProperty(systemPropertyName);
        }
        return originalValue;
    }

    public static File getReportDir() {
        return new File("target/failsafe-reports/selenium-reports");
    }

    public static boolean isDemoMode() {
        return TestUtils.getEnvOrSystemPropertyAsBoolean(false, "DEMO_MODE", "demoMode");
    }

    public static void initI18N() {
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
    }

    private static String msg(String key, String message) {
        return message != null ? message : key;
    }

}
