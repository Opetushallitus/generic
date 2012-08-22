package fi.vm.sade.generic.ui.portlet.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 
 * @author kkammone
 * 
 */
public class UserMock implements User {

    @Override
    public boolean isUserInRole(String role) {
        return true;
    }

    @Override
    public String getOid() {
        return "SAMPLEOID";
    }

    @Override
    public List<AccessRight> getRawAccessRights() {
        return new ArrayList<AccessRight>();
    }

    @Override
    public Locale getLang() {
        return new Locale("fi");
    }

}
