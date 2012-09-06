package fi.vm.sade.generic.common.auth.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Eetu Blomqvist
 */
@XmlRootElement(name = ElementNames.AUTHZ_DATA)
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthzDataHolder {

    @XmlElement(name = ElementNames.ORGANISAATIO)
    public Set<Organisation> organisations = new HashSet<Organisation>();

//    @XmlElement
//    @XmlJavaTypeAdapter(AuthzDataXmlAdapter.class)
//    public HashMap<String, Set<String>> organisations;
}
