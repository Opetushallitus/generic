package fi.vm.sade.generic.common.auth.xml;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Eetu Blomqvist
 */
@XmlRootElement(name = "Timestamp", namespace = ElementNames.WSU)
@XmlAccessorType(XmlAccessType.FIELD)
public class Timestamp {

    @XmlAttribute(name = "Id", namespace = ElementNames.WSU)
    public String id;

    @XmlElement(name ="Created", namespace = ElementNames.WSU)
    public XMLGregorianCalendar Created;

    @XmlElement(name = "Expires", namespace = ElementNames.WSU)
    public XMLGregorianCalendar Expires;
}
