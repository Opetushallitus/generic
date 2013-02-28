package fi.vm.sade.authentication.service.technical;

/**
 * @author Eetu Blomqvist
 */
@Deprecated // todo: cas todo, pois tai siirto muualle, koko moduuli pois?
public class UserNotFoundException extends Exception {
    public UserNotFoundException() {
        super();
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Throwable cause) {
        super(cause);
    }
}
