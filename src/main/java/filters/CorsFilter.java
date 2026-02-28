package filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filter to handle Cross-Origin Resource Sharing (CORS) for the application.
 * This filter allows requests from any origin and supports common HTTP methods.
 */
@WebFilter(filterName = "CorsFilter", urlPatterns = "/*")
public class CorsFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        // Cast to HTTP-specific request and response objects
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = getHttpServletResponse((HttpServletResponse) response);

        // Handle preflight CORS requests (OPTIONS method)
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            // For preflight requests, we only need to set the CORS headers and return a 200 OK status
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // For other requests, set CORS headers and continue with the filter chain
        chain.doFilter(request, response);
    }

    /**
     * Helper method to set CORS headers on the HTTP response.
     *
     * @param response The HttpServletResponse object to which CORS headers will be added.
     * @return The modified HttpServletResponse with CORS headers set.
     */
    private static HttpServletResponse getHttpServletResponse(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        response.setHeader("Access-Control-Max-Age", "3600");
        return response;
    }
}
