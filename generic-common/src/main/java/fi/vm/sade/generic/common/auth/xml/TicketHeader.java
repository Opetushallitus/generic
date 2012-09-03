package fi.vm.sade.generic.common.auth.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Eetu Blomqvist
 */
@XmlRootElement(name = ElementNames.SECURITY_TICKET)
@XmlAccessorType(XmlAccessType.FIELD)
public class TicketHeader {

    @XmlAttribute(required = true)
    public String username;

    @XmlAttribute(required = true)
    public String ticket;
}