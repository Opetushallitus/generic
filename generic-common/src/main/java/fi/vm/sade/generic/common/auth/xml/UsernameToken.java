package fi.vm.sade.generic.common.auth.xml;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Eetu Blomqvist
 */
@XmlRootElement(name = "UsernameToken", namespace = ElementNames.WSSE)
@XmlAccessorType(XmlAccessType.FIELD)
public class UsernameToken {

    @XmlAttribute(name = "Id", namespace = ElementNames.WSU)
    public String Id;

    @XmlElement(name = "Username", namespace = ElementNames.WSSE)
    public String Username;

    @XmlElement(name = "Password", namespace = ElementNames.WSSE)
    public String Password;

    @XmlElement(name = "Nonce", namespace = ElementNames.WSSE)
    public String Nonce;

    @XmlElement(name = "Created", namespace = ElementNames.WSU)
    public XMLGregorianCalendar Created;
}
