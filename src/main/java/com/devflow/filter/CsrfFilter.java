package com.devflow.filter;

import com.devflow.util.CsrfUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Set;

/**
 * Cross-Site Request Forgery protection. Every POST must send the session's secret token
 * (hidden form field "_csrf" or the "X-CSRF-Token" header from JavaScript). A malicious site
 * cannot read the token, so it cannot forge a valid request on the user's behalf.
 */
public class CsrfFilter implements Filter {
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (req.getServletPath().startsWith("/assets/")) { chain.doFilter(req, resp); return; }

        String token = CsrfUtil.tokenFor(req.getSession(true));
        req.setAttribute("csrfToken", token);

        if (!SAFE_METHODS.contains(req.getMethod())) {
            String sent = req.getHeader("X-CSRF-Token");
            if (sent == null) sent = req.getParameter("_csrf");
            if (!CsrfUtil.matches(token, sent)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Your session expired or the request was not valid. Please go back and try again.");
                return;
            }
        }
        chain.doFilter(req, resp);
    }
}
