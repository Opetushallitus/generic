package fi.vm.sade.generic.ui.app;

import javax.portlet.PortletRequest;

public class ThreadLocalPortletRequestSupport {

    private static ThreadLocal<PortletRequest> threadLocalPortletRequest = new ThreadLocal<PortletRequest>();

    /**
     * default visibilty intentional
     * 
     * @return
     */
    static PortletRequest getPortletRequest() {
        return threadLocalPortletRequest.get();
    }

    /**
     * default visibilty intentional
     */
    static void setPortletRequest(PortletRequest portletRequest) {
        threadLocalPortletRequest.set(portletRequest);
    }

}
