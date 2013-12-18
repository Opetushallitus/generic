package fi.vm.sade.generic.rest;

/**
 * @author Antti Salonen
 */
public class TestParams {

    public static TestParams instance;

    //

    public int authRedirects = 0;
    public int authTgtCount = 0;
    public int authTicketCount = 0;
    public int isRequestAuthenticatedCount = 0;
    public int authTicketValidatedSuccessfullyCount = 0;
    public boolean failNextBackendAuthentication = false;
    public String userIsAlreadyAuthenticatedToCas = null;
}