package fi.vm.sade.koodisto.selenium;

import org.junit.runner.Description;

import java.io.File;

/**
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
}
