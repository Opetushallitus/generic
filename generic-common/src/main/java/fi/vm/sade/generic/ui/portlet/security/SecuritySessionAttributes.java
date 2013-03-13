package fi.vm.sade.generic.ui.portlet.security;

@Deprecated // cas todo poista?
public class SecuritySessionAttributes {
    
    /**
     * Liferay requires this so that session scope is shared from servlet to portlets (and between WARs) 
     */
    public static final String LIFERAY_SESSION_SHARE_PREFIX = "LIFERAY_SHARED_"; 

    public static final String AUTHENTICATION_DATA =  LIFERAY_SESSION_SHARE_PREFIX + "AUTHENTICATION_DATA";

    public static final String TICKET = LIFERAY_SESSION_SHARE_PREFIX +  "TICKET";
    
   
    
    

}
