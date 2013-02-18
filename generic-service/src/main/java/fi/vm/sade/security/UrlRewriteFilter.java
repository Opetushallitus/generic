package fi.vm.sade.security;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Rewrites urls so that they can be handled with standard spring security cas classes.
 * Eg rewrites custom oph CasSecurityHeader as http parameter.
 *
 * @author Antti Salonen
 */
public class UrlRewriteFilter implements Filter {

    public static final String ALREADY_PROCESSED = UrlRewriteFilter.class.getName()+"_alreadyProcessed";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;

            System.out.println("\nUrlRewriteFilter.doFilter: "+request.getRequestURL().append("?").append(request.getQueryString())); // TODO: soutit yms, parempi string käsittely

            if (!"true".equals(request.getAttribute(ALREADY_PROCESSED))) {
                String casTicketHeader = request.getHeader("CasSecurityTicket");
                if ("oldDeprecatedSecurity_REMOVE".equals(casTicketHeader)) { // todo: tukee vanhaa autentikaatioa spring securityn kanssa
                    forward(request, servletResponse, "oldDeprecatedSecurity_REMOVE", "true");
                    return;
                } else if (casTicketHeader != null) {
                    forward(request, servletResponse, "ticket", casTicketHeader);
                    return;
                }

            }

        }

        // if no rewrite has taken place continue as normal
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void forward(HttpServletRequest request, ServletResponse servletResponse, final String paramName, final String paramValue) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith(request.getContextPath())) {
            path = path.substring(request.getContextPath().length());
        }
        if (path.contains("?")) {
            path += "&";
        } else {
            path += "?";
        }
        path += paramName + "=" + paramValue;
        System.out.println("    UrlRewriteFilter.doFilter forward to: " + path);
        request.setAttribute(ALREADY_PROCESSED, "true");
        request.getRequestDispatcher(path).forward(request, servletResponse);
        return;
    }

    @Override
    public void destroy() {
    }

}
