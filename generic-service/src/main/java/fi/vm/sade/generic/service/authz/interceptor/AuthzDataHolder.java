package fi.vm.sade.generic.service.authz.interceptor;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Eetu Blomqvist
 */
@XmlRootElement(name = "AuthzData")
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthzDataHolder {

    @XmlElement
    @XmlJavaTypeAdapter(AuthzDataXmlAdapter.class)
    public HashMap<String, Set<String>> organisations;
}
