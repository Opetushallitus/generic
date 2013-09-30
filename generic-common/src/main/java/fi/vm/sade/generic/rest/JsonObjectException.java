package fi.vm.sade.generic.rest;

/**
 * User: tommiha
 * Date: 9/30/13
 * Time: 2:43 PM
 */
public class JsonObjectException extends RuntimeException {
    public JsonObjectException() {
    }

    public JsonObjectException(String message) {
        super(message);
    }

    public JsonObjectException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonObjectException(Throwable cause) {
        super(cause);
    }
}
