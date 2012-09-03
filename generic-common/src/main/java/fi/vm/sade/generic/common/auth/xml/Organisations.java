package fi.vm.sade.generic.common.auth.xml;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Eetu Blomqvist
 */
public class Organisations {

    @XmlElement(name = ElementNames.ORGANISAATIO)
    public OrganisationRoles[] orgRoles;
}
