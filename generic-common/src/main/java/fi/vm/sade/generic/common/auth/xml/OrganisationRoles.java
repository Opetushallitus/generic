package fi.vm.sade.generic.common.auth.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Eetu Blomqvist
 */
public class OrganisationRoles {


    @XmlAttribute
    public String oid;

    @XmlElement(name= ElementNames.ROLE)
    public String[] roles;

}
