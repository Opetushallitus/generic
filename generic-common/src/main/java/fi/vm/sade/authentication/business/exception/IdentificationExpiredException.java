package fi.vm.sade.authentication.business.exception;

import fi.vm.sade.generic.service.exception.SadeBusinessException;

/**
 * Thrown when identification for for an idp has expired. For example in Haka
 * identification must be considered expired, if 24 months have passed since
 * last authentication.
 *
 * @author Juuso Makinen <juuso.makinen@gofore.com>
 */
public class IdentificationExpiredException extends SadeBusinessException {

    private static final String ERROR_KEY = IdentificationExpiredException.class.getCanonicalName();

    public IdentificationExpiredException() {
    }

    public IdentificationExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentificationExpiredException(String message) {
        super(message);
    }

    public IdentificationExpiredException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getErrorKey() {
        return ERROR_KEY;
    }
}
