package fi.vm.sade.security;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * @author Antti Salonen
 */
@Deprecated // tukee vanhaa autentikaatioa spring securityn kanssa
public class UrlRewriteFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            System.out.println("\nUrlRewriteFilter.doFilter: "+request.getRequestURL().append("?").append(request.getQueryString()));
            if ("oldDeprecatedSecurity_REMOVE".equals(request.getHeader("CasSecurityTicket")) && !"true".equals(request.getParameter("oldDeprecatedSecurity_REMOVE"))) {
                String path = request.getRequestURI();
                if (path.startsWith(request.getContextPath())) {
                    path = path.substring(request.getContextPath().length());
                }
                path += "?oldDeprecatedSecurity_REMOVE=true";
                System.out.println("    UrlRewriteFilter.doFilter forward to: "+path);
                servletRequest.getRequestDispatcher(path).forward(servletRequest, servletResponse);
                return;
            }
        }

        // if no rewrite has taken place continue as normal
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }

}
