package fi.vm.sade.generic.service;

import org.hibernate.validator.HibernateValidator;

import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.ValidatorFactory;
import javax.validation.bootstrap.ProviderSpecificBootstrap;
import javax.validation.spi.ValidationProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: en saanut ValidatorFactoryä toimimaan osgissa, joten koitetaan tämmöistä
 *
 * OVT-407
 *
 * @author Antti
 */
public final class ValidatorFactoryBean {

    private ValidatorFactoryBean() {
    }

    /**
     * Custom provider resolver is needed since the default provider resolver
     * relies on current thread context loader and doesn't find the default
     * META-INF/services/.... configuration file
     */
    private static class HibernateValidationProviderResolver implements ValidationProviderResolver {

        @Override
        public List getValidationProviders() {
            List providers = new ArrayList(1);
            providers.add(new HibernateValidator());
            return providers;
        }
    }

    private static final ValidatorFactory INSTANCE;

    static {
        ProviderSpecificBootstrap validationBootStrap = Validation.byProvider(ValidationProvider.class);
        validationBootStrap.providerResolver(new HibernateValidationProviderResolver());
        Configuration configuration = validationBootStrap.configure();
        INSTANCE = configuration.buildValidatorFactory();
    }

    public static ValidatorFactory getInstance() {
        return INSTANCE;
    }
}