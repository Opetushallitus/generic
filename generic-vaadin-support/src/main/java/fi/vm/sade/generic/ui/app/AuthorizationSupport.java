package fi.vm.sade.generic.ui.app;

import java.util.List;

import fi.vm.sade.generic.ui.portlet.security.AccessRight;

/**
 * 
 * @author kkammone
 * 
 */
public interface AuthorizationSupport {

    boolean isUserInRole(String role);

    String getUserOID();

    List<AccessRight> getRawAccessRights();

}
