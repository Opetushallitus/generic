package fi.vm.sade.generic.service;

import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Validator;

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

    public static Validator getValidator() {
        // hibernate validator cannt be used directly because does not support multilingual messages
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return validator;
    }

}