package filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.HttpUtil;
import utils.JwtUtil;

import java.io.IOException;

@WebFilter(urlPatterns = {"/users/*", "/cart/*", "/orders/*", "/payments/checkout"})
public class JwtAuthenticationFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Get access token from Authorization header
        String authHeader = req.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Get token string after "Bearer"
        }

        // Check the validity of the token
        if (token != null && JwtUtil.validateAccessToken(token)) {
            // Token is valid, proceed with the request
            try {
                Integer userId = JwtUtil.getUserIdFromToken(token);
                req.setAttribute("userId", userId);

                chain.doFilter(request, response);
            } catch (Exception e) {
                HttpUtil.sendJson(res, HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token Payload!");
            }
        } else {
            // Invalid or missing token, return 401 Unauthorized with error message
            HttpUtil.sendJson(res, HttpServletResponse.SC_UNAUTHORIZED, "Access Token Expired or Invalid!");
        }
    }
}
