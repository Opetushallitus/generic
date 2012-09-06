package fi.vm.sade.generic.common.auth.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Eetu Blomqvist
 */
public class Organisation {

    @XmlAttribute(name = ElementNames.OID)
    public String oid;

    @XmlElement(name = ElementNames.ORG_CHILD)
    public Set<String> children = new HashSet<String>();

    @XmlElement(name = ElementNames.ROLE)
    public Set<String> roles = new HashSet<String>();
}
