package controllers;

import dto.cart.AddToCartRequestDto;
import dto.cart.CartItemResponseDto;
import dto.cart.UpdateCartItemRequestDto;
import services.CartService;
import utils.HttpUtil;
import utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
/**
 * CartController
 *
 * Responsibilities:
 * - Parse request (JWT auth header + JSON body)
 * - Delegate business logic to CartService
 * - Return JSON responses with appropriate HTTP status codes
 *
 * Notes:
 * - Currently all business errors are returned as 400 BAD_REQUEST.
 *   (If you later want more RESTful behavior, stock conflict can be mapped to 409 CONFLICT.)
 */

public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    /**
     * GET /cart
     * - Extract userId from Authorization header (JWT)
     * - Return all cart items of current user
     */

    public void handleGetCart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer userId = JwtUtil.getUserIdFromAuthorizationHeader(req.getHeader("Authorization"));
            List<CartItemResponseDto> items = cartService.getMyCart(userId);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, items);
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
    /**
     * POST /cart/add
     * Body: { "productId": number, "quantity": number }
     * - Add a product to user's cart (create cart if not exists)
     */

    public void handleAddToCart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer userId = JwtUtil.getUserIdFromAuthorizationHeader(req.getHeader("Authorization"));
            AddToCartRequestDto dto = HttpUtil.jsonToClass(req.getReader(), AddToCartRequestDto.class);

            cartService.addToCart(userId, dto.getProductId(), dto.getQuantity());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_CREATED, "Product added to cart successfully");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
/**
     * PUT /cart/items/{cartItemId}
     * Body: { "quantity": number }
     * - Update quantity of a specific cart item (owned by current user)
     * - If quantity <= 0, the item will be removed
     */
    public void handleUpdateQuantity(HttpServletRequest req, HttpServletResponse resp, Integer cartItemId)
     throws IOException {
        try {
            Integer userId = JwtUtil.getUserIdFromAuthorizationHeader(req.getHeader("Authorization"));
            UpdateCartItemRequestDto dto = HttpUtil.jsonToClass(req.getReader(), UpdateCartItemRequestDto.class);

            cartService.updateQuantity(userId, cartItemId, dto.getQuantity());
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "Cart item updated successfully");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
    /**
     * DELETE /cart/items/{cartItemId}
     * - Remove one cart item (owned by current user)
     */

    public void handleRemoveItem(HttpServletRequest req, HttpServletResponse resp, Integer cartItemId)
     throws IOException {
        try {
            Integer userId = JwtUtil.getUserIdFromAuthorizationHeader(req.getHeader("Authorization"));
            cartService.removeItem(userId, cartItemId);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "Cart item removed successfully");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
    /**
     * DELETE /cart
     * - Clear entire cart of current user (typically after checkout succeeds)
     */

    public void handleClearCart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer userId = JwtUtil.getUserIdFromAuthorizationHeader(req.getHeader("Authorization"));
            cartService.clearCart(userId);
            HttpUtil.sendJson(resp, HttpServletResponse.SC_OK, "Cart cleared successfully");
        } catch (Exception e) {
            HttpUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
