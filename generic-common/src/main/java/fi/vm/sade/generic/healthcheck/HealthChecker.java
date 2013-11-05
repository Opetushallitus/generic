package fi.vm.sade.generic.healthcheck;

/**
 * @author Antti Salonen
 */
public interface HealthChecker {
    Object checkHealth() throws Throwable;
}
