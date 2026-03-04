package filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.HttpUtil;
import utils.JwtUtil;

import java.io.IOException;
import java.util.Locale;

/**
 * Role-based authorization filter using JWT role claim.
 */
public class RoleAuthorizationFilter implements Filter {
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String method = req.getMethod();
        String path = normalizePath(req);
        boolean adminEndpoint = isAdminOnlyEndpoint(path, method);
        boolean userEndpoint = isUserFeatureEndpoint(path);

        if ("OPTIONS".equalsIgnoreCase(method) || isPublicEndpoint(path, method)) {
            chain.doFilter(request, response);
            return;
        }

        // Endpoint not in RBAC policy: let servlet/router decide (typically 404/405).
        if (!adminEndpoint && !userEndpoint) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Integer userId = JwtUtil.getUserIdFromAuthorizationHeader(req.getHeader("Authorization"));
            String role = JwtUtil.getRoleFromAuthorizationHeader(req.getHeader("Authorization"))
                    .toUpperCase(Locale.ROOT);

            req.setAttribute("userId", userId);
            req.setAttribute("userRole", role);

            if (adminEndpoint) {
                if (!ROLE_ADMIN.equals(role)) {
                    HttpUtil.sendJson(res, HttpServletResponse.SC_FORBIDDEN, "Forbidden: admin role required");
                    return;
                }
                chain.doFilter(request, response);
                return;
            }

            if (userEndpoint) {
                if (!ROLE_USER.equals(role) && !ROLE_ADMIN.equals(role)) {
                    HttpUtil.sendJson(res, HttpServletResponse.SC_FORBIDDEN, "Forbidden: user role required");
                    return;
                }
                chain.doFilter(request, response);
                return;
            }
        } catch (RuntimeException ex) {
            HttpUtil.sendJson(res, HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
        }
    }

    private boolean isPublicEndpoint(String path, String method) {
        if (path.equals("/") || path.equals("/auth/login") || path.equals("/auth/signup") || path.equals("/auth/refresh")) {
            return true;
        }
        return "GET".equalsIgnoreCase(method) && path.startsWith("/products");
    }

    private boolean isUserFeatureEndpoint(String path) {
        return path.startsWith("/cart") || path.startsWith("/orders") || path.startsWith("/payments/checkout") || path.startsWith(
                "/auth/logout");
    }

    private boolean isAdminOnlyEndpoint(String path, String method) {
        boolean isWriteMethod = "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(
                method);

        if (path.startsWith("/users")) return true;
        if (path.startsWith("/suppliers")) return true;
        if (path.startsWith("/purchase-orders")) return true;
        if (path.startsWith("/supplier-analytics") || path.startsWith("/supplier-quotations")) return true;
        if (isWriteMethod && path.startsWith("/products")) return true;
        return isWriteMethod && path.startsWith("/categories");
    }

    private String normalizePath(HttpServletRequest req) {
        String servletPath = req.getServletPath() == null ? "" : req.getServletPath();
        String pathInfo = req.getPathInfo() == null ? "" : req.getPathInfo();
        String fullPath = servletPath + pathInfo;
        if (fullPath.isBlank()) return "/";
        if (fullPath.length() > 1 && fullPath.endsWith("/")) {
            return fullPath.substring(0, fullPath.length() - 1);
        }
        return fullPath;
    }
}
