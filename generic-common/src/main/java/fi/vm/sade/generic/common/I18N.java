package fi.vm.sade.generic.common;

import java.util.Locale;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.MessageSourceAccessor;

/**
 * @author tommiha
 * 
 */
public class I18N implements ApplicationContextAware {

    private static MessageSourceAccessor messageSourceAccessor;
    private static final ThreadLocal<Locale> LOCALE_THREAD_LOCAL = new ThreadLocal<Locale>();

    public static void setMessageSourceAccessor(MessageSourceAccessor msa) {
        messageSourceAccessor = msa;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        setMessageSourceAccessor(new MessageSourceAccessor(applicationContext));
    }

    /**
     * Retrieve the message for the given code and the default Locale.
     * 
     * @param code
     *            code of the message
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException
     *             if not found
     */
    public static String getMessage(String code) {
        Locale locale = LOCALE_THREAD_LOCAL.get();
        return messageSourceAccessor.getMessage(code, locale);
    }

    /**
     * Retrieve the message for the given code and the default Locale.
     * 
     * @param code
     *            code of the message
     * @param args
     *            arguments for the message, or <code>null</code> if none
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException
     *             if not found
     */
    public static String getMessage(String code, Object... args) {
        Locale locale = LOCALE_THREAD_LOCAL.get();
        return messageSourceAccessor.getMessage(code, args, locale);
    }

    public static void setLocale(Locale locale) {
        LOCALE_THREAD_LOCAL.set(locale);
    }

    public static Locale getLocale() {
        return LOCALE_THREAD_LOCAL.get();
    }
}
