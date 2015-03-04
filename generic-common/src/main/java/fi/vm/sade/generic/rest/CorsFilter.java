package fi.vm.sade.generic.rest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public abstract class CorsFilter {
    
    //Default mode is PRODUCTION
    private static final String CORSFILTER_MODE_PARAM = "${common.corsfilter.mode:PRODUCTION}";

    protected CorsFilterMode mode;
    
    //Multiple values can be provided by using space as a separator, this will be used only in PRODUCTION mode
    @Value("${common.corsfilter.allowed-domains:}")
    protected String allowedDomains;

    @Value(CORSFILTER_MODE_PARAM)
    void setMode(String mode) {
        this.mode = StringUtils.isNotBlank(mode) && !mode.equalsIgnoreCase(CORSFILTER_MODE_PARAM) ? CorsFilterMode.valueOf(mode) : CorsFilterMode.PRODUCTION;
    }
    
}
