package com.company.task_manager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Spring Security 6's CsrfTokenRequestAttributeHandler defers actually generating/writing
// the CSRF token (and its cookie) until something reads CsrfToken.getToken() - normally a
// server-rendered view. This app has no such view, so nothing would ever trigger the
// XSRF-TOKEN cookie to be written without this filter forcing that resolution on every request.
public class CsrfCookieFilter extends OncePerRequestFilter {

    // Touches the deferred CsrfToken on every request so the XSRF-TOKEN cookie gets written.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
