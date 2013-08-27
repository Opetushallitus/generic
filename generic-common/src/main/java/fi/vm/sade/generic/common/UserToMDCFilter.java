package fi.vm.sade.generic.common;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class UserToMDCFilter implements javax.servlet.Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        try {

            if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication()!=null) {
                if (SecurityContextHolder.getContext().getAuthentication()
                        .getPrincipal() instanceof UserDetails) {
                    UserDetails ud = (UserDetails) SecurityContextHolder
                            .getContext().getAuthentication().getPrincipal();
                    MDC.put("user", ud.getUsername());
                } else {
                    MDC.put("user",
                            "unknown user from: " + request.getRemoteAddr());
                }
            }
            chain.doFilter(request, response);
        } finally {
            MDC.remove("user");

        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
}
