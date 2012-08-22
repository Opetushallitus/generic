package fi.vm.sade.generic.ui.portlet.security;

import java.util.List;
import java.util.Locale;

public interface User {

    public boolean isUserInRole(String role);

    public String getOid();

    public List<AccessRight> getRawAccessRights();

    public Locale getLang();

}
