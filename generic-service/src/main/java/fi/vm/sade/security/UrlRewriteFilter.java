package fi.vm.sade.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Rewrites urls so that they can be handled with standard spring security cas classes.
 * Eg rewrites custom oph CasSecurityHeader as http parameter.
 *
 * @author Antti Salonen
 */
// todo: cas todo rethink
public class UrlRewriteFilter implements Filter {

    public static final String ALREADY_PROCESSED = UrlRewriteFilter.class.getName()+"_alreadyProcessed";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;

            //System.out.println("\nUrlRewriteFilter.doFilter: "+request.getRequestURL().append("?").append(request.getQueryString()));

            if (!"true".equals(request.getAttribute(ALREADY_PROCESSED))) {
                String casTicketHeader = request.getHeader("CasSecurityTicket");
                String oldDeprecatedSecurity_REMOVE_username = request.getHeader("oldDeprecatedSecurity_REMOVE_username");
                //System.out.println("UrlRewriteFilter.doFilter, casTicketHeader: "+casTicketHeader+", oldDeprecatedSecurity_REMOVE_username: "+oldDeprecatedSecurity_REMOVE_username);
                if ("oldDeprecatedSecurity_REMOVE".equals(casTicketHeader)) { // todo: tukee vanhaa autentikaatioa spring securityn kanssa, huom tässä ei haeta rooleja!
                    SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken(oldDeprecatedSecurity_REMOVE_username, oldDeprecatedSecurity_REMOVE_username));
                    forward(request, servletResponse, "oldDeprecatedSecurity_REMOVE", "true"); // huom! forwardin jälkeen ei ajeta enää springin securityputkea
                    return;
                } else if (casTicketHeader != null) {
                    /* tämän jälkeen ei ajeta casfiltteriä?!?!??!
                    forward(request, servletResponse, "ticket", casTicketHeader);
                    return;
                    */
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
        System.out.println("WARNING - UrlRewriteFilter forward to: " + path);
        request.setAttribute(ALREADY_PROCESSED, "true");
        request.getRequestDispatcher(path).forward(request, servletResponse);
    }

    @Override
    public void destroy() {
    }

}
