package br.com.test.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class SupportFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(SupportFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String supportUser = request.getHeader("supportuser");
        if (supportUser != null) {
            log.info(String.format("Support User: %s request to %s, %s ", request.getHeader("supportuser"), request.getMethod(), request.getRequestURL().toString()));
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() { }

}