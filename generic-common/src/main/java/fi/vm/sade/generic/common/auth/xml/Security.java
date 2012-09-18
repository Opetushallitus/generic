package fi.vm.sade.generic.common.auth.xml;

import javax.xml.bind.annotation.*;

/**
 * @author Eetu Blomqvist
 */

@XmlRootElement(name = "Security", namespace = ElementNames.WSSE)
@XmlAccessorType(XmlAccessType.FIELD)
public class Security {

    @XmlAttribute(name = "mustUnderstand", namespace = ElementNames.SOAP)
    public String mustUnderstand;

    @XmlElement(name = "UsernameToken", namespace = ElementNames.WSSE)
    public UsernameToken UsernameToken;

    @XmlElement(name = "Timestamp", namespace = ElementNames.WSU)
    public Timestamp timestamp;
}
