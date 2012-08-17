package fi.vm.sade.generic.ui.portlet.security;

import java.util.List;

public interface User {

    public boolean isUserInRole (String role) ;
    
    public String getOid();
    
    public List<AccessRight> getRawAccessRights();
    
    
}
