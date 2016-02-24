package fi.vm.sade.generic.rest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class CorsFiller<R, Q> {

    private final CorsFilterMode mode;

    private final String allowedDomains;

    private final Logger logger = LoggerFactory.getLogger(getClass());;

    protected static final String DEFAULT_DOMAIN_FOR_ALLOW_ORIGIN = "https://virkailija.opintopolku.fi";

    protected CorsFiller(CorsFilterMode mode, String allowedDomains) {
        this.mode = mode;
        this.allowedDomains = allowedDomains;
    }

    protected abstract void setHeader(String key, String value, R response);

    protected abstract List<String> getHeaders(String key, Q request);

    private String getRemoteDomain(Q request) {
        List<String> headers = getHeaders("origin", request);
        return !headers.isEmpty() ? headers.get(0) : getDomainFromReferer(request);
    }

    private String getDomainFromReferer(Q request) {
        List<String> headers = getHeaders("referer", request);
        try {
            if (!headers.isEmpty()) {
                URL url = new URL(headers.get(0));
                return new StringBuilder(url.getProtocol()).append("://").append(url.getHost()).toString();
            }
        } catch (MalformedURLException e) {
            logger.warn("Could not determine domain from request while forming CORS response", e);
        }
        return DEFAULT_DOMAIN_FOR_ALLOW_ORIGIN;
    }

    protected void setAllowOrigin(R response, Q request) {
        if (CorsFilterMode.DEVELOPMENT.equals(mode)) {
            setHeader("Access-Control-Allow-Origin", getRemoteDomain(request), response);
        } else if(StringUtils.isNotBlank(allowedDomains)){
            String matchingDomain = getMatchingDomain(request);
            if (StringUtils.isNotBlank(matchingDomain)) {
                setHeader("Access-Control-Allow-Origin", matchingDomain, response);
            }
        }
    }

    protected void setHeadersToResponse(Q request, R response) {
        for (String value : getHeaders("access-control-request-headers", request)) {
            setHeader("Access-Control-Allow-Headers", value, response);
        }
        setHeader("Access-Control-Allow-Headers", "Caller-Id, clientSubSystemCode, CSRF, ID", response);
        setHeader("Access-Control-Allow-Credentials", "true", response);
        setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD", response);
        setHeader("Access-Control-Max-Age", "604800", response);
    }

    private String getMatchingDomain(Q request) {
        String domain = getRemoteDomain(request);
        for(String allowedDomain : allowedDomains.split(" ")) {
            if (allowedDomain.equals(domain)) {
                return allowedDomain;
            }
        }
        return null;
    }
}
