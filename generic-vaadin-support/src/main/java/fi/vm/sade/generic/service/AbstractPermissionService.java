package fi.vm.sade.generic.service;

import fi.vm.sade.generic.ui.portlet.security.User;

/**
 * Abstract base class for UI permission checks
 * 
 * @author wuoti
 * 
 */
public abstract class AbstractPermissionService implements PermissionService {

    @Override
    public boolean userCanRead() {
        return getUser().isUserInRole(getReadRole()) || userCanReadAndUpdate() || userCanCreateReadUpdateAndDelete();
    }

    @Override
    public boolean userCanReadAndUpdate() {
        return getUser().isUserInRole(getReadUpdateRole()) || userCanCreateReadUpdateAndDelete();
    }

    @Override
    public boolean userCanCreateReadUpdateAndDelete() {
        return getUser().isUserInRole(getCreateReadUpdateDeleteRole());
    }

    protected abstract String getReadRole();

    protected abstract String getReadUpdateRole();

    protected abstract String getCreateReadUpdateDeleteRole();

    protected abstract User getUser();
}
