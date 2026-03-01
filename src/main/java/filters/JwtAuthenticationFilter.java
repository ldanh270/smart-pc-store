package filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.HttpUtil;
import utils.JwtUtil;

import java.io.IOException;

/**
 * Filter to authenticate requests using JWT access tokens.
 * This filter checks for the presence of a valid JWT token in the Authorization header
 * for protected endpoints and allows the request to proceed if the token is valid.
 * If the token is missing or invalid, it responds with a 401 Unauthorized status and an error message.
 */
@WebFilter(urlPatterns = {"/users/*", "/cart/*", "/orders/*", "/payments/checkout"})
public class JwtAuthenticationFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        // Cast to HttpServletRequest and HttpServletResponse to access HTTP-specific methods
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        try {
            // Extract userId from JWT token in Authorization header
            Integer userId = JwtUtil.getUserIdFromAuthorizationHeader(req.getHeader("Authorization"));

            // If token is valid, set userId as a request attribute for downstream use
            req.setAttribute("userId", userId);

            // Continue with the filter chain (proceed to the requested resource)
            chain.doFilter(request, response);
        } catch (Exception e) {
            System.out.println("ERROR JWTAuthenticationFilter - doFilter: " + e.getMessage());
            HttpUtil.sendJson(res, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }

    }
}
