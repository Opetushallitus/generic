package fi.vm.sade.generic.service.exception;

/**
 * @author Eetu Blomqvist
 */
public class NotAuthorizedException extends SadeBusinessException {

    public NotAuthorizedException() {
        super();
    }

    public NotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAuthorizedException(String message) {
        super(message);
    }

    public NotAuthorizedException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getErrorKey() {
        return NotAuthorizedException.class.getCanonicalName();
    }
}
