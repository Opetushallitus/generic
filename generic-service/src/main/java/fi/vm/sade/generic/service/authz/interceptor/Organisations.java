package fi.vm.sade.generic.service.authz.interceptor;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Eetu Blomqvist
 */
public class Organisations {

    @XmlElement(name = "organisaatio")
    public OrganisationRoles[] orgRoles;
}
