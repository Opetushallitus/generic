package fi.vm.sade.generic.service;

/**
 * 
 * @author wuoti
 * 
 */
public interface PermissionService {
    public boolean userCanRead();

    public boolean userCanReadAndUpdate();

    public boolean userCanCreateReadUpdateAndDelete();
}
