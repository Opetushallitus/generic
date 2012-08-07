package fi.vm.sade.generic.ui.portlet.security;

import java.io.Serializable;

public class AccessRight implements Serializable {

    private static final long serialVersionUID = 1L;
    private String organizatioOid;
    private String role;
    private String application;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOrganizatioOid() {
        return organizatioOid;
    }

    public void setOrganizatioOid(String organizatioOid) {
        this.organizatioOid = organizatioOid;
    }

    public AccessRight(String organizatioOid, String role, String application) {
        super();
        this.organizatioOid = organizatioOid;
        this.role = role;
        this.application = application;
    }

}
