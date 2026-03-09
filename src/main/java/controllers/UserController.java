package controllers;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import dto.user.CreateUserRequestDto;
import dto.user.UpdateUserRequestDto;
import dto.user.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.UserService;
import utils.HttpUtil;

/**
 * UserController
 * <p>
 * Endpoints: - GET /users : list users - GET /users/{id} : get user by id -
 * POST /users : create user - PUT /users/{id} : update user - DELETE
 * /users/{id} : delete user
 * <p>
 * Note: - Requires Authorization: Bearer <token> (same pattern as Cart/Auth) -
 * Role-based access is enforced by RoleAuthorizationFilter.
 */
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void handleGetAll(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String q = req.getParameter("q");
            if (q == null || q.isBlank()) {
                q = req.getParameter("query");
            }
            String pageStr = req.getParameter("page");
            String sizeStr = req.getParameter("size");

            Integer page = (pageStr == null || pageStr.isBlank()) ? null : Integer.valueOf(pageStr);
            Integer size = (sizeStr == null || sizeStr.isBlank()) ? null : Integer.valueOf(sizeStr);

            if (page == null && size != null) {
                page = 0;
            }
            if (page != null && size == null) {
                size = 5;
            }
            if (page != null && page < 0) {
                throw new IllegalArgumentException("page must be >= 0");
            }
            if (size != null && size <= 0) {
                throw new IllegalArgumentException("size must be > 0");
            }

            List<UserDto> users = userService.getAll(q, page, size);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, users);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid numeric query parameter");
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    public void handleGetById(HttpServletRequest req, HttpServletResponse resp, UUID id) throws IOException {
        try {
            UserDto user = userService.getById(id);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, user);
        } catch (IOException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    public void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CreateUserRequestDto dto = HttpUtil.jsonToClass(req.getReader(), CreateUserRequestDto.class);
            UserDto created = userService.create(dto);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, created);
        } catch (IOException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    public void handleUpdate(HttpServletRequest req, HttpServletResponse resp, UUID id) throws IOException {
        try {
            UpdateUserRequestDto dto = HttpUtil.jsonToClass(req.getReader(), UpdateUserRequestDto.class);
            UserDto updated = userService.update(id, dto);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, updated);
        } catch (IOException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    public void handleDelete(HttpServletRequest req, HttpServletResponse resp, UUID id) throws IOException {
        try {
            userService.delete(id);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "User deleted successfully");
        } catch (IOException e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
