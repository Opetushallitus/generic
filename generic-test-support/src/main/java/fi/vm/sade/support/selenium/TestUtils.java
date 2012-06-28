package fi.vm.sade.support.selenium;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Form;
import com.vaadin.ui.Window;
import fi.vm.sade.generic.common.I18N;
import org.junit.runner.Description;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Utils for all tests
 *
 * @author Antti Salonen
 */
public final class TestUtils {

    private static long nextDebugId = 0;

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

    @SuppressWarnings("unchecked")
    private static <T extends Component> void getComponentsByTypeRecursive(Component comp, List<T> components, Class<T> clazz) {

        if (clazz.isAssignableFrom(comp.getClass())) {
            components.add((T) comp);
        }

        if (comp instanceof ComponentContainer) {

            ComponentContainer cc = (ComponentContainer) comp;
            Iterator<Component> iterator = cc.getComponentIterator();

            while (iterator.hasNext()) {
                getComponentsByTypeRecursive(iterator.next(), components, clazz);
            }
        }
    }

    public static <T extends Component> List<T> getComponentsByType(Component comp, Class<T> clazz) {
        List<T> components = new ArrayList<T>();
        getComponentsByTypeRecursive(comp, components, clazz);
        return components;
    }
    
    /**
     * Generates an id for a component using class name and component hash code.
     * 
     * @param component
     * @return
     */
    public static String resolveComponentIdFromHashCode(Component component) {
        return component.getClass().getName() + "@" + Integer.toHexString(component.hashCode());
    }
    
    public static void generateIds(Component component) {
        generateIds(component, false);
    }

    public static void generateIds(Component component, boolean useHashCode) {
        if (component == null) {
            return;
        }

        // generate debugid if not present
        if (component.getDebugId() == null) {
            String id = null;
            if (useHashCode) {
                id = resolveComponentIdFromHashCode(component);
            }
            else {
                id = "generatedId_" + (++nextDebugId);
            }
            component.setDebugId(id);
        }

        // recursion
        if (component instanceof ComponentContainer) {
            ComponentContainer container = (ComponentContainer) component;
            Iterator<Component> iterator = container.getComponentIterator();
            while (iterator.hasNext()) {
                generateIds(iterator.next());
            }
        }
        if (component instanceof Window) {
            Window window = (Window) component;
            for (Window child : window.getChildWindows()) {
                generateIds(child);
            }
        }
        if (component instanceof Form) {
            Form form = (Form) component;
            generateIds(form.getLayout());
            generateIds(form.getFooter());
        }
    }
}
