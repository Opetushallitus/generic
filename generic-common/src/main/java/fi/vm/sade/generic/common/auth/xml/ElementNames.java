package fi.vm.sade.generic.common.auth.xml;

import javax.xml.namespace.QName;

/**
 * @author Eetu Blomqvist
 */
public class ElementNames {

    public static final String AUTHZ_DATA = "AuthzData";
    public static final String ROLE = "role";
    public static final String ORGANISAATIO = "organisaatio";
    public static final String SECURITY_TICKET = "SecurityTicket";
    public static final String SADE_URI = "http://service.sade.vm.fi";
    public static final String NOT_AUTHORIZED = "NotAuthorized";
    public static final String AUTHENTICATION_FAILED = "AuthenticationFailed";

    public static final QName SECURITY_TICKET_QNAME = new QName(SADE_URI, SECURITY_TICKET);
    public static final String OID = "oid";
    public static final String ORG_CHILD = "child";
}
