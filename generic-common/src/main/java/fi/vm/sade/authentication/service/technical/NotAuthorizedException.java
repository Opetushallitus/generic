package fi.vm.sade.authentication.service.technical;

/**
 * @author Eetu Blomqvist
 */
@Deprecated // todo: cas todo, pois tai siirto muualle, koko moduuli pois? + tämän nimisiä poikkareita nyt kaksi
public class NotAuthorizedException extends Exception {
    public NotAuthorizedException() {
        super();
    }

    public NotAuthorizedException(String message) {
        super(message);
    }

    public NotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAuthorizedException(Throwable cause) {
        super(cause);
    }
}
