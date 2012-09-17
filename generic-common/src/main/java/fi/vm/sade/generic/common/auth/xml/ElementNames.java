package fi.vm.sade.generic.common.auth.xml;

import javax.xml.namespace.QName;

/**
 * @author Eetu Blomqvist
 */
public class ElementNames {
    public static final String WSS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static final String SECURITY_HEADER = "Security";
    public static final QName SECURITY_HEADER_QN = new QName(WSS, SECURITY_HEADER);
    public static final String AUTHZ_DATA = "AuthzData";
    public static final String ROLE = "role";
    public static final String ORGANISAATIO = "organisaatio";
    public static final String SECURITY_TICKET = "SecurityTicket";
    public static final String USERNAME_TOKEN = "UsernameToken";
    public static final String USERNAME = "Username";
    public static final String SADE_URI = "http://service.sade.vm.fi";
    public static final String NOT_AUTHORIZED = "NotAuthorized";
    public static final QName FAULT_Q_NAME = new QName(SADE_URI, NOT_AUTHORIZED);
    public static final String AUTHENTICATION_FAILED = "AuthenticationFailed";

    public static final QName SECURITY_TICKET_QNAME = new QName(SADE_URI, SECURITY_TICKET);
    public static final String OID = "oid";
    public static final String ORG_CHILD = "child";
}
