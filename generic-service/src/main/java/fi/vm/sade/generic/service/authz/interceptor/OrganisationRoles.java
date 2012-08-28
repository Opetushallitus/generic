package fi.vm.sade.generic.service.authz.interceptor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Eetu Blomqvist
 */
public class OrganisationRoles {


    @XmlAttribute
    public String oid;

    @XmlElement(name="role")
    public String[] roles;

}
