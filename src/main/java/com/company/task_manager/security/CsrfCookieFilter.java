package com.company.task_manager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Spring Security's CSRF token is only written to a cookie once something reads its value
// (normally done by server-rendered form templates). This app has no such templates, so
// without forcing that read on every request, the XSRF-TOKEN cookie would never be created
public class CsrfCookieFilter extends OncePerRequestFilter {

    // Reads the CSRF token to trigger the cookie write; the value itself isn't used here
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
