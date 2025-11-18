package com.prestador.filter;

import com.prestador.config.ApiConfigurationService;
import jakarta.ejb.EJB;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * API Key Authentication Filter
 *
 * Servlet filter that intercepts all requests to /api/documents/* endpoints
 * and validates the API key provided in the X-API-Key header.
 *
 * This filter implements the security requirement for protecting clinical document
 * endpoints from unauthorized access. Only requests with a valid API key are allowed
 * to proceed to the servlet.
 *
 * Authentication Flow:
 * 1. Extract X-API-Key header from request
 * 2. Validate against configured API key (via ApiConfigurationService)
 * 3. If valid: allow request to proceed (filter chain continues)
 * 4. If invalid/missing: return 401 Unauthorized with JSON error response
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-18
 */
@WebFilter(filterName = "ApiKeyAuthenticationFilter", urlPatterns = {"/api/documents", "/api/documents/*"})
public class ApiKeyAuthenticationFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(ApiKeyAuthenticationFilter.class.getName());

    private static final String API_KEY_HEADER = "X-API-Key";

    @EJB
    private ApiConfigurationService configService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.log(Level.INFO, "API Key Authentication Filter initialized for /api/documents/*");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Extract API key from request header
        String providedApiKey = httpRequest.getHeader(API_KEY_HEADER);

        // Log the authentication attempt (without exposing the actual key)
        String requestUri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        String remoteAddr = httpRequest.getRemoteAddr();

        LOGGER.log(Level.INFO, "API authentication attempt - Method: {0}, URI: {1}, Remote IP: {2}, Has API Key: {3}",
                new Object[]{method, requestUri, remoteAddr, (providedApiKey != null)});

        // Validate API key
        if (configService.validateApiKey(providedApiKey)) {
            // Authentication successful - proceed with request
            LOGGER.log(Level.INFO, "API authentication successful - URI: {0}, Remote IP: {1}",
                    new Object[]{requestUri, remoteAddr});
            chain.doFilter(request, response);
        } else {
            // Authentication failed - return 401 Unauthorized
            LOGGER.log(Level.WARNING, "API authentication failed - Method: {0}, URI: {1}, Remote IP: {2}",
                    new Object[]{method, requestUri, remoteAddr});

            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");

            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "UNAUTHORIZED");
            errorResponse.put("message", "Invalid or missing API key. Please provide a valid X-API-Key header.");
            errorResponse.put("timestamp", java.time.Instant.now().toString());

            httpResponse.getWriter().write(errorResponse.toString());
        }
    }

    @Override
    public void destroy() {
        LOGGER.log(Level.INFO, "API Key Authentication Filter destroyed");
    }
}
